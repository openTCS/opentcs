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
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.guing.event.OrderSequenceEvent;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * A special event dispatcher for transport order sequences.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderSequenceDispatcher
    implements EventListener<TCSEvent> {

  /**
   * The event bus to dispatch events to.
   */
  private final MBassador<Object> eventBus;

  /**
   * Creates a new instance of OrderSequenceDispatcher.
   *
   * @param eventBus The event bus to dispatch events to.
   */
  @Inject
  public OrderSequenceDispatcher(MBassador<Object> eventBus) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
  }

  @Override
  public void processEvent(TCSEvent event) {
    if (!(event instanceof TCSObjectEvent)) {
      return;
    }
    TCSObjectEvent objEvent = (TCSObjectEvent) event;

    if (!(objEvent.getCurrentOrPreviousObjectState() instanceof OrderSequence)) {
      return;
    }

    OrderSequence os = (OrderSequence) objEvent.getCurrentOrPreviousObjectState();

    switch (objEvent.getType()) {
      case OBJECT_CREATED:
        eventBus.publish(new OrderSequenceEvent(this, os, OrderSequenceEvent.Type.SEQ_CREATED));
        break;

      case OBJECT_MODIFIED:
        eventBus.publish(new OrderSequenceEvent(this, os, OrderSequenceEvent.Type.SEQ_CHANGED));
        break;

      case OBJECT_REMOVED:
        eventBus.publish(new OrderSequenceEvent(this, os, OrderSequenceEvent.Type.SEQ_REMOVED));
        break;

      default:
    }
  }
}
