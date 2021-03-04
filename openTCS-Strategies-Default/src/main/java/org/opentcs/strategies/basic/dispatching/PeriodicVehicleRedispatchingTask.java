/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically checks for idle vehicles that could process a transport order.
 * The main purpose of doing this is retrying to dispatch vehicles that were not in a dispatchable
 * state when dispatching them was last tried.
 * A potential reason for this is that a vehicle temporarily reported an error because a safety
 * sensor was triggered.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PeriodicVehicleRedispatchingTask
    implements Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeriodicVehicleRedispatchingTask.class);

  private final DispatcherService dispatcherService;

  private final TCSObjectService objectService;

  /**
   * Creates a new instance.
   *
   * @param dispatcherService The dispatcher service used to dispatch vehicles.
   * @param objectService The object service.
   */
  @Inject
  public PeriodicVehicleRedispatchingTask(DispatcherService dispatcherService,
                                          TCSObjectService objectService) {
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.objectService = requireNonNull(objectService, "objectService");
  }

  @Override
  public void run() {
    // If there are any vehicles that could process a transport order,
    // trigger the dispatcher once.
    objectService.fetchObjects(Vehicle.class, this::couldProcessTransportOrder).stream()
        .findAny()
        .ifPresent(vehicle -> {
          LOG.debug("Vehicle {} could process transport order, triggering dispatcher ...", vehicle);
          dispatcherService.dispatch();
        });
  }

  private boolean couldProcessTransportOrder(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() ==Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.getCurrentPosition() != null
        && !vehicle.isEnergyLevelCritical()
        && (processesNoOrder(vehicle)
            || processesDispensableOrder(vehicle));
  }

  private boolean processesNoOrder(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && (vehicle.hasState(Vehicle.State.IDLE)
            || vehicle.hasState(Vehicle.State.CHARGING));
  }

  private boolean processesDispensableOrder(Vehicle vehicle) {
    return vehicle.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)
        && objectService.fetchObject(TransportOrder.class, vehicle.getTransportOrder())
            .isDispensable();
  }
}
