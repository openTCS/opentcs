// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.transport;

import java.util.HashSet;
import java.util.Set;
import org.opentcs.components.plantoverview.OrderTypeSuggestions;
import org.opentcs.data.order.OrderConstants;

/**
 * The default suggestions for transport order types.
 */
public class DefaultOrderTypeSuggestions
    implements
      OrderTypeSuggestions {

  /**
   * The transport order type suggestions.
   */
  private final Set<String> typeSuggestions = new HashSet<>();

  /**
   * Creates a new instance.
   */
  public DefaultOrderTypeSuggestions() {
    typeSuggestions.add(OrderConstants.TYPE_NONE);
    typeSuggestions.add(OrderConstants.TYPE_CHARGE);
    typeSuggestions.add(OrderConstants.TYPE_PARK);
    typeSuggestions.add(OrderConstants.TYPE_TRANSPORT);
  }

  @Override
  public Set<String> getTypeSuggestions() {
    return typeSuggestions;
  }
}
