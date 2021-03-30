/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

/**
 * Defines some constants for {@link TransportOrder}s and {@link OrderSequence}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface OrderConstants {

  /**
   * The string representing the <em>any</em> type.
   * Primarily intended to be used for a vehicle to indicate there are no restrictions to its 
   * allowed oder types.
   */
  String TYPE_ANY = "*";
  /**
   * The default type of orders.
   */
  String TYPE_NONE = "-";
  /**
   * A type for charge orders.
   */
  String TYPE_CHARGE = "Charge";
  /**
   * A type for park orders.
   */
  String TYPE_PARK = "Park";
  /**
   * A type for transport orders.
   */
  String TYPE_TRANSPORT = "Transport";
}
