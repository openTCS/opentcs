// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.plantoverview;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Objects implementing this interface provide a set for suggested property keys and values each.
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
