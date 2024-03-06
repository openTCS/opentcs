/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import org.opentcs.strategies.basic.dispatching.phase.assignment.OrderAssigner;
import static com.google.common.base.Preconditions.checkState;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.dipatching.TransportOrderAssignmentException;
import org.opentcs.components.kernel.dipatching.TransportOrderAssignmentVeto;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches transport orders and vehicles.
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
   * The kernel's executor.
   */
  private final ScheduledExecutorService kernelExecutor;

  private final FullDispatchTask fullDispatchTask;

  private final Provider<PeriodicVehicleRedispatchingTask> periodicDispatchTaskProvider;

  private final DefaultDispatcherConfiguration configuration;

  private final RerouteUtil rerouteUtil;

  private final OrderAssigner orderAssigner;

  private final TransportOrderAssignmentChecker transportOrderAssignmentChecker;

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
   * @param kernelExecutor Executes dispatching tasks.
   * @param fullDispatchTask The full dispatch task.
   * @param periodicDispatchTaskProvider Provides the periodic vehicle redospatching task.
   * @param configuration The dispatcher configuration.
   * @param rerouteUtil The reroute util.
   * @param orderAssigner Handles assignments of transport orders to vehicles.
   * @param transportOrderAssignmentChecker Checks whether the assignment of transport orders to
   * vehicles is possible.
   */
  @Inject
  public DefaultDispatcher(OrderReservationPool orderReservationPool,
                           TransportOrderUtil transportOrderUtil,
                           InternalVehicleService vehicleService,
                           @KernelExecutor ScheduledExecutorService kernelExecutor,
                           FullDispatchTask fullDispatchTask,
                           Provider<PeriodicVehicleRedispatchingTask> periodicDispatchTaskProvider,
                           DefaultDispatcherConfiguration configuration,
                           RerouteUtil rerouteUtil,
                           OrderAssigner orderAssigner,
                           TransportOrderAssignmentChecker transportOrderAssignmentChecker) {
    this.orderReservationPool = requireNonNull(orderReservationPool, "orderReservationPool");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.fullDispatchTask = requireNonNull(fullDispatchTask, "fullDispatchTask");
    this.periodicDispatchTaskProvider = requireNonNull(periodicDispatchTaskProvider,
                                                       "periodicDispatchTaskProvider");
    this.configuration = requireNonNull(configuration, "configuration");
    this.rerouteUtil = requireNonNull(rerouteUtil, "rerouteUtil");
    this.orderAssigner = requireNonNull(orderAssigner, "orderAssigner");
    this.transportOrderAssignmentChecker = requireNonNull(transportOrderAssignmentChecker,
                                                          "transportOrderAssignmentChecker");
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

    fullDispatchTask.terminate();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void dispatch() {
    LOG.debug("Executing dispatch task...");
    fullDispatchTask.run();
  }

  @Override
  public void withdrawOrder(TransportOrder order, boolean immediateAbort) {
    requireNonNull(order, "order");
    checkState(isInitialized(), "Not initialized");

    LOG.debug("Withdrawing transport order '{}' (immediate={})...",
              order.getName(),
              immediateAbort);
    transportOrderUtil.abortOrder(order, immediateAbort);
  }

  @Override
  public void withdrawOrder(Vehicle vehicle, boolean immediateAbort) {
    requireNonNull(vehicle, "vehicle");
    checkState(isInitialized(), "Not initialized");

    LOG.debug("Withdrawing transport order for vehicle '{}' (immediate={})...",
              vehicle.getName(),
              immediateAbort);
    transportOrderUtil.abortOrder(vehicle, immediateAbort);
  }

  @Override
  public void reroute(Vehicle vehicle, ReroutingType reroutingType) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(reroutingType, "reroutingType");

    LOG.info(
        "Rerouting vehicle '{}' from its current position '{}' using rerouting type '{}'...",
        vehicle.getName(),
        vehicle.getCurrentPosition() == null ? null : vehicle.getCurrentPosition().getName(),
        reroutingType
    );
    rerouteUtil.reroute(vehicle, reroutingType);
  }

  @Override
  public void rerouteAll(ReroutingType reroutingType) {
    requireNonNull(reroutingType, "reroutingType");

    LOG.info("Rerouting all vehicles using rerouting type '{}'...", reroutingType);
    rerouteUtil.reroute(vehicleService.fetchObjects(Vehicle.class), reroutingType);
  }

  @Override
  public void assignNow(TransportOrder transportOrder)
      throws TransportOrderAssignmentException {
    requireNonNull(transportOrder, "transportOrder");

    TransportOrderAssignmentVeto assignmentVeto
        = transportOrderAssignmentChecker.checkTransportOrderAssignment(transportOrder);

    if (assignmentVeto != TransportOrderAssignmentVeto.NO_VETO) {
      throw new TransportOrderAssignmentException(
          transportOrder.getReference(),
          transportOrder.getIntendedVehicle(),
          assignmentVeto
      );
    }

    orderAssigner.tryAssignments(
        List.of(vehicleService.fetchObject(Vehicle.class, transportOrder.getIntendedVehicle())),
        List.of(transportOrder)
    );
  }
}
