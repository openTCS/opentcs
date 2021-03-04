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
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelClient;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.access.TCSKernelStateEvent;
import org.opentcs.access.TCSNotificationEvent;
import org.opentcs.access.rmi.RemoteKernelConnection;
import org.opentcs.access.rmi.TCSProxyStateEvent;
import org.opentcs.data.TCSObjectEvent;
import static org.opentcs.data.TCSObjectEvent.Type.OBJECT_MODIFIED;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.event.KernelStateChangeEvent;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.util.MessageDisplay;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The openTCS implementation of the abstract event dispatcher.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSEventDispatcher
    extends AbstractEventDispatcher
    implements EventListener<TCSEvent> {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSEventDispatcher.class);
  /**
   * A display for messages received from the kernel.
   */
  private final MessageDisplay messageDisplay;
  /**
   * The transport order dispatcher.
   */
  private final TransportOrderDispatcher fTransportOrderDispatcher;
  /**
   * The transport order sequence dispatcher.
   */
  private final OrderSequenceDispatcher fOrderSequenceDispatcher;
  /**
   * The application's event bus.
   */
  private final MBassador<Object> eventBus;
  /**
   * A reference to a shared kernel instance.
   */
  private SharedKernelClient kernelClient;

  /**
   * Creates a new instance.
   *
   * @param kernelProvider Provides a access to a kernel.
   * @param messageDisplay A display for messages received from the kernel.
   * @param eventBus The application's event bus.
   * @param orderDispatcher Handles events concerning transport orders.
   * @param sequenceDispatcher Handles events concerning order sequences.
   */
  @Inject
  public OpenTCSEventDispatcher(SharedKernelProvider kernelProvider,
                                MessageDisplay messageDisplay,
                                MBassador<Object> eventBus,
                                TransportOrderDispatcher orderDispatcher,
                                OrderSequenceDispatcher sequenceDispatcher) {
    super(kernelProvider);
    this.messageDisplay = requireNonNull(messageDisplay, "messageDisplay");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.fTransportOrderDispatcher = requireNonNull(orderDispatcher,
                                                    "orderDispatcher");
    this.fOrderSequenceDispatcher = requireNonNull(sequenceDispatcher,
                                                   "sequenceDispatcher");
  }

  @Override
  public void register() {
    LOG.debug("EventDispatcher {} registering with kernel...", this);
    if (!getKernelProvider().kernelShared()) {
      LOG.warn("No shared kernel to register with, aborting.");
      return;
    }

    kernelClient = getKernelProvider().register();

    kernelClient.getKernel().addEventListener(fTransportOrderDispatcher);
    kernelClient.getKernel().addEventListener(fOrderSequenceDispatcher);
    kernelClient.getKernel().addEventListener(this);
  }

  @Override
  public void release() {
    LOG.debug("EventDispatcher {} unregistering with kernel...", this);
    if (!getKernelProvider().kernelShared() || kernelClient == null) {
      LOG.warn("No shared kernel to unregister with, aborting.");
      return;
    }

    kernelClient.getKernel().removeEventListener(fTransportOrderDispatcher);
    kernelClient.getKernel().removeEventListener(fOrderSequenceDispatcher);
    kernelClient.getKernel().removeEventListener(this);

    kernelClient.close();
    kernelClient = null;
  }

  @Override
  public void processEvent(TCSEvent event) {
    if (event instanceof TCSObjectEvent) {
      processObjectEvent((TCSObjectEvent) event);
    }
    else if (event instanceof TCSKernelStateEvent) {
      TCSKernelStateEvent kse = (TCSKernelStateEvent) event;

      // React instantly on SHUTDOWN of the kernel, otherwise wait for
      // the transition to finish
      if (kse.isTransitionFinished()
          || kse.getEnteredState() == Kernel.State.SHUTDOWN) {
        eventBus.publish(new KernelStateChangeEvent(
            this,
            KernelStateChangeEvent.convertKernelState(kse.getEnteredState())));
      }
    }
    else if (event instanceof TCSProxyStateEvent) {
      TCSProxyStateEvent pse = (TCSProxyStateEvent) event;

      if (pse.getEnteredState() == RemoteKernelConnection.State.DISCONNECTED) {
        eventBus.publish(new KernelStateChangeEvent(this,
                                                    KernelStateChangeEvent.State.DISCONNECTED));
      }
    }
    else if (event instanceof TCSNotificationEvent) {
      messageDisplay.display(((TCSNotificationEvent) event).getNotification());
    }
  }

  @Handler
  public void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case UNLOADING:
        release();
        break;
      case UNLOADED:
        // XXX Explicitly unsubscribing from the event bus when the old
        // XXX system model is thrown away is unelegant but currently necessary.
        // XXX If we did not do this, the dispatcher would re-register with the
        // XXX kernel upon the following LOADED event before the garbage
        // XXX collection has a chance to implicitly unsubscribe it.
        eventBus.unsubscribe(this);
        break;
      case LOADED:
        register();
        break;
      default:
    }
  }

  @Handler
  public void handleOperationModeChange(OperationModeChangeEvent evt) {
    // If the application switches to any state other than OPERATING, we will
    // not be able to permanently communicate with the kernel any more, so
    // unregister from it.
    if (evt.getNewMode() != OperationMode.OPERATING) {
      release();
    }
  }

  private void processObjectEvent(TCSObjectEvent objectEvent) {
    LOG.debug("TCSObjectEvent received. Name: {} Event type: {}",
              objectEvent.getCurrentOrPreviousObjectState().getName(),
              objectEvent.getType().name());

    if (objectEvent.getType() == OBJECT_MODIFIED) {
      ProcessAdapter adapter
          = findProcessAdapter(objectEvent.getCurrentObjectState().getReference());
      if (adapter == null) {
        LOG.debug("No adapter found for {}",
                  objectEvent.getCurrentOrPreviousObjectState().getName());
        return;
      }
      adapter.updateModelProperties(kernelClient.getKernel(),
                                    objectEvent.getCurrentObjectState(),
                                    null);
    }
  }

}
