// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.order;

import org.opentcs.data.order.OrderSequence;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Defines some constants for {@link TransportOrderCreationTO}s and
 * {@link OrderSequenceCreationTO}s.
 */
public interface OrderConstantsTO {

  /**
   * A special type used to indicate that an order sequence's type is not set.
   * An order sequence's type should not be set to this value by a user.
   *
   * @deprecated This sole purpose of this type is to serve as the default value for
   * {@link OrderSequence#getType() an order sequence's type}, which is scheduled for removal with
   * openTCS 8.0.
   */
  @Deprecated
  @ScheduledApiChange(when = "8.0", details = "Will be removed.")
  String TYPE_UNSET = "";
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
