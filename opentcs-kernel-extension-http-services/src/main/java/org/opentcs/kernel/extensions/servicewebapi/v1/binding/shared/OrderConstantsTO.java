// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

/**
 * Defines some constants for transport orders and order sequences.
 */
public abstract class OrderConstantsTO {
  /**
   * The default type of orders.
   */
  public static final String TYPE_NONE = "-";
  /**
   * A special type used to indicate that an order sequence's type is not set.
   * An order sequence's type should not be set to this value by a user.
   */
  @Deprecated
  public static final String TYPE_UNSET = "";

  private OrderConstantsTO() {
  }
}
