// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_CUSTOM_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_CUSTOM_DEST_ACTION_PREFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Functions to map a movement command operation or custom actions to vda5050 actions.
 */
public class ActionsMapping {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ActionsMapping.class);
  /**
   * A regex matching action property keys.
   */
  private static final Pattern ACTION_PROP_INDEX_PATTERN
      = Pattern.compile(PROPKEY_CUSTOM_ACTION_PREFIX + "\\.([^\\.]+)");
  /**
   * A regex matching floating point action parameters.
   */
  private static final Pattern ACTION_PARAM_FLOAT_PATTERN = Pattern.compile("float:(.+)");
  /**
   * A regex matching integer action parameters.
   */
  private static final Pattern ACTION_PARAM_INTEGER_PATTERN = Pattern.compile("integer:(.+)");
  /**
   * A regex matching boolean action parameters.
   */
  private static final Pattern ACTION_PARAM_BOOLEAN_PATTERN = Pattern.compile("boolean:(.+)");
  /**
   * A regex matching string action parameters.
   */
  private static final Pattern ACTION_PARAM_STRING_PATTERN = Pattern.compile("string:(.*)");

  /**
   * Prevents unwanted instantiation.
   */
  private ActionsMapping() {
  }

  public static List<PropertyAction> mapPropertyActions(TCSResource<?> res) {
    requireNonNull(res, "res");
    // XXX AtomicInteger is only used as a counter object, not for concurrency.
    // XXX We may want to use something more efficient.
    AtomicInteger actionCounter = new AtomicInteger(0);
    return res.getProperties().entrySet().stream()
        .map(property -> extractActionIndex(property.getKey()))
        .filter(actionIndex -> actionIndex != null)
        .sorted()
        .map(
            actionIndex -> createPropertyAction(
                actionIndex,
                actionCounter.getAndIncrement(),
                res
            )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Action fromPropertyAction(
      Vehicle vehicle,
      PropertyAction propertyAction
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(propertyAction, "propertyAction");

    LOG.debug("Mapping action from vehicle properties: {}", vehicle.getName());
    return createAction(propertyAction, vehicle);
  }

  public static List<PropertyAction> mapPropertyActions(MovementCommand command) {
    requireNonNull(command, "command");

    if (!command.isFinalMovement()) {
      return List.of();
    }

    // XXX AtomicInteger is only used as a counter object, not for concurrency.
    // XXX We may want to use something more efficient.
    AtomicInteger actionCounter = new AtomicInteger(0);
    return command.getProperties().entrySet().stream()
        .map(property -> extractActionIndex(property.getKey()))
        .filter(actionIndex -> actionIndex != null)
        .sorted()
        .map(
            actionIndex -> createPropertyAction(
                actionIndex,
                actionCounter.getAndIncrement(),
                command
            )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Maps a movement command to a {@link PropertyAction}.
   *
   * @param vehicle The vehicle that is processing the movement command.
   * @param command The movement command to map.
   * @param destinationLocation The destination location of this command.
   * @param destinationType The destination location type.
   * @return The mapped {@link PropertyAction}.
   */
  public static Optional<PropertyAction> fromMovementCommand(
      Vehicle vehicle,
      MovementCommand command,
      Location destinationLocation,
      LocationType destinationType
  ) {
    requireNonNull(vehicle, "vehicle");
    requireNonNull(command, "command");
    requireNonNull(destinationLocation, "destinationLocation");
    requireNonNull(destinationType, "destinationType");

    if (command.hasEmptyOperation()) {
      return Optional.empty();
    }

    String actionId = "Order_destination_action_" + command.getStep().getRouteIndex();

    return Optional.of(
        mapMovementCommandAction(command, destinationLocation, destinationType, actionId)
    );
  }

  private static PropertyAction mapMovementCommandAction(
      MovementCommand command,
      Location location,
      LocationType locationType,
      String actionId
  ) {
    LOG.debug("Mapping action for movement command: {}", command);
    return new PropertyAction(
        command.getOperation(),
        actionId,
        extractDestBlockingType(null, command.getProperties())
            .or(() -> extractDestBlockingType(command.getOperation(), location.getProperties()))
            .or(() -> extractDestBlockingType(command.getOperation(), locationType.getProperties()))
            .orElse(BlockingType.NONE),
        extractDestActionParameters(command, location, locationType),
        EnumSet.allOf(ActionTrigger.class),
        new HashSet<>()
    );
  }

  private static List<ActionParameter> extractDestActionParameters(
      MovementCommand command,
      Location location,
      LocationType locationType
  ) {
    Map<String, String> combinedParameters = new HashMap<>();
    combinedParameters.putAll(
        extractDestActionParameters(command.getOperation(), locationType.getProperties())
    );
    combinedParameters.putAll(
        extractDestActionParameters(command.getOperation(), location.getProperties())
    );
    combinedParameters.putAll(extractDestActionParameters(null, command.getProperties()));

    return combinedParameters.entrySet().stream()
        .map(entry -> new ActionParameter(entry.getKey(), parseParameter(entry.getValue())))
        .collect(Collectors.toList());
  }

  private static Map<String, String> extractDestActionParameters(
      @Nullable
      String actionType,
      @Nonnull
      Map<String, String> properties
  ) {
    String patternStr = Optional.ofNullable(actionType)
        .map(at -> PROPKEY_CUSTOM_DEST_ACTION_PREFIX + "\\." + at + "\\.parameter\\.(.+)")
        .orElse(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + "\\.parameter\\.(.+)");

    Pattern pattern = Pattern.compile(patternStr);
    return properties.entrySet().stream()
        .map((entry) -> {
          return new Object() {
            private Map.Entry<String, String> property = entry;
            private Matcher matcher = pattern.matcher(entry.getKey());
          };
        })
        .filter(element -> element.matcher.matches())
        .collect(
            Collectors.toMap(
                element -> element.matcher.group(1),
                element -> element.property.getValue()
            )
        );
  }

  private static Optional<BlockingType> extractDestBlockingType(
      @Nullable
      String actionType,
      @Nonnull
      Map<String, String> properties
  ) {
    String blockingTypePropKey = Optional.ofNullable(actionType)
        .map(at -> PROPKEY_CUSTOM_DEST_ACTION_PREFIX + "." + at + ".blockingType")
        .orElse(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".blockingType");

    try {
      return Optional.ofNullable(properties.get(blockingTypePropKey))
          .map(blockingTypeStr -> BlockingType.valueOf(blockingTypeStr));
    }
    catch (IllegalArgumentException e) {
      LOG.warn("Could not parse blocking type for action '{}': {}.", actionType, e.getMessage());
      return Optional.empty();
    }
  }

  private static Optional<BlockingType> extractBlockingType(
      @Nonnull
      String actionIndex,
      @Nonnull
      Map<String, String> properties
  ) {
    String blockingTypePropKey = PROPKEY_CUSTOM_ACTION_PREFIX + "." + actionIndex + ".blockingType";

    try {
      return Optional.ofNullable(properties.get(blockingTypePropKey))
          .map(blockingTypeStr -> BlockingType.valueOf(blockingTypeStr));
    }
    catch (IllegalArgumentException e) {
      LOG.warn("Could not parse blocking type for action '{}': {}.", actionIndex, e.getMessage());
      return Optional.empty();
    }
  }

  @Nullable
  private static List<ActionParameter> extractActionParameters(
      String actionIndex,
      Map<String, String> properties
  ) {
    Pattern pattern = Pattern.compile(
        PROPKEY_CUSTOM_ACTION_PREFIX + "\\." + actionIndex + "\\.parameter\\.(.+)"
    );
    // Iterate over all property keys matching the pattern.
    return properties.entrySet().stream()
        .map((entry) -> {
          return new Object() {
            private Map.Entry<String, String> property = entry;
            private Matcher matcher = pattern.matcher(entry.getKey());
          };
        })
        .filter(element -> element.matcher.matches())
        .map(
            element -> new ActionParameter(
                element.matcher.group(1),
                parseParameter(element.property.getValue())
            )
        )
        .collect(Collectors.toList());
  }

  @Nullable
  private static Object parseParameter(
      @Nullable
      String value
  ) {
    if (value == null) {
      return null;
    }

    try {
      return tryParseParameterAsFloat(value)
          .or(() -> tryParseParameterAsInteger(value))
          .or(() -> tryParseParameterAsBoolean(value))
          .or(() -> tryParseParameterAsString(value))
          .orElse(value);
    }
    catch (IllegalArgumentException e) {
      LOG.warn("Exception parsing parameter value '{}', falling back to null value", e);
      return null;
    }
  }

  private static Optional<Object> tryParseParameterAsFloat(
      @Nonnull
      String value
  )
      throws IllegalArgumentException {
    Matcher matcher = ACTION_PARAM_FLOAT_PATTERN.matcher(value);
    if (matcher.matches()) {
      return Optional.of(Double.valueOf(matcher.group(1)));
    }
    else {
      return Optional.empty();
    }
  }

  private static Optional<Object> tryParseParameterAsInteger(
      @Nonnull
      String value
  )
      throws IllegalArgumentException {
    Matcher matcher = ACTION_PARAM_INTEGER_PATTERN.matcher(value);
    if (matcher.matches()) {
      return Optional.of(Long.valueOf(matcher.group(1)));
    }
    else {
      return Optional.empty();
    }
  }

  private static Optional<Object> tryParseParameterAsBoolean(
      @Nonnull
      String value
  ) {
    Matcher matcher = ACTION_PARAM_BOOLEAN_PATTERN.matcher(value);
    if (matcher.matches()) {
      return Optional.of(Boolean.valueOf(matcher.group(1)));
    }
    else {
      return Optional.empty();
    }
  }

  private static Optional<Object> tryParseParameterAsString(
      @Nonnull
      String value
  ) {
    Matcher matcher = ACTION_PARAM_STRING_PATTERN.matcher(value);
    if (matcher.matches()) {
      return Optional.of(matcher.group(1));
    }
    else {
      return Optional.empty();
    }
  }

  private static EnumSet<ActionTrigger> extractTrigger(
      String actionIndex,
      Map<String, String> properties
  ) {
    String triggerKey = PROPKEY_CUSTOM_ACTION_PREFIX + "." + actionIndex + ".when";
    String triggerValue = properties.get(triggerKey);

    return triggerValue == null
        ? EnumSet.allOf(ActionTrigger.class)
        : parseTrigger(triggerValue, triggerKey);
  }

  private static EnumSet<ActionTrigger> parseTrigger(
      String value,
      String key
  ) {
    EnumSet<ActionTrigger> trigger = EnumSet.noneOf(ActionTrigger.class);

    for (String triggerString : value.split("\\|")) {
      try {
        trigger.add(ActionTrigger.valueOf(triggerString.strip()));
      }
      catch (IllegalArgumentException e) {
        LOG.warn(
            "Could not parse execution trigger for action '{}': {}. ",
            key,
            e.getMessage()
        );
      }
    }
    return trigger;
  }

  private static Set<String> extractTags(
      String actionIndex,
      Map<String, String> properties
  ) {
    String tagsKey = PROPKEY_CUSTOM_ACTION_PREFIX + "." + actionIndex + ".tags";
    String tagsValue = properties.getOrDefault(tagsKey, "default");

    return Arrays.stream(tagsValue.split("\\|"))
        .map(str -> str.strip())
        .collect(Collectors.toSet());
  }

  private static String extractActionIndex(String propertyKey) {
    Matcher matcher = ACTION_PROP_INDEX_PATTERN.matcher(propertyKey);
    return matcher.matches() ? matcher.group(1) : null;
  }

  private static PropertyAction createPropertyAction(
      String actionIndex,
      int actionNumber,
      MovementCommand command
  ) {
    LOG.debug("Start mapping actions for movement command: {}", command);
    return new PropertyAction(
        command.getProperties().get(PROPKEY_CUSTOM_ACTION_PREFIX + "." + actionIndex),
        "Order_destination_custom_action_" + actionNumber,
        extractBlockingType(actionIndex, command.getProperties()).orElse(BlockingType.NONE),
        extractActionParameters(actionIndex, command.getProperties()),
        EnumSet.allOf(ActionTrigger.class),
        Set.of()
    );
  }

  private static PropertyAction createPropertyAction(
      String actionIndex,
      int actionNumber,
      TCSResource<?> res
  ) {
    LOG.debug("Start mapping actions for resource '{}'", res.getName());
    return new PropertyAction(
        res.getProperty(PROPKEY_CUSTOM_ACTION_PREFIX + "." + actionIndex),
        res.getName() + "_action_" + actionNumber,
        extractBlockingType(actionIndex, res.getProperties()).orElse(BlockingType.NONE),
        extractActionParameters(actionIndex, res.getProperties()),
        extractTrigger(actionIndex, res.getProperties()),
        extractTags(actionIndex, res.getProperties())
    );
  }

  private static Action createAction(PropertyAction propertyAction, Vehicle vehicle) {
    Action action = new Action(
        propertyAction.getActionType(),
        propertyAction.getActionId(),
        extractBlockingType(propertyAction.getActionType(), vehicle.getProperties())
            .orElse(propertyAction.getBlockingType())
    );
    action.setActionParameters(
        propertyAction.getActionParameters().isEmpty() ? null : propertyAction.getActionParameters()
    );
    return action;
  }
}
