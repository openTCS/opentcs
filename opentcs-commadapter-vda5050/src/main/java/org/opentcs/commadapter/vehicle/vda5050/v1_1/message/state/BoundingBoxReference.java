// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Point of reference for the location of the bounding box.
 * <p>
 * The point of reference is always the center of the bounding box's bottom surface (at height = 0)
 * and is described in coordinates of the AGV's coordinate system.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoundingBoxReference
    implements
      Serializable {

  /**
   * X-coordinate of the point of reference.
   */
  private Double x;
  /**
   * Y-coordinate of the point of reference.
   */
  private Double y;
  /**
   * Z-coordinate of the point of reference.
   */
  private Double z;
  /**
   * [Optional] Orientation of the loads bounding box.
   * <p>
   * Important e.g. for tugger trains.
   */
  private Double theta;

  @JsonCreator
  public BoundingBoxReference(
      @Nonnull
      @JsonProperty(required = true, value = "x")
      Double x,
      @Nonnull
      @JsonProperty(required = true, value = "y")
      Double y,
      @Nonnull
      @JsonProperty(required = true, value = "z")
      Double z
  ) {
    this.x = requireNonNull(x, "x");
    this.y = requireNonNull(y, "y");
    this.z = requireNonNull(z, "z");
  }

  public Double getX() {
    return x;
  }

  public BoundingBoxReference setX(
      @Nonnull
      Double x
  ) {
    this.x = requireNonNull(x, "x");
    return this;
  }

  public Double getY() {
    return y;
  }

  public BoundingBoxReference setY(
      @Nonnull
      Double y
  ) {
    this.y = requireNonNull(y, "y");
    return this;
  }

  public Double getZ() {
    return z;
  }

  public BoundingBoxReference setZ(
      @Nonnull
      Double z
  ) {
    this.z = requireNonNull(z, "z");
    return this;
  }

  public Double getTheta() {
    return theta;
  }

  public BoundingBoxReference setTheta(Double theta) {
    this.theta = theta;
    return this;
  }

  @Override
  public String toString() {
    return "BoundingBoxReference{" + "x=" + x + ", y=" + y + ", z=" + z + ", theta=" + theta + '}';
  }

}
