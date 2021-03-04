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
   * The default category name for orders.
   */
  String CATEGORY_NONE = "-";
  /**
   * The category name for charge orders.
   */
  String CATEGORY_CHARGE = "Charge";
  /**
   * The category name for park orders.
   */
  String CATEGORY_PARK = "Park";
  /**
   * The category name for transport orders.
   */
  String CATEGORY_TRANSPORT = "Transport";
}
