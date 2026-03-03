// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
// tag::tutorial_gettingstarted_MyKernelExtension[]
package com.example;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exemplary kernel extension.
 */
public class MyKernelExtension
    implements
      KernelExtension,
      EventHandler {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MyKernelExtension.class);
  /**
   * The event bus to subscribe with.
   */
  private final EventSource eventBus;
  /**
   * Whether this extension has been initialized already.
   */
  private boolean initialized;

  @Inject
  public MyKernelExtension(
      @ApplicationEventBus
      EventSource eventBus
  ) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    LOG.info("Subscribing with event bus...");
    eventBus.subscribe(this);

    initialized = true;
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

    LOG.info("Unsubscribing with event bus...");
    eventBus.unsubscribe(this);

    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent objectEvent
        && objectEvent.getType() == TCSObjectEvent.Type.OBJECT_MODIFIED
        && objectEvent.getCurrentObjectState() instanceof Vehicle) {
      processObjectModifiedEvent(
          (Vehicle) objectEvent.getPreviousObjectState(),
          (Vehicle) objectEvent.getCurrentObjectState()
      );
    }
  }

  private void processObjectModifiedEvent(Vehicle oldVehicleState, Vehicle newVehicleState) {
    if (oldVehicleState.getTransportOrder() != newVehicleState.getTransportOrder()
        && newVehicleState.getTransportOrder() != null) {
      LOG.info(
          "Vehicle '{}' starts processing transport order '{}'",
          newVehicleState.getName(),
          newVehicleState.getTransportOrder().getName()
      );
    }
  }
}
// end::tutorial_gettingstarted_MyKernelExtension[]
