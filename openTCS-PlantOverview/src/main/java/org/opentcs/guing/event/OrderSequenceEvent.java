/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.event;

import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.order.OrderSequence;

/**
 * Indicates changes of an order sequence's state.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderSequenceEvent
    extends EventObject {

  /**
   * The event's type.
   */
  private final Type type;
  /**
   * The sequence this event is all about.
   */
  private final OrderSequence sequence;

  public OrderSequenceEvent(Object source, OrderSequence sequence, Type type) {
    super(source);
    this.sequence = requireNonNull(sequence, "sequence");
    this.type = requireNonNull(type, "type");
  }

  /**
   * Returns this event's type.
   *
   * @return This event's type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the order sequence.
   *
   * @return The order sequence.
   */
  public OrderSequence getSequence() {
    return sequence;
  }

  public static enum Type {

    /**
     * Indicates the order sequence was created.
     */
    SEQ_CREATED,
    /**
     * Indicates the order sequence was modified in some way.
     */
    SEQ_CHANGED,
    /**
     * Indicates the order sequence was removed.
     */
    SEQ_REMOVED;
  }
}
