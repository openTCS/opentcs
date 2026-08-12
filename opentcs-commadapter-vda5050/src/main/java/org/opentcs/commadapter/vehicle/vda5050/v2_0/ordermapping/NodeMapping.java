// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping;

import static java.lang.Math.toRadians;
import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.AngleMath.toRelativeConvexAngle;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getProperty;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyDouble;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_EXTENDED_DEVIATION_RANGE_PADDING;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAP_ID;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.NodePosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Node;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Function to map {@link Point}s from openTCS movement commands to a VDA5050 node.
 */
public class NodeMapping {

  /**
   * The default value of the extended deviation range padding.
   */
  public static final double EXTENDED_DEVIATION_RANGE_PADDING_DEFAULT = 0.01;

  /**
   * Prevents unwanted instantiation.
   */
  @Inject
  public NodeMapping() {
  }

  /**
   * Maps a {@link Point} to a VDA5050 base node.
   * The node has the release flag set to true.
   *
   * @param point The point to map.
   * @param sequenceId The sequence ID for the node to be created.
   * @param vehicle The vehicle to map the point for.
   * @param actions The actions for this node.
   * @param extendDeviationToIncludeVehicle Whether the node's deviation should be extended to
   * include the vehicle's position.
   * @return A mapped Node.
   */
  public Node toBaseNode(
      Point point,
      long sequenceId,
      Vehicle vehicle,
      List<Action> actions,
      boolean extendDeviationToIncludeVehicle
  ) {
    requireNonNull(point, "point");
    requireNonNull(vehicle, "vehicle");
    requireNonNull(actions, "actions");

    Node node = new Node(
        point.getName(),
        sequenceId,
        true,
        actions
    );
    node.setNodePosition(toNodePosition(point, vehicle, extendDeviationToIncludeVehicle));
    return node;
  }

  /**
   * Maps a {@link Point} to a VDA5050 horizon node.
   * The node has the release flag set to false.
   *
   * @param point The point to map.
   * @param sequenceId The sequence ID for the node to be created.
   * @param vehicle The vehicle to map the point for.
   * @param actions The actions for this node.
   * include the vehicle's position.
   * @return A mapped Node.
   */
  public Node toHorizonNode(
      Point point,
      long sequenceId,
      Vehicle vehicle,
      List<Action> actions
  ) {
    requireNonNull(point, "point");
    requireNonNull(vehicle, "vehicle");

    Node node = new Node(
        point.getName(),
        sequenceId,
        false,
        actions
    );
    node.setNodePosition(toNodePosition(point, vehicle, false));
    return node;
  }

  /**
   * Maps the given point to a node position, filled with data taken from both the point and the
   * vehicle.
   *
   * @param point The point.
   * @param vehicle The vehicle.
   * @param extendDeviationToIncludeVehicle Whether the node's deviation should be extended to
   * include the vehicle's position.
   * @return A node position.
   */
  public NodePosition toNodePosition(
      @Nonnull
      Point point,
      @Nonnull
      Vehicle vehicle,
      boolean extendDeviationToIncludeVehicle
  ) {
    requireNonNull(point, "point");
    requireNonNull(vehicle, "vehicle");

    NodePosition position = new NodePosition(
        point.getPose().getPosition().getX() / 1000.0,
        point.getPose().getPosition().getY() / 1000.0,
        getProperty(PROPKEY_VEHICLE_MAP_ID, point, vehicle).orElse("")
    );

    if (!Double.isNaN(point.getPose().getOrientationAngle())) {
      position.setTheta(
          toRadians(toRelativeConvexAngle(point.getPose().getOrientationAngle()))
      );
    }

    position.setAllowedDeviationXY(
        extendDeviationToIncludeVehicle
            ? extendedDeviationXY(point, vehicle)
            : regularDeviationXY(point, vehicle).orElse(null)
    );

    position.setAllowedDeviationTheta(
        extendDeviationToIncludeVehicle
            ? extendedDeviationTheta()
            : regularDeviationTheta(point, vehicle).orElse(null)
    );

    return position;
  }

  private Optional<Double> regularDeviationXY(Point point, Vehicle vehicle) {
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_XY, point, vehicle);
  }

  private Optional<Double> regularDeviationTheta(Point point, Vehicle vehicle) {
    // XXX Ensure the angle is (positive and) within 0 and 180 degrees.
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_THETA, point, vehicle)
        .map(value -> toRadians(value));
  }

  private Double extendedDeviationXY(Point point, Vehicle vehicle) {
    if (vehicle.getPose().getPosition() == null) {
      return 0.0;
    }

    // Ensure the deviation range is large enough for the vehicle to accept this node.
    double deltaX
        = (vehicle.getPose().getPosition().getX() - point.getPose().getPosition().getX()) / 1000.0;
    double deltaY
        = (vehicle.getPose().getPosition().getY() - point.getPose().getPosition().getY()) / 1000.0;

    double padding = PropertyExtractions.getPropertyDouble(
        PROPKEY_VEHICLE_EXTENDED_DEVIATION_RANGE_PADDING, vehicle
    )
        .map(p -> Math.max(p, 0.0))
        .orElse(EXTENDED_DEVIATION_RANGE_PADDING_DEFAULT);

    return Math.max(
        Math.sqrt(deltaX * deltaX + deltaY * deltaY) + padding,
        regularDeviationXY(point, vehicle).orElse(0.0)
    );
  }

  private Double extendedDeviationTheta() {
    return Math.PI;
  }
}
