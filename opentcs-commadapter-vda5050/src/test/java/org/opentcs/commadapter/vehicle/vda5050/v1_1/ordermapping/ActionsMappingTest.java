// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_CUSTOM_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_CUSTOM_DEST_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping.ActionTrigger.ORDER_END;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping.ActionTrigger.ORDER_START;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping.ActionTrigger.PASSING;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Unit tests for {@link ActionsMapping}.
 */
public class ActionsMappingTest {

  private Point point;

  @BeforeEach
  public void setup() {
    point = new Point("point-001");
  }

  @Test
  public void createActions() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameter.x", "234");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameter.y", "567");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    PropertyAction action = result.get(0);
    assertThat(action.getActionType(), is("duck"));
    assertThat(action.getBlockingType(), is(BlockingType.SOFT));
    assertThat(action.getActionParameters(), hasSize(2));

    org.assertj.core.api.Assertions.assertThat(action.getActionParameters())
        .hasSize(2)
        .extracting(ActionParameter::getKey, ActionParameter::getValue)
        .contains(tuple("x", "234"), tuple("y", "567"));
  }

  @Test
  public void createActionsFromMovementCommand() {
    LocationType locationType = new LocationType("locType");
    Location location = new Location("destLoc", locationType.getReference());

    MovementCommand command = createMovementCommand("destOp", location)
        .withFinalMovement(true)
        .withProperties(
            Map.of(
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01.blockingType", "SOFT",
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "123",
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.y", "456"
            )
        );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(command);

    assertThat(result, hasSize(1));

    PropertyAction action = result.get(0);
    assertThat(action.getActionType(), is("some-action"));
    assertThat(action.getBlockingType(), is(BlockingType.SOFT));
    assertThat(action.getActionParameters(), hasSize(2));

    org.assertj.core.api.Assertions.assertThat(action.getActionParameters())
        .hasSize(2)
        .extracting(ActionParameter::getKey, ActionParameter::getValue)
        .contains(tuple("x", "123"), tuple("y", "456"));
  }

  @Test
  public void createNoActionsIfNotFinalMovement() {
    LocationType locationType = new LocationType("locType");
    Location location = new Location("destLoc", locationType.getReference());

    MovementCommand command = createMovementCommand("destOp", location)
        .withFinalMovement(false)
        .withProperties(
            Map.of(
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01.blockingType", "SOFT",
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "123",
                PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.y", "456"
            )
        );
    List<PropertyAction> result = ActionsMapping.mapPropertyActions(command);

    assertThat(result, hasSize(0));
  }

  @Test
  public void parseFloatingPointActionParameterPositive() {
    point = point.withProperties(
        Map.of(
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "float:3.14"
        )
    );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    List<ActionParameter> actionParameters = result.get(0).getActionParameters();

    assertThat(actionParameters, hasSize(1));

    assertThat(actionParameters.get(0).getKey(), is("x"));
    assertThat(actionParameters.get(0).getValue(), isA(Double.class));
    assertThat((Double) actionParameters.get(0).getValue(), is(Matchers.closeTo(3.14, 0.0001)));
  }

  @Test
  public void parseFloatingPointActionParameterNegative() {
    point = point.withProperties(
        Map.of(
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "float:-3.14"
        )
    );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    List<ActionParameter> actionParameters = result.get(0).getActionParameters();

    assertThat(actionParameters, hasSize(1));

    assertThat(actionParameters.get(0).getKey(), is("x"));
    assertThat(actionParameters.get(0).getValue(), isA(Double.class));
    assertThat((Double) actionParameters.get(0).getValue(), is(Matchers.closeTo(-3.14, 0.0001)));
  }

  @Test
  public void parseIntegerActionParameterPositive() {
    point = point.withProperties(
        Map.of(
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "integer:1234"
        )
    );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    List<ActionParameter> actionParameters = result.get(0).getActionParameters();

    assertThat(actionParameters, hasSize(1));
    assertThat(actionParameters.get(0).getKey(), is("x"));
    assertThat(actionParameters.get(0).getValue(), isA(Long.class));
    assertThat((Long) actionParameters.get(0).getValue(), is(1234L));
  }

  @Test
  public void parseIntegerActionParameterNegative() {
    point = point.withProperties(
        Map.of(
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "integer:-1234"
        )
    );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    List<ActionParameter> actionParameters = result.get(0).getActionParameters();

    assertThat(actionParameters, hasSize(1));
    assertThat(actionParameters.get(0).getKey(), is("x"));
    assertThat(actionParameters.get(0).getValue(), isA(Long.class));
    assertThat((Long) actionParameters.get(0).getValue(), is(-1234L));
  }

  @Test
  public void parseBooleanActionParameterTrue() {
    point = point.withProperties(
        Map.of(
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "boolean:true"
        )
    );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    List<ActionParameter> actionParameters = result.get(0).getActionParameters();

    assertThat(actionParameters, hasSize(1));
    assertThat(actionParameters.get(0).getKey(), is("x"));
    assertThat(actionParameters.get(0).getValue(), isA(Boolean.class));
    assertThat((Boolean) actionParameters.get(0).getValue(), is(Boolean.TRUE));
  }

  @Test
  public void parseBooleanActionParameterFalse() {
    point = point.withProperties(
        Map.of(
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "boolean:something"
        )
    );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    List<ActionParameter> actionParameters = result.get(0).getActionParameters();

    assertThat(actionParameters, hasSize(1));
    assertThat(actionParameters.get(0).getKey(), is("x"));
    assertThat(actionParameters.get(0).getValue(), isA(Boolean.class));
    assertThat((Boolean) actionParameters.get(0).getValue(), is(Boolean.FALSE));
  }

  @Test
  public void parseStringActionParameter() {
    point = point.withProperties(
        Map.of(
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "some-action",
            PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "string:float:3.14"
        )
    );

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    List<ActionParameter> actionParameters = result.get(0).getActionParameters();

    assertThat(actionParameters, hasSize(1));
    assertThat(actionParameters.get(0).getKey(), is("x"));
    assertThat(actionParameters.get(0).getValue(), isA(String.class));
    assertThat((String) actionParameters.get(0).getValue(), is("float:3.14"));
  }

  @Test
  public void defaultBlockingTypeIsNone() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getBlockingType(), is(BlockingType.NONE));
  }

  @Test
  public void fallBackToNoneForInvalidBlockingTypes() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT_something_invalid");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getBlockingType(), is(BlockingType.NONE));
  }

  @Test
  public void actionParametersAreOptionalWithEmpty() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getActionParameters(), is(empty()));
  }

  @Test
  public void ignoreActionsWithoutActionType() {
    Map<String, String> properties = new HashMap<>();
    // Action with no action type is invalid and ignored
    //properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.blockingType", "SOFT");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.x", "x");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameter.y", "y");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, is(empty()));
  }

  @Test
  public void orderActionsLexicographically() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".cd", "duck");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "beep");

    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(2));
    assertThat(result.get(0).getActionType(), is("beep"));
    assertThat(result.get(1).getActionType(), is("duck"));
  }

  @Test
  public void createActionIdsFromResourceNameAndCounter() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".c", "beep");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(2));
    assertThat(result.get(0).getActionId(), is(point.getName() + "_action_0"));
    assertThat(result.get(1).getActionId(), is(point.getName() + "_action_1"));
  }

  @Test
  public void parseTrigger() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.when", "PASSING");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "beep");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.when", "PASSING | ORDER_START");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".03", "bong");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".03.when", "PASSING | ORDER_START | ORDER_END");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".04", "quack");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(4));

    assertThat(result.get(0).getTrigger(), contains(PASSING));
    assertThat(
        result.get(1).getTrigger(), containsInAnyOrder(
            ORDER_START,
            PASSING
        )
    );
    assertThat(
        result.get(2).getTrigger(), containsInAnyOrder(
            ORDER_END,
            ORDER_START,
            PASSING
        )
    );
    assertThat(
        result.get(3).getTrigger(), containsInAnyOrder(
            ORDER_END,
            ORDER_START,
            PASSING
        )
    );
  }

  @Test
  public void parseTags() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck");
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "duck | beep | quack");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    assertThat(result.get(0).getTags(), hasSize(3));
    assertThat(result.get(0).getTags(), containsInAnyOrder("duck", "beep", "quack"));
  }

  @Test
  public void useDefaultTagIfNoneAreSet() {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck");
    point = point.withProperties(properties);

    List<PropertyAction> result = ActionsMapping.mapPropertyActions(point);

    assertThat(result, hasSize(1));

    assertThat(result.get(0).getTags(), hasSize(1));
    assertThat(result.get(0).getTags(), containsInAnyOrder("default"));

  }

  @Test
  public void useBlockingTypeFromVehicleIfSetThere() {
    PropertyAction propAction = new PropertyAction(
        "actionType",
        "actionId",
        BlockingType.NONE,
        List.of(),
        EnumSet.allOf(ActionTrigger.class),
        new HashSet<>()
    );
    Vehicle vehicle = new Vehicle("Vehicle-0001")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".actionType.blockingType", "HARD");

    Action result = ActionsMapping.fromPropertyAction(vehicle, propAction);

    assertThat(result.getBlockingType(), is(BlockingType.HARD));
  }

  @Test
  public void shouldMapDestinationAction() {
    Vehicle vehicle = new Vehicle("vehicle");

    LocationType locationType = new LocationType("locType");
    Location location = new Location("destLoc", locationType.getReference());

    MovementCommand command = createMovementCommand("destOp", location)
        .withProperties(
            Map.of(
                PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".blockingType", "HARD",
                PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".parameter.source", "command"
            )
        );

    Optional<PropertyAction> action
        = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    assertThat(action.get().getBlockingType(), is(BlockingType.HARD));
    org.assertj.core.api.Assertions.assertThat(action.get().getActionParameters())
        .hasSize(1)
        .extracting(ActionParameter::getKey, ActionParameter::getValue)
        .contains(tuple("source", "command"));
  }

  @Test
  public void useDefaultBlockingTypeForDestinationAction() {
    Vehicle vehicle = new Vehicle("vehicle");
    LocationType locationType = new LocationType("locType");
    Location location = new Location("destLoc", locationType.getReference());
    MovementCommand command = createMovementCommand("destOp", location);

    Optional<PropertyAction> action
        = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    assertThat(action.get().getBlockingType(), is(BlockingType.NONE));
  }

  @Test
  public void overrideDestinationActionBlockingType() {
    Vehicle vehicle = new Vehicle("vehicle");

    // Location type overrides default
    LocationType locationType = new LocationType("locType")
        .withProperty(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".destOp.blockingType", "SOFT");
    Location location = new Location("destLoc", locationType.getReference());
    MovementCommand command = createMovementCommand("destOp", location);

    Optional<PropertyAction> action
        = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    assertThat(action.get().getBlockingType(), is(BlockingType.SOFT));

    // Location overrides location type
    location = new Location("destLoc", locationType.getReference())
        .withProperty(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".destOp.blockingType", "NONE");
    command = createMovementCommand("destOp", location);

    action = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    assertThat(action.get().getBlockingType(), is(BlockingType.NONE));

    // Transport order destination overrides location
    command = createMovementCommand("destOp", location)
        .withProperties(Map.of(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".blockingType", "HARD"));

    action = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    assertThat(action.get().getBlockingType(), is(BlockingType.HARD));
  }

  @Test
  public void emptyListAsDefaultForDestinationActionParameters() {
    Vehicle vehicle = new Vehicle("vehicle");
    LocationType locationType = new LocationType("locType");
    Location location = new Location("destLoc", locationType.getReference());
    MovementCommand command = createMovementCommand("destOp", location);

    Optional<PropertyAction> action
        = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    assertThat(action.get().getActionParameters(), is(empty()));
  }

  @Test
  public void overrideDestinationActionParameters() {
    Vehicle vehicle = new Vehicle("vehicle");

    // Location type appends/overrides default
    LocationType locationType = new LocationType("locType")
        .withProperty(
            PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".destOp.parameter.locationType",
            "value-from-location-type"
        )
        .withProperty(
            PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".destOp.parameter.override",
            "value-from-location-type"
        );
    Location location = new Location("destLoc", locationType.getReference());
    MovementCommand command = createMovementCommand("destOp", location);

    Optional<PropertyAction> action
        = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    org.assertj.core.api.Assertions.assertThat(action.get().getActionParameters())
        .hasSize(2)
        .extracting(ActionParameter::getKey, ActionParameter::getValue)
        .contains(
            tuple("locationType", "value-from-location-type"),
            tuple("override", "value-from-location-type")
        );

    // Location appends/overrides location type
    location = new Location("destLoc", locationType.getReference())
        .withProperty(
            PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".destOp.parameter.location",
            "value-from-location"
        )
        .withProperty(
            PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".destOp.parameter.override",
            "value-from-location"
        );
    command = createMovementCommand("destOp", location);

    action = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    org.assertj.core.api.Assertions.assertThat(action.get().getActionParameters())
        .hasSize(3)
        .extracting(ActionParameter::getKey, ActionParameter::getValue)
        .contains(
            tuple("locationType", "value-from-location-type"),
            tuple("location", "value-from-location"),
            tuple("override", "value-from-location")
        );

    // Transport order destination appends/overrides location
    command = createMovementCommand("destOp", location)
        .withProperties(
            Map.of(
                PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".parameter.movementCommand",
                "value-from-movement-command",
                PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".parameter.override",
                "value-from-movement-command"
            )
        );

    action = ActionsMapping.fromMovementCommand(vehicle, command, location, locationType);

    assertTrue(action.isPresent());
    org.assertj.core.api.Assertions.assertThat(action.get().getActionParameters())
        .hasSize(4)
        .extracting(ActionParameter::getKey, ActionParameter::getValue)
        .contains(
            tuple("locationType", "value-from-location-type"),
            tuple("location", "value-from-location"),
            tuple("movementCommand", "value-from-movement-command"),
            tuple("override", "value-from-movement-command")
        );
  }

  private MovementCommand createMovementCommand(String operation, Location opLocation) {
    requireNonNull(operation, "operation");
    requireNonNull(opLocation, "opLocation");
    Point src = new Point("p1");
    Point dst = new Point("p2");

    Route.Step dummyStep = new Route.Step(
        new Path("path1", src.getReference(), dst.getReference()),
        src,
        dst,
        Vehicle.Orientation.FORWARD,
        0,
        1
    );

    return new MovementCommand(
        new TransportOrder("1", List.of()),
        new DriveOrder("drive-order", new DriveOrder.Destination(dst.getReference())),
        dummyStep,
        operation,
        opLocation,
        false,
        null,
        dst,
        "NOP",
        Map.of()
    );
  }
}
