/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport.sequences;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.Lifecycle;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.guing.common.event.OperationModeChangeEvent;
import org.opentcs.guing.common.event.SystemModelTransitionEvent;
import org.opentcs.operationsdesk.event.KernelStateChangeEvent;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a set of all order sequences existing on the kernel.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class OrderSequencesContainer
    implements Lifecycle,
               EventHandler {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OrderSequencesContainer.class);
  /**
   * Event bus.
   */
  private final EventBus eventBus;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The order sequences.
   */
  private final Map<String, OrderSequence> orderSequences = new HashMap<>();
  /**
   * This container's listeners.
   */
  private final Set<OrderSequenceContainerListener> listeners = new HashSet<>();
  /**
   * Whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public OrderSequencesContainer(@ApplicationEventBus EventBus eventBus,
                                 SharedKernelServicePortalProvider portalProvider) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public boolean isInitialized() {
    return initialized;
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
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent) {
      handleObjectEvent((TCSObjectEvent) event);
    }
    else if (event instanceof OperationModeChangeEvent) {
      initSequences();
    }
    else if (event instanceof SystemModelTransitionEvent) {
      initSequences();
    }
    else if (event instanceof KernelStateChangeEvent) {
      initSequences();
    }
  }

  private void initSequences() {
    setOrderSequences(fetchSequencesIfOnline());
    listeners.forEach(listener -> listener.containerInitialized(orderSequences.values()));
  }

  private Set<OrderSequence> fetchSequencesIfOnline() {
    if (portalProvider.portalShared()) {
      try ( SharedKernelServicePortal sharedPortal = portalProvider.register()) {
        return sharedPortal.getPortal().getTransportOrderService()
            .fetchObjects(OrderSequence.class);
      }
      catch (KernelRuntimeException exc) {
        LOG.warn("Exception fetching order sequences", exc);
      }
    }

    return new HashSet<>();
  }

  public void addListener(OrderSequenceContainerListener listener) {
    listeners.add(listener);
  }

  public void removeListener(OrderSequenceContainerListener listener) {
    listeners.remove(listener);
  }

  public Collection<OrderSequence> getOrderSequences() {
    return orderSequences.values();
  }

  private void handleObjectEvent(TCSObjectEvent evt) {
    if (evt.getCurrentOrPreviousObjectState() instanceof OrderSequence) {
      switch (evt.getType()) {
        case OBJECT_CREATED:
          orderSequenceAdded((OrderSequence) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_MODIFIED:
          orderSequencesChanged((OrderSequence) evt.getCurrentOrPreviousObjectState());
          break;
        case OBJECT_REMOVED:
          orderSequenceRemoved((OrderSequence) evt.getCurrentOrPreviousObjectState());
          break;
        default:
          LOG.warn("Unhandled event type: {}", evt.getType());
      }
    }
  }

  private void orderSequenceAdded(OrderSequence seq) {
    orderSequences.put(seq.getName(), seq);
    listeners.forEach(listener -> listener.orderSequenceAdded(seq));
  }

  private void orderSequencesChanged(OrderSequence seq) {
    orderSequences.put(seq.getName(), seq);
    listeners.forEach(listener -> listener.orderSequenceUpdated(seq));
  }

  private void orderSequenceRemoved(OrderSequence seq) {
    orderSequences.remove(seq.getName());
    listeners.forEach(listener -> listener.orderSequenceRemoved(seq));
  }

  private void setOrderSequences(Set<OrderSequence> sequences) {
    orderSequences.clear();
    for (OrderSequence seq : sequences) {
      orderSequences.put(seq.getName(), seq);
    }
  }

}
