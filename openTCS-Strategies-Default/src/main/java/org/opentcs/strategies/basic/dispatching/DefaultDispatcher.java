/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration.RerouteTrigger.TOPOLOGY_CHANGE;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches transport orders and vehicles.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultDispatcher
    implements Dispatcher {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultDispatcher.class);
  /**
   * Stores reservations of transport orders for vehicles.
   */
  private final OrderReservationPool orderReservationPool;
  /**
   * Provides services/utility methods for working with transport orders.
   */
  private final TransportOrderUtil transportOrderUtil;
  /**
   * The transport order service.
   */
  private final InternalTransportOrderService transportOrderService;
  /**
   * The vehicle service.
   */
  private final InternalVehicleService vehicleService;
  /**
   * Where we register for application events.
   */
  private final EventSource eventSource;
  /**
   * The kernel's executor.
   */
  private final ScheduledExecutorService kernelExecutor;

  private final FullDispatchTask fullDispatchTask;

  private final Provider<PeriodicVehicleRedispatchingTask> periodicDispatchTaskProvider;

  private final DefaultDispatcherConfiguration configuration;

  private final RerouteTask rerouteTask;
  /**
   *
   */
  private ImplicitDispatchTrigger implicitDispatchTrigger;

  private ScheduledFuture<?> periodicDispatchTaskFuture;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param orderReservationPool Stores reservations of transport orders for vehicles.
   * @param transportOrderUtil Provides services for working with transport orders.
   * @param transportOrderService The transport order service.
   * @param vehicleService The vehicle service.
   * @param eventSource Where this instance registers for application events.
   * @param kernelExecutor Executes dispatching tasks.
   */
  @Inject
  public DefaultDispatcher(OrderReservationPool orderReservationPool,
                           TransportOrderUtil transportOrderUtil,
                           InternalTransportOrderService transportOrderService,
                           InternalVehicleService vehicleService,
                           @ApplicationEventBus EventSource eventSource,
                           @KernelExecutor ScheduledExecutorService kernelExecutor,
                           FullDispatchTask fullDispatchTask,
                           Provider<PeriodicVehicleRedispatchingTask> periodicDispatchTaskProvider,
                           DefaultDispatcherConfiguration configuration,
                           RerouteTask rerouteTask) {
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.fullDispatchTask = requireNonNull(fullDispatchTask, "fullDispatchTask");
    this.periodicDispatchTaskProvider = requireNonNull(periodicDispatchTaskProvider,
                                                       "periodicDispatchTaskProvider");
    this.configuration = requireNonNull(configuration, "configuration");
    this.rerouteTask = requireNonNull(rerouteTask, "rerouteTask");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    LOG.debug("Initializing...");

    transportOrderUtil.initialize();
    orderReservationPool.clear();

    fullDispatchTask.initialize();

    implicitDispatchTrigger = new ImplicitDispatchTrigger(this);
    eventSource.subscribe(implicitDispatchTrigger);

    LOG.debug("Scheduling periodic dispatch task with interval of {} ms...",
              configuration.idleVehicleRedispatchingInterval());
    periodicDispatchTaskFuture = kernelExecutor.scheduleAtFixedRate(
        periodicDispatchTaskProvider.get(),
        configuration.idleVehicleRedispatchingInterval(),
        configuration.idleVehicleRedispatchingInterval(),
        TimeUnit.MILLISECONDS
    );

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    LOG.debug("Terminating...");

    periodicDispatchTaskFuture.cancel(false);
    periodicDispatchTaskFuture = null;

    eventSource.unsubscribe(implicitDispatchTrigger);
    implicitDispatchTrigger = null;

    fullDispatchTask.terminate();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void dispatch() {
    LOG.debug("Scheduling dispatch task...");
    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(fullDispatchTask);
  }

  @Override
  @Deprecated
  public void dispatch(Vehicle incomingVehicle) {
    requireNonNull(incomingVehicle, "incomingVehicle");
    checkState(isInitialized(), "Not initialized");

    LOG.debug("Processing incoming vehicle '{}'...", incomingVehicle.getName());
    // Get an up-to-date copy of the kernel's object first.
    Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, incomingVehicle.getReference());
    // Check if this vehicle is interesting for the dispatcher at all.
    if (!vehicleDispatchable(vehicle)) {
      LOG.debug("Vehicle '{}' not dispatchable, ignoring.", vehicle.getName());
      return;
    }

    LOG.debug("Scheduling dispatch task for vehicle {}...", vehicle);
    dispatch();
  }

  @Override
  @Deprecated
  public void dispatch(TransportOrder incomingOrder) {
    requireNonNull(incomingOrder, "incomingOrder");
    checkState(isInitialized(), "Not initialized");

    LOG.debug("Processing incoming transport order '{}'...", incomingOrder.getName());
    // Get an up-to-date copy of the kernel's object first.
    TransportOrder order = transportOrderService.fetchObject(TransportOrder.class,
                                                             incomingOrder.getReference());
    if (order.getState().isFinalState()) {
      LOG.warn("Transport order '{}' already in final state '{}', not dispatching it.",
               order.getName(),
               order.getState());
      return;
    }

    LOG.debug("Scheduling dispatch task for transport order {}...", order);
    // Schedule this to be executed by the kernel executor.
    dispatch();
  }

  @Override
  public void withdrawOrder(TransportOrder order, boolean immediateAbort) {
    requireNonNull(order, "order");
    checkState(isInitialized(), "Not initialized");

    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(() -> {
      LOG.debug("Scheduling withdrawal for transport order '{}' (immediate={})...",
                order.getName(),
                immediateAbort);
      transportOrderUtil.abortOrder(order, immediateAbort, false);
    });
  }

  @Override
  public void withdrawOrder(Vehicle vehicle, boolean immediateAbort) {
    requireNonNull(vehicle, "vehicle");
    checkState(isInitialized(), "Not initialized");

    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(() -> {
      LOG.debug("Scheduling withdrawal for vehicle '{}' (immediate={})...",
                vehicle.getName(),
                immediateAbort);
      transportOrderUtil.abortOrder(vehicle, immediateAbort, false, false);
    });
  }

  @Override
  @Deprecated
  public void withdrawOrder(TransportOrder order, boolean immediateAbort, boolean disableVehicle) {
    requireNonNull(order, "order");
    checkState(isInitialized(), "Not initialized");

    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(() -> {
      LOG.debug("Scheduling withdrawal for transport order '{}' (immediate={}, disable={})...",
                order.getName(),
                immediateAbort,
                disableVehicle);
      transportOrderUtil.abortOrder(order, immediateAbort, disableVehicle);
    });
  }

  @Override
  @Deprecated
  public void withdrawOrder(Vehicle vehicle, boolean immediateAbort, boolean disableVehicle) {
    requireNonNull(vehicle, "vehicle");
    checkState(isInitialized(), "Not initialized");

    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(() -> {
      LOG.debug("Scheduling withdrawal for vehicle '{}' (immediate={}, disable={})...",
                vehicle.getName(),
                immediateAbort,
                disableVehicle);
      transportOrderUtil.abortOrder(vehicle, immediateAbort, disableVehicle, false);
    });
  }

  @Override
  @Deprecated
  public void releaseVehicle(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    checkState(isInitialized(), "Not initialized");

    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(() -> {
      LOG.debug("Scheduling release for vehicle '{}'...", vehicle.getName());
      transportOrderUtil.abortOrder(vehicle, true, true, true);
    });
  }

  @Override
  public void topologyChanged() {
    if (configuration.rerouteTrigger() == TOPOLOGY_CHANGE) {
      LOG.debug("Scheduling reroute task...");
      kernelExecutor.submit(rerouteTask);
    }
  }

  private static boolean vehicleDispatchable(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getCurrentPosition() == null) {
      LOG.debug("Vehicle '{}' unknown position -> not dispatchable", vehicle.getName());
      return false;
    }
    if (vehicle.getIntegrationLevel() != Vehicle.IntegrationLevel.TO_BE_UTILIZED) {
      LOG.debug("Vehicle '{}' is not to be utilized.", vehicle.getName());
      return false;
    }
    // ProcState IDLE, State CHARGING and energy level not high enough? Then let
    // it charge a bit longer.
    if (vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.CHARGING)
        && vehicle.isEnergyLevelCritical()) {
      LOG.debug("Vehicle '{}' is CHARGING, energy level {}<={} -> not (yet) dispatchable.",
                vehicle.getName(),
                vehicle.getEnergyLevel(),
                vehicle.getEnergyLevelCritical());
      return false;
    }
    // Only dispatch vehicles that are either not processing any order at all or
    // are waiting for the next drive order.
    if (!vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && !vehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER)) {
      LOG.debug("Vehicle '{}' is in processing state {} -> not dispatchable",
                vehicle.getName(),
                vehicle.getProcState());
      return false;
    }
    return true;
  }

}
