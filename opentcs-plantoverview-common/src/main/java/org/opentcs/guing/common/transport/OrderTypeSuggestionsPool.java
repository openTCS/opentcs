// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.transport;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.TreeSet;
import org.opentcs.components.plantoverview.OrderTypeSuggestions;

/**
 * A collection of all transport order types suggested.
 */
public class OrderTypeSuggestionsPool
    implements
      OrderTypeSuggestions {

  /**
   * The transport order type suggestions.
   */
  private final Set<String> typeSuggestions = new TreeSet<>();

  @Inject
  public OrderTypeSuggestionsPool(Set<OrderTypeSuggestions> typeSuggestions) {
    requireNonNull(typeSuggestions, "typeSuggestions");

    for (OrderTypeSuggestions typeSuggestion : typeSuggestions) {
      this.typeSuggestions.addAll(typeSuggestion.getTypeSuggestions());
    }
  }

  @Override
  public Set<String> getTypeSuggestions() {
    return typeSuggestions;
  }

  /**
   * Adds an additional type to the pool.
   *
   * @param typeSuggestion The type to add.
   */
  public void addTypeSuggestion(String typeSuggestion) {
    typeSuggestions.add(typeSuggestion);
  }
}
