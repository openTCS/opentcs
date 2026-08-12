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
 * Defines the control points of NURBS.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ControlPoint
    implements
      Serializable {

  /**
   * X coordinate described in the world coordinate system (in m).
   */
  private Double x;
  /**
   * Y coordinate described in the world coordinate system (in m).
   */
  private Double y;
  /**
   * [Optional] The weight with which this control point pulls on the curve. Range: [0 ... infinity]
   * <p>
   * When not defined, the default will be 1.0.
   */
  private Double weight;

  @JsonCreator
  public ControlPoint(
      @Nonnull
      @JsonProperty(required = true, value = "x")
      Double x,
      @Nonnull
      @JsonProperty(required = true, value = "y")
      Double y
  ) {
    this.x = requireNonNull(x, "x");
    this.y = requireNonNull(y, "y");
  }

  public Double getWeight() {
    return weight;
  }

  public ControlPoint setWeight(Double weight) {
    this.weight = weight == null ? null : checkInRange(weight, 0.0, Double.MAX_VALUE, "weight");
    return this;
  }

  public Double getX() {
    return x;
  }

  public ControlPoint setX(
      @Nonnull
      Double x
  ) {
    this.x = requireNonNull(x, "x");
    return this;
  }

  public Double getY() {
    return y;
  }

  public ControlPoint setY(
      @Nonnull
      Double y
  ) {
    this.y = requireNonNull(y, "y");
    return this;
  }

  @Override
  public String toString() {
    return "ControlPoint{" + "x=" + x
        + ", y=" + y
        + ", weight=" + weight
        + '}';
  }

}
