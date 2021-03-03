/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.guing.transport.OrderSequenceListener;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * A special event dispatcher for transport order sequences.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class OrderSequenceDispatcher
    implements EventListener<TCSEvent> {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(OrderSequenceDispatcher.class.getName());
  /**
   * The central dispatcher to which incoming events are forwarded.
   */
  private final OpenTCSEventDispatcher fDispatcher;
  /**
   * The listeners.
   */
  private final List<OrderSequenceListener> fListeners = new ArrayList<>();

  /**
   * Creates a new instance of OrderSequenceDispatcher.
   *
   * @param dispatcher The injected {@link OpenTCSEventDispatcher}.
   */
  public OrderSequenceDispatcher(OpenTCSEventDispatcher dispatcher) {
    fDispatcher = requireNonNull(dispatcher, "dispatcher");
  }

  /**
   * Fügt einen OrderSequenceListener hinzu.
   *
   * @param listener der Listener
   */
  public void addListener(OrderSequenceListener listener) {
    fListeners.add(listener);
  }

  @Override
  public void processEvent(TCSEvent event) {
    // TODO: Synchronized???
    TCSObjectEvent objEvent = (TCSObjectEvent) event;
    OrderSequence os = (OrderSequence) objEvent.getCurrentOrPreviousObjectState();

    switch (objEvent.getType()) {
      case OBJECT_CREATED:
        orderSequenceAdded(os);
        break;

      case OBJECT_MODIFIED:
        orderSequenceChanged(os);
        break;

      case OBJECT_REMOVED:
        orderSequenceRemoved(os);
        break;
    }
  }

  /**
   * Returns all order sequences.
   *
   * @return All order sequences.
   */
  public Set<OrderSequence> getOrderSequences() {
    try {
      return fDispatcher.getKernel().getTCSObjects(OrderSequence.class);
    }
    catch (CredentialsException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
      return null;
    }
  }

  /**
   * Informs the listeners that a new order sequence was added.
   *
   * @param os The newly created order sequence.
   */
  private void orderSequenceAdded(OrderSequence os) {
    for (OrderSequenceListener listener : fListeners) {
      listener.orderSequenceAdded(os);
    }
  }

  /**
   * Informs the listeners that a order sequence has changed.
   *
   * @param os The order sequence that has changed.
   */
  private void orderSequenceChanged(OrderSequence os) {
    for (OrderSequenceListener listener : fListeners) {
      listener.orderSequenceChanged(os);
    }
  }

  /**
   * Informs the listeners that a order sequence was removed.
   *
   * @param os The removed order sequence.
   */
  private void orderSequenceRemoved(OrderSequence os) {
    for (OrderSequenceListener listener : fListeners) {
      listener.orderSequenceRemoved(os);
    }
  }
}
