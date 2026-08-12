// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
 * Tests for object properties-related utility methods.
 */
public class PropertyExtractionsTest {

  @Test
  public void returnPropertyValueIfPresent() {
    Point point = new Point("some-point").withProperty("some-property", "some-value");

    Optional<String> result;

    result = PropertyExtractions.getProperty("some-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is("some-value"));

    point = point.withProperty("some-other-property", "some-other-value");

    result = PropertyExtractions.getProperty("some-other-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is("some-other-value"));
  }

  @Test
  public void preferObjectsInGivenOrder() {
    Point point1 = new Point("point1").withProperty("some-property", "value1");
    Point point2 = new Point("point2").withProperty("some-property", "value2");

    Optional<String> result;

    result = PropertyExtractions.getProperty("some-property", point1, point2);

    assertTrue(result.isPresent());
    assertThat(result.get(), is("value1"));

    result = PropertyExtractions.getProperty("some-property", point2, point1);

    assertTrue(result.isPresent());
    assertThat(result.get(), is("value2"));
  }

  @Test
  public void returnEmptyIfPropertyNotPresent() {
    Point point1 = new Point("point1").withProperty("some-property", "value1");
    Point point2 = new Point("point2").withProperty("some-property", "value2");

    Optional<String> result;

    result = PropertyExtractions.getProperty("some-other-property", point1, point2);

    assertFalse(result.isPresent());

    result = PropertyExtractions.getProperty("some-other-property", point2, point1);

    assertFalse(result.isPresent());
  }

  @Test
  public void returnPropertyValueIfPresentInteger() {
    Point point = new Point("some-point").withProperty("some-property", "1234");

    Optional<Integer> result;

    result = PropertyExtractions.getPropertyInteger("some-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(1234));

    point = point.withProperty("some-other-property", "500");

    result = PropertyExtractions.getPropertyInteger("some-other-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(500));
  }

  @Test
  public void preferObjectsInGivenOrderInteger() {
    Point point1 = new Point("point1").withProperty("some-property", "42");
    Point point2 = new Point("point2").withProperty("some-property", "23");

    Optional<Integer> result;

    result = PropertyExtractions.getPropertyInteger("some-property", point1, point2);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(42));

    result = PropertyExtractions.getPropertyInteger("some-property", point2, point1);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(23));
  }

  @Test
  public void returnPropertyValueIfPresentDouble() {
    Point point = new Point("some-point").withProperty("some-property", "3.14");

    Optional<Double> result;

    result = PropertyExtractions.getPropertyDouble("some-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(3.14));

    point = point.withProperty("some-other-property", "0.5");

    result = PropertyExtractions.getPropertyDouble("some-other-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(0.5));
  }

  @Test
  public void preferObjectsInGivenOrderDouble() {
    Point point1 = new Point("point1").withProperty("some-property", "42.0");
    Point point2 = new Point("point2").withProperty("some-property", "23.45");

    Optional<Double> result;

    result = PropertyExtractions.getPropertyDouble("some-property", point1, point2);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(42.0));

    result = PropertyExtractions.getPropertyDouble("some-property", point2, point1);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(23.45));
  }

  @Test
  public void returnEmptyIfPropertyNotPresentDouble() {
    Point point1 = new Point("point1").withProperty("some-property", "12.0");
    Point point2 = new Point("point2").withProperty("some-property", "45.0");

    Optional<Double> result;

    result = PropertyExtractions.getPropertyDouble("some-other-property", point1, point2);

    assertFalse(result.isPresent());

    result = PropertyExtractions.getPropertyDouble("some-other-property", point2, point1);

    assertFalse(result.isPresent());
  }

  @Test
  public void returnPropertyValueIfPresentLong() {
    Point point = new Point("some-point").withProperty("some-property", "42");

    Optional<Long> result;

    result = PropertyExtractions.getPropertyLong("some-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(42L));

    point = point.withProperty("some-other-property", "23");

    result = PropertyExtractions.getPropertyLong("some-other-property", point);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(23L));
  }

  @Test
  public void preferObjectsInGivenOrderLong() {
    Point point1 = new Point("point1").withProperty("some-property", "42");
    Point point2 = new Point("point2").withProperty("some-property", "23");

    Optional<Long> result;

    result = PropertyExtractions.getPropertyLong("some-property", point1, point2);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(42L));

    result = PropertyExtractions.getPropertyLong("some-property", point2, point1);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(23L));
  }

