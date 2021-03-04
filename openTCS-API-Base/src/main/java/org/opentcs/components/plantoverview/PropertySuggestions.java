/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Objects implementing this interface provide a set for suggested property keys and values each.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public interface PropertySuggestions {

  /**
   * Returns suggested property keys.
   *
   * @return Suggested property keys.
   */
  @Nonnull
  Set<String> getKeySuggestions();

  /**
   * Returns suggested property values.
   *
   * @return Suggested property values.
   */
  @Nonnull
  Set<String> getValueSuggestions();

  /**
   * Returns suggested property values that are specified for the <Code>key</Code>.
   *
   * @param key A key suggestion for which value suggestions are requested.
   * @return A set of property value suggestions.
   */
  default Set<String> getValueSuggestionsFor(String key) {
    return new HashSet<>();
  }
}
