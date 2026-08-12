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
 * Defines the position on a map in world coordinates. Each floor has its own map.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgvPosition
    implements
      Serializable {

  /**
   * X-position on the map in reference to the map coordinate system.
   * <p>
   * Precision is up to the specific implementation.
   */
  private Double x;
  /**
   * Y-position on the map in reference to the map coordinate system.
   * <p>
   * Precision is up to the specific implementation.
   */
  private Double y;
  /**
   * Orientation of the AGV (in rad). Range: [-PI ... PI]
   */
  private Double theta;
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
  /**
   * Whether the AGV's position is initialized or not.
   */
  private Boolean positionInitialized;
  /**
   * [Optional] Describes the quality of the localization. Range: [0.0 ... 1.0]
   * <p>
   * Can be used e. g. by SLAM-AGVs to describe how accurate the current position information is.
   * <p>
   * Interpretation of values:
   * <ul>
   * <li>0.0: Position unknown</li>
   * <li>1.0: Position known</li>
   * </ul>
   * <p>
   * Optional for vehicles that cannot estimate their localization score. Only for logging and
   * visualization purposes.
   */
  private Double localizationScore;
  /**
   * [Optional] Value for position deviation range (in m).
   * <p>
   * Optional for vehicles that cannot estimate their deviation (e.g. with grid-based localization).
   * Only for logging and visualization purposes.
   */
  private Double deviationRange;

  // CHECKSTYLE:OFF (Long lines because some of these parameter declarations are very long.)
  @JsonCreator
  public AgvPosition(
      @Nonnull
      @JsonProperty(required = true, value = "x")
      Double x,
      @Nonnull
      @JsonProperty(required = true, value = "y")
      Double y,
      @Nonnull
      @JsonProperty(required = true, value = "theta")
      Double theta,
      @Nonnull
      @JsonProperty(required = true, value = "mapId")
      String mapId,
      @Nonnull
      @JsonProperty(required = true, value = "positionInitialized")
      Boolean positionInitialized
  ) {
    this.x = requireNonNull(x, "x");
    this.y = requireNonNull(y, "y");
    this.theta = checkInRange(requireNonNull(theta, "theta"), -Math.PI, Math.PI, "theta");
    this.mapId = requireNonNull(mapId, "mapId");
    this.positionInitialized = requireNonNull(positionInitialized, "positionInitialized");
  }
  // CHECKSTYLE:ON

  public Double getX() {
    return x;
  }

  public AgvPosition setX(
      @Nonnull
      Double x
  ) {
    this.x = requireNonNull(x, "x");
    return this;
  }

  public Double getY() {
    return y;
  }

  public AgvPosition setY(
      @Nonnull
      Double y
  ) {
    this.y = requireNonNull(y, "y");
    return this;
  }

  public Double getTheta() {
    return theta;
  }

  public AgvPosition setTheta(
      @Nonnull
      Double theta
  ) {
    this.theta = checkInRange(requireNonNull(theta, "theta"), -Math.PI, Math.PI, "theta");
    return this;
  }

  public String getMapId() {
    return mapId;
  }

  public AgvPosition setMapId(
      @Nonnull
      String mapId
  ) {
    this.mapId = requireNonNull(mapId, "mapId");
    return this;
  }

  public String getMapDescription() {
    return mapDescription;
  }

  public AgvPosition setMapDescription(String mapDescription) {
    this.mapDescription = mapDescription;
    return this;
  }

  public Boolean isPositionInitialized() {
    return positionInitialized;
  }

  public AgvPosition setPositionInitialized(
      @Nonnull
      Boolean positionInitialized
  ) {
    this.positionInitialized = requireNonNull(positionInitialized, "positionInitialized");
    return this;
  }

  public Double getLocalizationScore() {
    return localizationScore;
  }

  public AgvPosition setLocalizationScore(Double localizationScore) {
    if (localizationScore != null) {
      checkInRange(localizationScore, 0.0, 1.0, "localizationScore");
    }
    this.localizationScore = localizationScore;
    return this;
  }

  public Double getDeviationRange() {
    return deviationRange;
  }

  public AgvPosition setDeviationRange(Double deviationRange) {
    this.deviationRange = deviationRange;
    return this;
  }

  @Override
  public String toString() {
    return "AgvPosition{" + "x=" + x
        + ", y=" + y
        + ", theta=" + theta
        + ", mapId=" + mapId
        + ", mapDescription=" + mapDescription
        + ", positionInitialized=" + positionInitialized
        + ", localizationScore=" + localizationScore
        + ", deviationRange=" + deviationRange
        + '}';
  }

}
