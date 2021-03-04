/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.util.Set;

/**
 * Implementations of this class provide suggestions for transport order categories.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface OrderCategorySuggestions {

  /**
   * Returns a set of categories that can be assigned to a transport order.
   *
   * @return A set of categories that can be assigned to a transport order.
   */
  Set<String> getCategorySuggestions();
}
