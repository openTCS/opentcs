/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static java.util.Objects.requireNonNull;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers dispatching of vehicles and transport orders on certain events.
 */
public class VehicleDispatchTrigger
    implements EventHandler,
               Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleDispatchTrigger.class);
  /**
   * The dispatcher in use.
   */
  private final DispatcherService dispatcher;
  /**
   * The event bus.
   */
  private final EventBus eventBus;
  /**
   * The app configuration.
   */
  private final KernelApplicationConfiguration configuration;
  /**
   * The kernel executor.
   */
  private final Executor kernelExecutor;
  /**
   * This instance's <em>initialized</em> flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param kernelExecutor The kernel executor to use.
   * @param eventBus The event bus.
   * @param dispatcher The dispatcher in use.
   * @param configuration The application configuration.
   */
  @Inject
  public VehicleDispatchTrigger(@KernelExecutor Executor kernelExecutor,
                                @ApplicationEventBus EventBus eventBus,
                                DispatcherService dispatcher,
                                KernelApplicationConfiguration configuration) {
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
    eventBus.subscribe(this);
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
    eventBus.unsubscribe(this);
  }

  @Override
  public void onEvent(Object event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }
    TCSObjectEvent objectEvent = (TCSObjectEvent) event;
    if (objectEvent.getCurrentOrPreviousObjectState() instanceof Vehicle) {
      checkVehicleChange((Vehicle) objectEvent.getPreviousObjectState(),
                         (Vehicle) objectEvent.getCurrentObjectState());
    }
  }

  private void checkVehicleChange(Vehicle oldVehicle, Vehicle newVehicle) {
    if (driveOrderFinished(oldVehicle, newVehicle)
        && configuration.rerouteOnDriveOrderFinished()) {
      LOG.debug("Rerouting vehicle {}...", newVehicle);
      dispatcher.reroute(newVehicle.getReference(), ReroutingType.REGULAR);
    }

    if ((newVehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
         || newVehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_RESPECTED)
        && (idleAndEnergyLevelChanged(oldVehicle, newVehicle)
            || awaitingNextOrder(oldVehicle, newVehicle)
            || orderSequenceNulled(oldVehicle, newVehicle))) {
      LOG.debug("Dispatching for {}...", newVehicle);
      // Dispatching may result in changes to the vehicle and thus trigger this code, which would
      // then lead to a second dispatch run before the first one is completed. To avoid this, we
      // ensure dispatching is done at some later point by scheduling it to be executed on the
      // kernel executor (so it does not trigger itself in a loop).
      kernelExecutor.execute(() -> dispatcher.dispatch());
    }
  }

  private boolean idleAndEnergyLevelChanged(Vehicle oldVehicle, Vehicle newVehicle) {
    // If the vehicle is idle and its energy level changed, we may want to order it to recharge.
    return newVehicle.hasProcState(Vehicle.ProcState.IDLE)
        && (newVehicle.hasState(Vehicle.State.IDLE) || newVehicle.hasState(Vehicle.State.CHARGING))
        && newVehicle.getEnergyLevel() != oldVehicle.getEnergyLevel();
  }

  private boolean awaitingNextOrder(Vehicle oldVehicle, Vehicle newVehicle) {
    // If the vehicle's processing state changed to IDLE or AWAITING_ORDER, it is waiting for
    // its next order, so look for one.
    return newVehicle.getProcState() != oldVehicle.getProcState()
        && (newVehicle.hasProcState(Vehicle.ProcState.IDLE)
            || newVehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER));
  }

  private boolean orderSequenceNulled(Vehicle oldVehicle, Vehicle newVehicle) {
    // If the vehicle's order sequence reference has become null, the vehicle has just been released
    // from an order sequence, so we may look for new assignments.
    return newVehicle.getOrderSequence() == null
        && oldVehicle.getOrderSequence() != null;
  }

  private boolean driveOrderFinished(Vehicle oldVehicle, Vehicle newVehicle) {
    // If the vehicle's processing state changes to AWAITING_ORDER, it has finished its current
    // drive order.
    return newVehicle.getProcState() != oldVehicle.getProcState()
        && newVehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER);
  }
}
