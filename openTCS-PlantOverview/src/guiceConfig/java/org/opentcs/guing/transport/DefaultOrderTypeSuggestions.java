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
import org.opentcs.data.order.OrderConstants;
import org.opentcs.components.plantoverview.OrderTypeSuggestions;

/**
 * The default suggestions for transport order types.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultOrderTypeSuggestions
    implements OrderTypeSuggestions {

  /**
   * The transport order type suggestions.
   */
  private final Set<String> typeSuggestions = new HashSet<>();

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
