// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.plantoverview;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;

/**
 * Objects implementing this interface provide a set of suggested types for
 * {@link VehicleCommAdapterMessage}s and keys and values for their parameters.
 */
public interface VehicleCommAdapterMessageSuggestions {

  /**
   * Returns suggested types for {@link VehicleCommAdapterMessage}s.
   *
   * @return Suggested types for {@link VehicleCommAdapterMessage}s.
   */
  @Nonnull
  Set<String> getTypeSuggestions();

  /**
   * Returns a map of parameter suggestions that are specified for the given
   * {@link VehicleCommAdapterMessage} type.
   * <p>
   * The map contains parameter keys mapped to suggestions for the corresponding parameter's values.
   * </p>
   *
   * @param type The {@link VehicleCommAdapterMessage} type for which parameter suggestions are
   * requested.
   * @return A map of parameter suggestions.
   */
  @Nonnull
  Map<String, Set<String>> getParameterSuggestionsFor(
      @Nonnull
      String type
  );
}
