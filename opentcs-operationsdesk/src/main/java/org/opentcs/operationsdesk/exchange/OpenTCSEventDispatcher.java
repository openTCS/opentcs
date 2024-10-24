// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.exchange;

import static java.util.Objects.requireNonNull;
import static org.opentcs.data.TCSObjectEvent.Type.OBJECT_MODIFIED;

import jakarta.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelStateTransitionEvent;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.ClientConnectionMode;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.common.exchange.adapter.ProcessAdapterUtil;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.event.KernelStateChangeEvent;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A central event dispatcher between the kernel and the plant overview.
 */
public class OpenTCSEventDispatcher
    implements
      Lifecycle,
      EventHandler {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSEventDispatcher.class);
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
   * @param eventBus Where this instance gets events from and sends them to.
   * @param processAdapterUtil The process adapter util.
   * @param modelManager The model manager.
   */
  @Inject
  public OpenTCSEventDispatcher(
      SharedKernelServicePortalProvider portalProvider,
      @ApplicationEventBus
      EventBus eventBus,
      ProcessAdapterUtil processAdapterUtil,
      ModelManager modelManager
  ) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
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
    sharedPortal = portalProvider.register();
  }

  private void release() {
    if (sharedPortal == null) {
      return;
    }

    LOG.debug("EventDispatcher {} unregistering with portal...", this);
    sharedPortal.close();
    sharedPortal = null;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent tcsObjectEvent) {
      processObjectEvent(tcsObjectEvent);
    }
    else if (event instanceof KernelStateTransitionEvent kernelStateTransitionEvent) {
      // React instantly on SHUTDOWN of the kernel, otherwise wait for
      // the transition to finish
      if (kernelStateTransitionEvent.isTransitionFinished()
          || kernelStateTransitionEvent.getEnteredState() == Kernel.State.SHUTDOWN) {
        eventBus.onEvent(
            new KernelStateChangeEvent(
                this,
                KernelStateChangeEvent.convertKernelState(
                    kernelStateTransitionEvent.getEnteredState()
                )
            )
        );
      }
    }
    else if (event instanceof ClientConnectionMode connectionMode) {
      switch (connectionMode) {
        case ONLINE:
          handleKernelConnect();
          break;
        case OFFLINE:
          handleKernelDisconnect();
          break;
        default:
          // Do nothing.
      }
    }
  }

  private void handleKernelConnect() {
    register();

    eventBus.onEvent(
        new KernelStateChangeEvent(this, KernelStateChangeEvent.State.LOGGED_IN)
    );
    eventBus.onEvent(
        new KernelStateChangeEvent(
            this,
            KernelStateChangeEvent.convertKernelState(sharedPortal.getPortal().getState())
        )
    );
  }

  private void handleKernelDisconnect() {
    release();

    eventBus.onEvent(
        new KernelStateChangeEvent(
            this,
            KernelStateChangeEvent.State.DISCONNECTED
        )
    );
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
    adapter.updateModelProperties(
        tcsObject,
        modelComponent,
        modelManager.getModel(),
        sharedPortal.getPortal().getPlantModelService()
    );
  }

}
