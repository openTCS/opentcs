/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import java.util.HashSet;
import java.util.Set;
import org.opentcs.components.plantoverview.OrderCategorySuggestions;
import org.opentcs.data.order.OrderConstants;

/**
 * The default suggestions for transport order categories.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultOrderCategorySuggestions
    implements OrderCategorySuggestions {

  /**
   * The transport order category suggestions.
   */
  private final Set<String> categorySuggestions = new HashSet<>();

  public DefaultOrderCategorySuggestions() {
    categorySuggestions.add(OrderConstants.CATEGORY_NONE);
    categorySuggestions.add(OrderConstants.CATEGORY_CHARGE);
    categorySuggestions.add(OrderConstants.CATEGORY_PARK);
    categorySuggestions.add(OrderConstants.CATEGORY_TRANSPORT);
  }

  @Override
  public Set<String> getCategorySuggestions() {
    return categorySuggestions;
  }
}
