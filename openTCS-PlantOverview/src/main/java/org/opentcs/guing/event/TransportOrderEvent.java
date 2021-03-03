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
import org.opentcs.data.order.TransportOrder;

/**
 * Indicates changes of a transport order's state.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderEvent
    extends EventObject {

  /**
   * The event's type.
   */
  private final Type type;
  /**
   * The order this event is all about.
   */
  private final TransportOrder order;

  /**
   * Creates a new instance.
   *
   * @param source The originator of this event.
   * @param order The order this event is all about.
   * @param type This event's type.
   */
  public TransportOrderEvent(Object source, TransportOrder order, Type type) {
    super(source);
    this.order = requireNonNull(order, "order");
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
   * Returns the transport order.
   *
   * @return The transport order.
   */
  public TransportOrder getOrder() {
    return order;
  }

  public static enum Type {

    /**
     * Indicates the order sequence was created.
     */
    ORDER_CREATED,
    /**
     * Indicates the order sequence was modified in some way.
     */
    ORDER_CHANGED,
    /**
     * Indicates the order sequence was removed.
     */
    ORDER_REMOVED;
  }
}
