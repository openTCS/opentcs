// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.plantoverview;

import java.util.Set;

/**
 * Implementations of this class provide suggestions for transport order types.
 */
public interface OrderTypeSuggestions {

  /**
   * Returns a set of types that can be assigned to a transport order.
   *
   * @return A set of types that can be assigned to a transport order.
   */
  Set<String> getTypeSuggestions();
}
