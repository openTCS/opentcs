/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A transfer object describing a point in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PointCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * This point's position (in mm).
   */
  @Nonnull
  private Triple position = new Triple(0, 0, 0);
  /**
   * The vehicle's (assumed) orientation angle (-360..360) when it is at this position.
   * May be Double.NaN if an orientation angle is not defined for this point.
   */
  private double vehicleOrientationAngle = Double.NaN;
  /**
   * This point's type.
   */
  @Nonnull
  private Point.Type type = Point.Type.HALT_POSITION;

  /**
   * Creates a new instance.
   *
   * @param name The name of this point.
   */
  public PointCreationTO(@Nonnull String name) {
    super(name);
  }

  /**
   * Sets the name of this point.
   *
   * @param name The new name.
   * @return The modified point.
   */
  @Nonnull
  @Override
  public PointCreationTO setName(@Nonnull String name) {
    return (PointCreationTO) super.setName(name);
  }

  /**
   * Returns the position of this point (in mm).
   *
   * @return The position of this point (in mm).
   */
  @Nonnull
  public Triple getPosition() {
    return position;
  }

  /**
   * Sets the position of this point (in mm).
   *
   * @param position The new position.
   * @return The modified point.
   */
  @Nonnull
  public PointCreationTO setPosition(@Nonnull Triple position) {
    this.position = requireNonNull(position, "position");
    return this;
  }

  /**
   * Returns a vehicle's orientation angle at this position.
   * (-360..360, or {@code Double.NaN}, if an orientation angle is not specified for this point.)
   *
   * @return The vehicle's orientation angle when it's at this position.
   */
  public double getVehicleOrientationAngle() {
    return vehicleOrientationAngle;
  }

  /**
   * Sets the vehicle's (assumed) orientation angle when it's at this position.
   * Allowed value range: [-360..360], or {@code Double.NaN} to indicate that there is no specific
   * orientation angle for this point.
   *
   * @param vehicleOrientationAngle The new angle.
   * @return The modified point.
   */
  @Nonnull
  public PointCreationTO setVehicleOrientationAngle(double vehicleOrientationAngle) {
    checkArgument(Double.isNaN(vehicleOrientationAngle)
        || (vehicleOrientationAngle >= -360.0 || vehicleOrientationAngle <= 360.0),
                  "vehicleOrientationAngle not in [-360..360]: " + vehicleOrientationAngle);
    this.vehicleOrientationAngle = vehicleOrientationAngle;
    return this;
  }

  /**
   * Returns the type of this point.
   *
   * @return The type of this point.
   */
  @Nonnull
  public Point.Type getType() {
    return type;
  }

  /**
   * Sets the type of this point.
   *
   * @param type The new type.
   * @return The modified point.
   */
  @Nonnull
  public PointCreationTO setType(@Nonnull Point.Type type) {
    this.type = requireNonNull(type, "type");
    return this;
  }

  /**
   * Sets the properties of this point.
   *
   * @param properties The new properties.
   * @return The modified point.
   */
  @Nonnull
  @Override
  public PointCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (PointCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this point.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified point.
   */
  @Nonnull
  @Override
  public PointCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (PointCreationTO) super.setProperty(key, value);
  }
}
