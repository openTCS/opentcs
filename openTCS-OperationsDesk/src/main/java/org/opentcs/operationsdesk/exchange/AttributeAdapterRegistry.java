/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.exchange;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.event.SystemModelTransitionEvent;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.exchange.adapter.LocationLockAdapter;
import org.opentcs.operationsdesk.exchange.adapter.PathLockAdapter;
import org.opentcs.operationsdesk.exchange.adapter.VehicleAllowedOrderTypesAdapter;
import org.opentcs.operationsdesk.exchange.adapter.VehiclePausedAdapter;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;

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
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
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
                                  ModelManager modelManager,
                                  @ApplicationEventBus EventSource eventSource) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
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
      model.addAttributesChangeListener(new VehicleAllowedOrderTypesAdapter(portalProvider,
                                                                            model));
      model.addAttributesChangeListener(new VehiclePausedAdapter(portalProvider, model));
    }
    for (PathModel model : modelManager.getModel().getPathModels()) {
      model.addAttributesChangeListener(new PathLockAdapter(portalProvider, model));
    }
    for (LocationModel model : modelManager.getModel().getLocationModels()) {
      model.addAttributesChangeListener(new LocationLockAdapter(portalProvider, model));
    }
  }
}
