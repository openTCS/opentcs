// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.order;

/**
 * Defines some constants for {@link TransportOrderCreationTO}s and
 * {@link OrderSequenceCreationTO}s.
 */
public interface OrderConstantsTO {

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