  @Test
  public void returnPropertyValueIfPresentInMovementCommand() {
    MovementCommand command = createMovementCommand(null, Map.of("some-property", "value1"));

    Optional<String> result = PropertyExtractions.getProperty("some-property", command);

    assertTrue(result.isPresent());
    assertThat(result.get(), is("value1"));
  }

  @Test
  public void returnPropertyValueIfPresentInMovementCommandLocation() {
    MovementCommand command = createMovementCommand(
        new Location("location1", new LocationType("locationType1").getReference())
            .withProperty("some-property", "value1"),
        Map.of()
    );

    Optional<String> result = PropertyExtractions.getProperty("some-property", command);

    assertTrue(result.isPresent());
    assertThat(result.get(), is("value1"));
  }

  @Test
  public void returnEmptyIfPropertyNotPresentInMovementCommandOrLocation() {
    MovementCommand command = createMovementCommand(
        new Location("location1", new LocationType("locationType1").getReference())
            .withProperty("some-property", "value1"),
        Map.of("some-property", "value2")
    );

    Optional<String> result = PropertyExtractions.getProperty("some-other-property", command);

    assertFalse(result.isPresent());
  }

  @Test
  public void returnPropertyValueIfPresentInMovementCommandFloat() {
    MovementCommand command = createMovementCommand(null, Map.of("some-property", "11.0"));

    Optional<Float> result = PropertyExtractions.getPropertyFloat("some-property", command);

    assertTrue(result.isPresent());
    assertThat(result.get(), is(11.0F));
  }

  @Test
  public void returnEmptyIfPropertyNotPresentInMovementCommandFloat() {
    MovementCommand command = createMovementCommand(null, Map.of("some-property", "47.0"));

    Optional<Float> result = PropertyExtractions.getPropertyFloat("some-other-property", command);

    assertFalse(result.isPresent());
  }

  @Test
  public void returnEmptyIfMovementCommandCompletedConditionPropertyNotPresentInVehicle() {
    Vehicle vehicle = new Vehicle("vehicle-01");

    Optional<MovementCommandCompletedCondition> result
        = PropertyExtractions.getMovementCommandCompletedCondition(
            "MovementCommandCompletedCondition",
            vehicle
        );

    assertThat(result).isEmpty();
  }

  @Test
  public void returnEmptyIfMovementCommandCompletedConditionPropertyValueNotValid() {
    Vehicle vehicle = new Vehicle("vehicle-01")
        .withProperty(
            "MovementCommandCompletedCondition",
            "invalid-value"
        );

    Optional<MovementCommandCompletedCondition> result
        = PropertyExtractions.getMovementCommandCompletedCondition(
            "MovementCommandCompletedCondition",
            vehicle
        );

    assertTrue(result.isEmpty());
  }

  @ParameterizedTest
  @EnumSource(MovementCommandCompletedCondition.class)
  public void returnMovementCommandCompletedConditionPropertyIfPresentInVehicle(
      MovementCommandCompletedCondition value
  ) {
    Vehicle vehicle = new Vehicle("vehicle-01")
        .withProperty(
            "MovementCommandCompletedCondition",
            value.name()
        );

    Optional<MovementCommandCompletedCondition> result
        = PropertyExtractions.getMovementCommandCompletedCondition(
            "MovementCommandCompletedCondition",
            vehicle
        );

    assertThat(result).isPresent();
  }

  private MovementCommand createMovementCommand(Location location, Map<String, String> properties) {
    Point point1 = new Point("1");
    Point point2 = new Point("2");

    return new MovementCommand(
        new TransportOrder("1", List.of()),
        new DriveOrder("drive-order", new DriveOrder.Destination(point2.getReference())),
        new Route.Step(
            new Path("path", point1.getReference(), point2.getReference()),
            point1,
            point2,
            Vehicle.Orientation.FORWARD,
            0,
            1
        ),
        "NOP",
        location,
        true,
        null,
        point2,
        "NOP",
        properties
    );
  }
}
