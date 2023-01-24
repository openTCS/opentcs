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
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
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

  private final RerouteUtil rerouteUtil;
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
   * @param vehicleService The vehicle service.
   * @param eventSource Where this instance registers for application events.
   * @param kernelExecutor Executes dispatching tasks.
   * @param fullDispatchTask The full dispatch task.
   * @param periodicDispatchTaskProvider Provides the periodic vehicle redospatching task.
   * @param configuration The dispatcher configuration.
   * @param rerouteUtil The reroute util.
   */
  @Inject
  public DefaultDispatcher(OrderReservationPool orderReservationPool,
                           TransportOrderUtil transportOrderUtil,
                           InternalVehicleService vehicleService,
                           @ApplicationEventBus EventSource eventSource,
                           @KernelExecutor ScheduledExecutorService kernelExecutor,
                           FullDispatchTask fullDispatchTask,
                           Provider<PeriodicVehicleRedispatchingTask> periodicDispatchTaskProvider,
                           DefaultDispatcherConfiguration configuration,
                           RerouteUtil rerouteUtil) {
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.fullDispatchTask = requireNonNull(fullDispatchTask, "fullDispatchTask");
    this.periodicDispatchTaskProvider = requireNonNull(periodicDispatchTaskProvider,
                                                       "periodicDispatchTaskProvider");
    this.configuration = requireNonNull(configuration, "configuration");
    this.rerouteUtil = requireNonNull(rerouteUtil, "rerouteUtil");
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
  public void topologyChanged() {
    if (configuration.rerouteOnTopologyChanges()) {
      LOG.debug("Scheduling reroute task...");
      kernelExecutor.submit(() -> {
        LOG.info("Rerouting all vehicles due to topology change...");
        rerouteUtil.reroute(vehicleService.fetchObjects(Vehicle.class), ReroutingType.REGULAR);
      });
    }
  }

  @Override
  public void reroute(Vehicle vehicle, ReroutingType reroutingType) {
    LOG.debug("Scheduling reroute task...");
    kernelExecutor.submit(() -> {
      LOG.info(
          "Rerouting vehicle due to explicit request: {} ({}, current position {})...",
          vehicle.getName(),
          reroutingType,
          vehicle.getCurrentPosition() == null ? null : vehicle.getCurrentPosition().getName()
      );
      rerouteUtil.reroute(vehicle, reroutingType);
    });
  }
}
