// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.operationsdesk;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterMessages;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;
import org.opentcs.components.plantoverview.VehicleCommAdapterMessageSuggestions;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;

/**
 * The VDA5050 suggestions for {@link VehicleCommAdapterMessage}s.
 *
 * Note that we keep a set of suggestions per VDA5050 version to make removal of suggestions for
 * older versions easier.
 */
public class VDA5050VehicleCommAdapterMessageSuggestions
    implements
      VehicleCommAdapterMessageSuggestions {

  private final Set<String> typeSuggestions = new HashSet<>();
  private final Map<String, Map<String, Set<String>>> parameterSuggestions = new HashMap<>();

  /**
   * Creates a new instance.
   */
  public VDA5050VehicleCommAdapterMessageSuggestions() {
    addTypeSuggestion(
        CommAdapterMessages.SEND_INSTANT_ACTION_TYPE,
        new ParameterSuggestion(CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_TYPE),
        new ParameterSuggestion(CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_ID),
        new ParameterSuggestion(CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION),
        new ParameterSuggestion(
            CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE,
            Set.of(BlockingType.NONE.name(), BlockingType.SOFT.name(), BlockingType.HARD.name())
        ),
        new ParameterSuggestion(
            CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX
                + "<ACTION_PARAM_KEY>"
        )
    );
    addTypeSuggestion(
        CommAdapterMessages.SEND_ORDER_TYPE,
        new ParameterSuggestion(CommAdapterMessages.SEND_ORDER_PARAM_ORDER_ID),
        new ParameterSuggestion(CommAdapterMessages.SEND_ORDER_PARAM_ORDER_UPDATE_ID),
        new ParameterSuggestion(CommAdapterMessages.SEND_ORDER_PARAM_SOURCE_NODE),
        new ParameterSuggestion(CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE),
        new ParameterSuggestion(
            CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION
        ),
        new ParameterSuggestion(
            CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE,
            Set.of(BlockingType.NONE.name(), BlockingType.SOFT.name(), BlockingType.HARD.name())
        ),
        new ParameterSuggestion(CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID),
        new ParameterSuggestion(CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE),
        new ParameterSuggestion(
            CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX
                + "<ACTION_PARAM_KEY>"
        ),
        new ParameterSuggestion(CommAdapterMessages.SEND_ORDER_PARAM_EDGE)
    );
    addTypeSuggestion(CommAdapterMessages.EXTEND_DEVIATION_ONCE_TYPE);
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
