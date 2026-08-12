// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.Assertions.checkInRange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Defines the position on a map in world coordinates.
 * <p>
 * Each floor has its own map. All maps must use the same project specific global origin.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodePosition
    implements
      Serializable {

  /**
   * X-position on the map in reference to the map coordinate system (in m).
   * Precision is up to the specific implementation.
   */
  private Double x;
  /**
   * Y-position on the map in reference to the map coordinate system (in m).
   * Precision is up to the specific implementation.
   */
  private Double y;
  /**
   * [Optional] Orientation of the AGV on the node (in rad). Range: [-PI ... PI]
   * <p>
   * Optional for AGVs that plan their path by themselves.
   * <p>
   * If defined, the AGV has to assume the theta angle on this node. If previous edge disallows
   * rotation, the AGV is to rotate on the node. If following edge has a differing orientation
   * defined but disallows rotation, the AGV is to rotate on the node to the edges desired rotation
   * before entering the edge.
   */
  private Double theta;
  /**
   * [Optional] Indicates how exact an AGV has to drive over a node in order for it to count as
   * traversed.
   * <p>
   * Interpretation of values:
   * <ul>
   * <li>If equals 0: No deviation is allowed. (No deviation means within the normal tolerance of
   * the AGV manufacturer.)</li>
   * <li>If greater than 0: Allowed deviation radius (in m). If the AGV passes a node within the
   * deviation radius, the node is considered to have been traversed.</li>
   * </ul>
   */
  private Double allowedDeviationXY;
  /**
   * [Optional] Indicates how big the deviation of theta angle can be. Range: [0 ... PI]
   * <p>
   * The lowest acceptable angle is {@code <theta> - <allowedDevaitionTheta>} and the highest
   * acceptable angle is {@code <theta> + <allowedDeviationTheta>}.
   */
  private Double allowedDeviationTheta;
  /**
   * Unique identification of the map in which the position is referenced.
   * <p>
   * Each map has the same origin of coordinates. When an AGV uses an elevator, e. g. leading from a
   * departure floor to a target floor, it will disappear off the map of the departure floor and
   * spawn in the related lift node on the map of the target floor.
   */
  private String mapId;
  /**
   * [Optional] Additional information on the map.
   */
  private String mapDescription;

  @JsonCreator
  public NodePosition(
      @Nonnull
      @JsonProperty(required = true, value = "x")
      Double x,
      @Nonnull
      @JsonProperty(required = true, value = "y")
      Double y,
      @Nonnull
      @JsonProperty(required = true, value = "mapId")
      String mapId
  ) {
    this.x = requireNonNull(x, "x");
    this.y = requireNonNull(y, "y");
    this.mapId = requireNonNull(mapId, "mapId");
  }

  public Double getX() {
    return x;
  }

  public NodePosition setX(
      @Nonnull
      Double x
  ) {
    this.x = requireNonNull(x, "x");
    return this;
  }

  public Double getY() {
    return y;
  }

  public NodePosition setY(
      @Nonnull
      Double y
  ) {
    this.y = requireNonNull(y, "y");
    return this;
  }

  public Double getTheta() {
    return theta;
  }

  public NodePosition setTheta(Double theta) {
    if (theta != null) {
      checkInRange(theta, -Math.PI, Math.PI, "theta");
    }
    this.theta = theta;
    return this;
  }

  public Double getAllowedDeviationXY() {
    return allowedDeviationXY;
  }

  public NodePosition setAllowedDeviationXY(Double allowedDeviationXY) {
    this.allowedDeviationXY = allowedDeviationXY;
    return this;
  }

  public Double getAllowedDeviationTheta() {
    return allowedDeviationTheta;
  }

  public NodePosition setAllowedDeviationTheta(Double allowedDeviationTheta) {
    if (allowedDeviationTheta != null) {
      checkInRange(allowedDeviationTheta, 0, Math.PI, "allowedDeviationTheta");
    }
    this.allowedDeviationTheta = allowedDeviationTheta;
    return this;
  }

  public String getMapId() {
    return mapId;
  }

  public NodePosition setMapId(
      @Nonnull
      String mapId
  ) {
    this.mapId = requireNonNull(mapId, "mapId");
    return this;
  }

  public String getMapDescription() {
    return mapDescription;
  }

  public NodePosition setMapDescription(String mapDescription) {
    this.mapDescription = mapDescription;
    return this;
  }

  @Override
  public String toString() {
    return "NodePosition{" + "x=" + x
        + ", y=" + y
        + ", theta=" + theta
        + ", allowedDeviationXY=" + allowedDeviationXY
        + ", allowedDeviationTheta=" + allowedDeviationTheta
        + ", mapId=" + mapId
        + ", mapDescription=" + mapDescription
        + '}';
  }

}
