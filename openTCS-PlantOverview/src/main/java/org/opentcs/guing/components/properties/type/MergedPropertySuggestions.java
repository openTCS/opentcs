/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import com.google.inject.Inject;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeSet;
import org.opentcs.components.plantoverview.PropertySuggestions;

/**
 * Merges {@link PropertySuggestions} instances to a single one.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class MergedPropertySuggestions
    implements PropertySuggestions {

  private final Set<String> keySuggestions = new TreeSet<>();
  private final Set<String> valueSuggestions = new TreeSet<>();

  /**
   * Creates a new instance, merging the keys/values of the given suggestions sets.
   *
   * @param propertySuggestions The suggestions to be merged.
   */
  @Inject
  public MergedPropertySuggestions(Set<PropertySuggestions> propertySuggestions) {
    requireNonNull(propertySuggestions, "propertySuggestors");

    for (PropertySuggestions suggestor : propertySuggestions) {
      keySuggestions.addAll(suggestor.getKeySuggestions());
      valueSuggestions.addAll(suggestor.getValueSuggestions());
    }
  }

  @Override
  public Set<String> getKeySuggestions() {
    return keySuggestions;
  }

  @Override
  public Set<String> getValueSuggestions() {
    return valueSuggestions;
  }

}
