/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Defines some constants for {@link TransportOrder}s and {@link OrderSequence}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface OrderConstants {

  /**
   * A category string indicating <em>any</em> category, primarily intended to be used for a
   * vehicle to indicate there are no restrictions to its processable categories.
   *
   * @deprecated Use {@link #TYPE_ANY} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed in favor of the type equivalent.")
  String CATEGORY_ANY = "*";
  /**
   * The string representing the <em>any</em> type.
   * Primarily intended to be used for a vehicle to indicate there are no restrictions to its 
   * allowed oder types.
   */
  String TYPE_ANY = "*";
  /**
   * The default category name for orders.
   *
   * @deprecated Use {@link #TYPE_NONE} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed in favor of the type equivalent.")
  String CATEGORY_NONE = "-";
  /**
   * The default type of orders.
   */
  String TYPE_NONE = "-";
  /**
   * The category name for charge orders.
   *
   * @deprecated Use {@link #TYPE_CHARGE} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed in favor of the type equivalent.")
  String CATEGORY_CHARGE = "Charge";
  /**
   * A type for charge orders.
   */
  String TYPE_CHARGE = "Charge";
  /**
   * The category name for park orders.
   *
   * @deprecated Use {@link #TYPE_PARK} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed in favor of the type equivalent.")
  String CATEGORY_PARK = "Park";
  /**
   * A type for park orders.
   */
  String TYPE_PARK = "Park";
  /**
   * The category name for transport orders.
   *
   * @deprecated Use {@link #TYPE_TRANSPORT} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed in favor of the type equivalent.")
  String CATEGORY_TRANSPORT = "Transport";
  /**
   * A type for transport orders.
   */
  String TYPE_TRANSPORT = "Transport";
}
