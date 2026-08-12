// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_CUSTOM_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;

/**
 * Unit tests for {@link PropertyActionsFilter}.
 */
public class PropertyActionsFilterTest {

  private Point point;

  @BeforeEach
  public void setup() {
    point = new Point("point-001");
  }

  @Test
  public void shouldAllowAction() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "duck | beep");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        makeFilter("duck", "beep"),
        s -> true,
        s -> true,
        EnumSet.allOf(ActionTrigger.class)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(1));
  }

  @Test
  public void actionNeedsToPassAllFiltersToBeAllowed() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "duck");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        makeFilter("duck", "beep", "third"),
        makeFilter("duck", "first"),
        makeFilter("duck", "something else", "and another"),
        EnumSet.allOf(ActionTrigger.class)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(1));
  }

  @Test
  public void actionNotAllowedIfOneFilterFails() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "duck");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        makeFilter("this-one-will-fail"),
        s -> true,
        s -> true,
        EnumSet.allOf(ActionTrigger.class)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(0));
  }

  @Test
  public void emptyFilterAllowsAllActions() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "duck");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        makeFilter(),
        makeFilter(),
        makeFilter(),
        EnumSet.allOf(ActionTrigger.class)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(1));
  }

  @Test
  public void emptyStringFilterAllowsNoActions() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "duck");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        makeFilter(""),
        s -> true,
        s -> true,
        EnumSet.allOf(ActionTrigger.class)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(0));
  }

  @Test
  public void actionWithNoTagsHasDefaultTag() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        makeFilter("default"),
        makeFilter("default"),
        makeFilter("default"),
        EnumSet.allOf(ActionTrigger.class)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(1));
  }

  @Test
  public void actionTriggerFilterAllowsAllActions() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.tags", "beep");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        s -> true,
        s -> true,
        s -> true,
        EnumSet.allOf(ActionTrigger.class)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(2));
  }

  @Test
  public void actionTriggerMustMatchAtLeastOneTriggerFilter() {
    point = point
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.when", "PASSING")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.when", "ORDER_START")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03", "quack")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03.when", "ORDER_END");

    PropertyActionsFilter filter = new PropertyActionsFilter(
        s -> true,
        s -> true,
        s -> true,
        EnumSet.of(ActionTrigger.ORDER_START, ActionTrigger.ORDER_END)
    );

    List<PropertyAction> actions = ActionsMapping.mapPropertyActions(point).stream()
        .filter(filter)
        .collect(Collectors.toList());

    assertThat(actions.size(), is(2));
    assertThat(actions.get(0).getActionType(), is("beep"));
    assertThat(actions.get(1).getActionType(), is("quack"));

  }

  private Predicate<String> makeFilter(String... tags) {
    Map<String, String> properties = new HashMap<>();
    if (tags.length > 0) {
      properties.put(
          PROPKEY_EXECUTABLE_ACTIONS_TAGS,
          Arrays.stream(tags).collect(Collectors.joining(" | "))
      );
    }
    return new ExecutableActionsTagsPredicate(properties);

  }
}
