// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opentcs.components.plantoverview.VehicleCommAdapterMessageSuggestions;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.virtualvehicle.LoopbackCommAdapterMessages;

/**
 * The default suggestions for {@link VehicleCommAdapterMessage}s.
 */
public class DefaultVehicleCommAdapterMessageSuggestions
    implements
      VehicleCommAdapterMessageSuggestions {

  private final Set<String> typeSuggestions = new HashSet<>();
  private final Map<String, Map<String, Set<String>>> parameterSuggestions = new HashMap<>();

  /**
   * Creates a new instance.
   */
  public DefaultVehicleCommAdapterMessageSuggestions() {
    addTypeSuggestion(
        LoopbackCommAdapterMessages.INIT_POSITION,
        new ParameterSuggestion(LoopbackCommAdapterMessages.INIT_POSITION_PARAM_POSITION)
    );
    addTypeSuggestion(LoopbackCommAdapterMessages.CURRENT_MOVEMENT_COMMAND_FAILED);
    addTypeSuggestion(
        LoopbackCommAdapterMessages.PUBLISH_EVENT,
        new ParameterSuggestion(LoopbackCommAdapterMessages.PUBLISH_EVENT_PARAM_APPENDIX)
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_ENERGY_LEVEL,
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_ENERGY_LEVEL_PARAM_LEVEL)
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_LOADED,
        new ParameterSuggestion(
            LoopbackCommAdapterMessages.SET_LOADED_PARAM_LOADED,
            Set.of("true", "false")
        )
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_ORIENTATION_ANGLE,
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_ORIENTATION_ANGLE_PARAM_ANGLE)
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_POSITION,
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_POSITION_PARAM_POSITION)
    );
    addTypeSuggestion(LoopbackCommAdapterMessages.RESET_POSITION);
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_PRECISE_POSITION,
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_PRECISE_POSITION_PARAM_X),
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_PRECISE_POSITION_PARAM_Y),
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_PRECISE_POSITION_PARAM_Z)
    );
    addTypeSuggestion(LoopbackCommAdapterMessages.RESET_PRECISE_POSITION);
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_STATE,
        new ParameterSuggestion(
            LoopbackCommAdapterMessages.SET_STATE_PARAM_STATE,
            Set.of(
                Vehicle.State.UNKNOWN.name(),
                Vehicle.State.UNAVAILABLE.name(),
                Vehicle.State.ERROR.name(),
                Vehicle.State.IDLE.name(),
                Vehicle.State.EXECUTING.name(),
                Vehicle.State.CHARGING.name()
            )
        )
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_PAUSED,
        new ParameterSuggestion(
            LoopbackCommAdapterMessages.SET_PAUSED_PARAM_PAUSED,
            Set.of("true", "false")
        )
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_PROPERTY,
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_PROPERTY_PARAM_KEY),
        new ParameterSuggestion(LoopbackCommAdapterMessages.SET_PROPERTY_PARAM_VALUE)
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.RESET_PROPERTY,
        new ParameterSuggestion(LoopbackCommAdapterMessages.RESET_PROPERTY_PARAM_KEY)
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.SET_SINGLE_STEP_MODE_ENABLED,
        new ParameterSuggestion(
            LoopbackCommAdapterMessages.SET_SINGLE_STEP_MODE_ENABLED_PARAM_ENABLED,
            Set.of("true", "false")
        )
    );
    addTypeSuggestion(
        LoopbackCommAdapterMessages.TRIGGER_SINGLE_STEP
    );
  }

  @Override
  @Nonnull
  public Set<String> getTypeSuggestions() {
    return Set.copyOf(typeSuggestions);
  }

  @Override
  @Nonnull
  public Map<String, Set<String>> getParameterSuggestionsFor(
      @Nonnull
      String type
  ) {
    return parameterSuggestions.getOrDefault(requireNonNull(type, "type"), Map.of());
  }

  private void addTypeSuggestion(
      String typeSuggestion,
      ParameterSuggestion... parameterSuggestions
  ) {
    typeSuggestions.add(typeSuggestion);
    this.parameterSuggestions.put(
        typeSuggestion,
        Stream.of(parameterSuggestions)
            .collect(
                Collectors.toMap(
                    ParameterSuggestion::key,
                    ParameterSuggestion::values
                )
            )
    );
  }

  /**
   * A suggestion for a single parameter consisting of the parameter's key and suggestions for its
   * value.
   *
   * @param key The parameter's key.
   * @param values A set of suggested values
   */
  private record ParameterSuggestion(String key, Set<String> values) {

    /**
     * Creates a new instance.
     *
     * @param key The parameter's key.
     */
    ParameterSuggestion(String key) {
      this(key, Set.of());
    }
  }
}
