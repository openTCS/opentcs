/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.exchange.adapter.PathLockAdapter;
import org.opentcs.guing.exchange.adapter.VehicleProcessableCategoriesAdapter;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles registering of model attribute adapters that update a model component's attribute with
 * the kernel when it changes.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AttributeAdapterRegistry
    implements EventHandler,
               Lifecycle {

  /**
   * This class' logger.
   */
  private final Logger LOG = LoggerFactory.getLogger(AttributeAdapterRegistry.class);
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The state of the plant overview.
   */
  private final ApplicationState applicationState;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The event soruce we're registering with.
   */
  private final EventSource eventSource;
  /**
   * Whether this instance is initialized or not.
   */
  private boolean initialized;

  @Inject
  public AttributeAdapterRegistry(SharedKernelServicePortalProvider portalProvider,
                                  ApplicationState applicationState,
                                  ModelManager modelManager,
                                  @ApplicationEventBus EventSource eventSource) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.applicationState = requireNonNull(applicationState, "applicationState");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.eventSource = requireNonNull(eventSource, "eventSource");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventSource.subscribe(this);
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

    eventSource.unsubscribe(this);
    initialized = false;
  }

  @Override
  public void onEvent(Object event) {
    if (applicationState.getOperationMode() != OperationMode.OPERATING) {
      LOG.debug("Ignoring update because the application is not in operating mode and therefore "
          + "not connected to a kernel.");
      return;
    }

    if (event instanceof SystemModelTransitionEvent) {
      SystemModelTransitionEvent evt = (SystemModelTransitionEvent) event;
      switch (evt.getStage()) {
        case LOADED:
          registerAdapters();
          break;
        default:
      }
    }
  }

  private void registerAdapters() {
    for (VehicleModel model : modelManager.getModel().getVehicleModels()) {
      model.addAttributesChangeListener(new VehicleProcessableCategoriesAdapter(portalProvider,
                                                                                applicationState,
                                                                                model));
    }
    for (PathModel model : modelManager.getModel().getPathModels()) {
      model.addAttributesChangeListener(new PathLockAdapter(portalProvider,
                                                            applicationState,
                                                            model));
    }
  }
}
