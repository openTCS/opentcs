// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_EXTENDED_DEVIATION_RANGE_PADDING;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.NodePosition;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link NodeMapping}.
 */
public class NodeMappingTest {

  private NodeMapping nodeMapping;

  @BeforeEach
  void setUp() {
    nodeMapping = new NodeMapping();
  }

  @Test
  public void setNodePositionPropertiesFromVehicleIfPointDoesntHaveAny() {
    Point point = new Point("Point-0001");
    Vehicle vehicle = new Vehicle("vehicle-0001")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY, "0.5")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA, "45")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_MAP_ID, "mapid-vehicle");

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, false);

    assertThat(np.getAllowedDeviationXY(), is(closeTo(0.5, 0.001)));
    assertThat(np.getAllowedDeviationTheta(), is(closeTo(Math.PI / 4, 0.00001)));
    assertThat(np.getMapId(), is("mapid-vehicle"));
  }

  @Test
  public void preferNodePositionPropertiesFromPointOverVehicle() {
    Vehicle vehicle = new Vehicle("vehicle-0001")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY, "0.5")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA, "45")
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_MAP_ID, "mapid-vehicle");
    Point point = new Point("Point-0001")
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_XY, "1.2")
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_THETA, "90")
        .withProperty(ObjectProperties.PROPKEY_POINT_MAP_ID, "mapid-point");

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, false);

    assertThat(np.getAllowedDeviationXY(), is(closeTo(1.2, 0.001)));
    assertThat(
        np.getAllowedDeviationTheta(),
        is(closeTo(Math.PI / 2, 0.00001))
    );
    assertThat(np.getMapId(), is("mapid-point"));
  }

  @Test
  public void extendDeviationToIncludeVehicle() {
    Vehicle vehicle = new Vehicle("vehicle-0001")
        .withPose(new Pose(new Triple(5000, 5000, 0), Double.NaN));
    Point point = new Point("Point-0001");
    point = point
        .withPose(point.getPose().withPosition(new Triple(1000, 1000, 0)))
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_XY, "1.2")
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_THETA, "90");

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, true);

    // Assert that the computed deviation is the distance between vehicle position and point,
    // with an extra tolerance added.
    assertThat(
        np.getAllowedDeviationXY(),
        is(closeTo(5.65685 + NodeMapping.EXTENDED_DEVIATION_RANGE_PADDING_DEFAULT, 0.00001))
    );
    assertThat(np.getAllowedDeviationTheta(), is(Math.PI));
  }

  @Test
  public void extendDeviationToIncludeVehicleWithExtraPadding() {
    Vehicle vehicle = new Vehicle("vehicle-0001")
        .withPose(new Pose(new Triple(5000, 5000, 0), Double.NaN))
        .withProperty(PROPKEY_VEHICLE_EXTENDED_DEVIATION_RANGE_PADDING, "1.23");
    Point point = new Point("Point-0001");
    point = point
        .withPose(point.getPose().withPosition(new Triple(1000, 1000, 0)))
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_XY, "1.2")
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_THETA, "90");

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, true);

    // Assert that the computed deviation is the distance between vehicle position and point,
    // with an extra tolerance added.
    assertThat(np.getAllowedDeviationXY(), is(closeTo(5.65685 + 1.23, 0.00001)));
    assertThat(np.getAllowedDeviationTheta(), is(Math.PI));
  }

  @Test
  public void extendedDeviationRangePaddingMustNotBeNegative() {
    Vehicle vehicle = new Vehicle("vehicle-0001")
        .withPose(new Pose(new Triple(5000, 5000, 0), Double.NaN))
        .withProperty(PROPKEY_VEHICLE_EXTENDED_DEVIATION_RANGE_PADDING, "-1.23");
    Point point = new Point("Point-0001");
    point = point
        .withPose(point.getPose().withPosition(new Triple(1000, 1000, 0)))
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_XY, "1.2")
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_THETA, "90");

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, true);

    // If the padding is less than zero it is clipped to zero.
    assertThat(np.getAllowedDeviationXY(), is(closeTo(5.65685 + 0.0, 0.00001)));
    assertThat(np.getAllowedDeviationTheta(), is(Math.PI));
  }

  @Test
  public void stickToNodeDeviationIfVehicleIsCloser() {
    Vehicle vehicle = new Vehicle("vehicle-0001")
        .withPose(new Pose(new Triple(500, 500, 0), Double.NaN));
    Point point = new Point("Point-0001");
    point = point
        .withPose(point.getPose().withPosition(new Triple(1000, 1000, 0)))
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_XY, "1.2")
        .withProperty(ObjectProperties.PROPKEY_POINT_DEVIATION_THETA, "90");

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, true);

    assertThat(np.getAllowedDeviationXY(), is(closeTo(1.2, 0.0)));
    assertThat(np.getAllowedDeviationTheta(), is(Math.PI));
  }

  @Test
  public void setNodePositionDefaultPropertiesIfNeitherPointNorVehicleHaveThem() {
    Point point = new Point("Point-0001");
    Vehicle vehicle = new Vehicle("vehicle-0001");

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, false);

    assertThat(np.getAllowedDeviationXY(), is(nullValue()));
    assertThat(np.getAllowedDeviationTheta(), is(nullValue()));
    assertThat(np.getMapId(), is(emptyString()));
  }

  @Test
  public void setPointPosition() {
    Vehicle vehicle = new Vehicle("vehicle-0001");
    Point point = new Point("Point-0001");
    point = point.withPose(point.getPose().withPosition(new Triple(12000, 144000, 0)));

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, false);

    assertThat(np.getX(), is(closeTo(12, 0.1)));
    assertThat(np.getY(), is(closeTo(144, 0.1)));
    assertThat(np.getMapId(), is(emptyString()));
  }

  @Test
  public void setKnownPointOrientation() {
    Vehicle vehicle = new Vehicle("vehicle-0001");
    Point point = new Point("Point-0001");

    point = point.withPose(point.getPose().withOrientationAngle(180.0));
    NodePosition np = nodeMapping.toNodePosition(point, vehicle, false);
    assertThat(np.getTheta(), closeTo(Math.PI, 0.00001));

    point = point.withPose(point.getPose().withOrientationAngle(270.0));
    np = nodeMapping.toNodePosition(point, vehicle, false);
    assertThat(np.getTheta(), closeTo(-Math.PI / 2, 0.00001));
  }

  @Test
  public void leaveUnknownPointOrientationUnset() {
    Vehicle vehicle = new Vehicle("vehicle-0001");
    Point point = new Point("Point-0001");
    point = point.withPose(point.getPose().withOrientationAngle(Double.NaN));

    NodePosition np = nodeMapping.toNodePosition(point, vehicle, false);

    assertThat(np.getTheta(), is(nullValue()));
  }
}
