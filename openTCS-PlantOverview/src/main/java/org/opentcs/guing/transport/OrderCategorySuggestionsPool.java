/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.opentcs.components.plantoverview.OrderCategorySuggestions;

/**
 * A collection of all transport order categories suggested.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderCategorySuggestionsPool
    implements OrderCategorySuggestions {

  /**
   * The transport order category suggestions.
   */
  private final Set<String> categorySuggestions = new TreeSet<>();

  @Inject
  public OrderCategorySuggestionsPool(Set<OrderCategorySuggestions> categorySuggestions) {
    requireNonNull(categorySuggestions, "categorySuggestions");

    for (OrderCategorySuggestions categorySuggestion : categorySuggestions) {
      this.categorySuggestions.addAll(categorySuggestion.getCategorySuggestions());
    }
  }

  @Override
  public Set<String> getCategorySuggestions() {
    return categorySuggestions;
  }

  /**
   * Adds an additional category to the pool.
   *
   * @param categorySuggestion The category to add.
   */
  public void addCategorySuggestion(String categorySuggestion) {
    categorySuggestions.add(categorySuggestion);
  }
}
