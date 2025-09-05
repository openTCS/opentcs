// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.vehicles;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.plantoverview.VehicleCommAdapterMessageSuggestions;

/**
 * Merges {@link VehicleCommAdapterMessageSuggestions} instances to a single one.
 */
public class MergedVehicleCommAdapterMessageSuggestions
    implements
      VehicleCommAdapterMessageSuggestions {

  private final Set<String> typeSuggestions = new HashSet<>();
  private final Set<VehicleCommAdapterMessageSuggestions> messageSuggestions;

  /**
   * Creates a new instance.
   *
   * @param messageSuggestions The suggestions to be merged.
   */
  @Inject
  public MergedVehicleCommAdapterMessageSuggestions(
      Set<VehicleCommAdapterMessageSuggestions> messageSuggestions
  ) {
    this.messageSuggestions = requireNonNull(messageSuggestions, "messagesSuggestions");
    for (VehicleCommAdapterMessageSuggestions suggestor : messageSuggestions) {
      typeSuggestions.addAll(suggestor.getTypeSuggestions());
    }
  }

  @Override
  @Nonnull
  public Set<String> getTypeSuggestions() {
    return Set.copyOf(typeSuggestions);
  }

  @Nonnull
  @Override
  public Map<String, Set<String>> getParameterSuggestionsFor(
      @Nonnull
      String type
  ) {
    return messageSuggestions.stream()
        .map(suggestor -> suggestor.getParameterSuggestionsFor(type))
        .map(Map::entrySet)
        .flatMap(Set::stream)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
