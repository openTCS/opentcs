// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link PositionDeviationPolicyImpl}.
 */
class PositionDeviationPolicyImplTest {

  private Vehicle vehicleWithoutDeviationValues;

  private Point pointWithoutDeviationValues;

  @BeforeEach
  void setUp() {
    vehicleWithoutDeviationValues = new Vehicle("vehicle-1");

    pointWithoutDeviationValues = new Point("point-1")
        .withPose(new Pose(new Triple(100, 100, 0), 45.0));
  }

  @Test
  void defaultToZeroDeviation() {
    PositionDeviationPolicyImpl policy
        = new PositionDeviationPolicyImpl(vehicleWithoutDeviationValues);

    assertThat(
        policy.allowedDeviationDistance(pointWithoutDeviationValues),
        is(0L)
    );
    assertThat(
        policy.allowedDeviationAngle(pointWithoutDeviationValues),
        is(0L)
    );
  }

  @Test
  void useValuesGivenOnlyInPoint() {
    Point pointWithDeviationValues = pointWithoutDeviationValues
        .withProperty(PROPKEY_VEHICLE_DEVIATION_XY, "20.0")
        .withProperty(PROPKEY_VEHICLE_DEVIATION_THETA, "120.0");

    PositionDeviationPolicyImpl policy
        = new PositionDeviationPolicyImpl(vehicleWithoutDeviationValues);

    assertThat(
        policy.allowedDeviationDistance(pointWithDeviationValues),
        is(20_000L)
    );
    assertThat(
        policy.allowedDeviationAngle(pointWithDeviationValues),
        is(120L)
    );
  }

  @Test
  void useValuesGivenOnlyInVehicle() {
    Vehicle vehicleWithDeviationValues = vehicleWithoutDeviationValues
        .withProperty(PROPKEY_VEHICLE_DEVIATION_XY, "10.0")
        .withProperty(PROPKEY_VEHICLE_DEVIATION_THETA, "90.0");

    PositionDeviationPolicyImpl policy
        = new PositionDeviationPolicyImpl(vehicleWithDeviationValues);

    assertThat(
        policy.allowedDeviationDistance(pointWithoutDeviationValues),
        is(10_000L)
    );
    assertThat(
        policy.allowedDeviationAngle(pointWithoutDeviationValues),
        is(90L)
    );
  }

  @Test
  void preferDeviationValuesInPoint() {
    Vehicle vehicleWithDeviationValues = vehicleWithoutDeviationValues
        .withProperty(PROPKEY_VEHICLE_DEVIATION_XY, "10.0")
        .withProperty(PROPKEY_VEHICLE_DEVIATION_THETA, "90.0");
    Point pointWithDeviationValues = pointWithoutDeviationValues
        .withProperty(PROPKEY_VEHICLE_DEVIATION_XY, "20.0")
        .withProperty(PROPKEY_VEHICLE_DEVIATION_THETA, "120.0");

    PositionDeviationPolicyImpl policy
        = new PositionDeviationPolicyImpl(vehicleWithDeviationValues);

    assertThat(
        policy.allowedDeviationDistance(pointWithDeviationValues),
        is(20_000L)
    );
    assertThat(
        policy.allowedDeviationAngle(pointWithDeviationValues),
        is(120L)
    );
  }
}
