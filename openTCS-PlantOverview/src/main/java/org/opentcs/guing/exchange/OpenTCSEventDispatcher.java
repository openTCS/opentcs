/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelStateTransitionEvent;
import org.opentcs.access.NotificationPublicationEvent;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.ClientConnectionMode;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import static org.opentcs.data.TCSObjectEvent.Type.OBJECT_MODIFIED;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.event.KernelStateChangeEvent;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapterUtil;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.MessageDisplay;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A central event dispatcher between the kernel and the plant overview.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSEventDispatcher
    implements Lifecycle,
               EventHandler {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSEventDispatcher.class);
  /**
   * A display for messages received from the kernel.
   */
  private final MessageDisplay messageDisplay;
  /**
   * Where we get events from and send them to.
   */
  private final EventBus eventBus;
  /**
   * The process adapter util.
   */
  private final ProcessAdapterUtil processAdapterUtil;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * A reference to a shared portal instance.
   */
  private SharedKernelServicePortal sharedPortal;
  /**
   * Whether this component is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param portalProvider Provides a access to a portal.
   * @param messageDisplay A display for messages received from the kernel.
   * @param eventBus Where this instance gets events from and sends them to.
   * @param processAdapterUtil The process adapter util.
   * @param modelManager The model manager.
   */
  @Inject
  public OpenTCSEventDispatcher(SharedKernelServicePortalProvider portalProvider,
                                MessageDisplay messageDisplay,
                                @ApplicationEventBus EventBus eventBus,
                                ProcessAdapterUtil processAdapterUtil,
                                ModelManager modelManager) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.messageDisplay = requireNonNull(messageDisplay, "messageDisplay");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.processAdapterUtil = requireNonNull(processAdapterUtil, "processAdapterUtil");
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventBus.subscribe(this);

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  private void register() {
    if (sharedPortal != null) {
      return;
    }

    LOG.debug("EventDispatcher {} registering with portal...", this);
    if (!portalProvider.portalShared()) {
      LOG.warn("No shared portal to register with, aborting.");
      return;
    }

    sharedPortal = portalProvider.register();

  }

  private void release() {
    if (sharedPortal == null) {
      return;
    }

    LOG.debug("EventDispatcher {} unregistering with portal...", this);
    if (!portalProvider.portalShared() || sharedPortal == null) {
      LOG.warn("No shared portal to unregister with, aborting.");
      return;
    }

    sharedPortal.close();
    sharedPortal = null;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent) {
      processObjectEvent((TCSObjectEvent) event);
    }
    else if (event instanceof KernelStateTransitionEvent) {
      KernelStateTransitionEvent kse = (KernelStateTransitionEvent) event;

      // React instantly on SHUTDOWN of the kernel, otherwise wait for
      // the transition to finish
      if (kse.isTransitionFinished()
          || kse.getEnteredState() == Kernel.State.SHUTDOWN) {
        eventBus.onEvent(new KernelStateChangeEvent(
            this,
            KernelStateChangeEvent.convertKernelState(kse.getEnteredState())));
      }
    }
    else if (event instanceof NotificationPublicationEvent) {
      messageDisplay.display(((NotificationPublicationEvent) event).getNotification());
    }
    else if (event instanceof OperationModeChangeEvent) {
      handleOperationModeChange((OperationModeChangeEvent) event);
    }
    else if (event == ClientConnectionMode.OFFLINE) {
      eventBus.onEvent(new KernelStateChangeEvent(this,
                                                  KernelStateChangeEvent.State.DISCONNECTED));
    }
  }

  private void handleOperationModeChange(OperationModeChangeEvent evt) {
    // If the application switches to OPERATING, we want to register with the kernel.
    // If the application switches to any state other than OPERATING, we will
    // not be able to permanently communicate with the kernel any more, so
    // unregister from it.
    if (evt.getNewMode() == OperationMode.OPERATING) {
      register();
    }
    else {
      release();
    }
  }

  private void processObjectEvent(TCSObjectEvent objectEvent) {
    LOG.debug("TCSObjectEvent received: {}", objectEvent);

    if (sharedPortal == null) {
      return;
    }

    if (objectEvent.getType() == OBJECT_MODIFIED) {
      processObjectModifiedEvent(objectEvent.getCurrentObjectState());
    }
  }

  private void processObjectModifiedEvent(TCSObject<?> tcsObject) {
    if (tcsObject instanceof TransportOrder
        || tcsObject instanceof OrderSequence) {
      // We only care about model objects (with ProcessAdapters) here, not transport orders.
      return;
    }

    ModelComponent modelComponent = modelManager.getModel()
        .getModelComponent(tcsObject.getReference().getName());
    if (modelComponent == null) {
      LOG.debug("No model component found for {}", tcsObject.getName());
      return;
    }

    ProcessAdapter adapter = processAdapterUtil.processAdapterFor(modelComponent);
    adapter.updateModelProperties(tcsObject,
                                  modelComponent,
                                  modelManager.getModel(),
                                  sharedPortal.getPortal().getPlantModelService(),
                                  null);
  }

}
