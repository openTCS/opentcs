/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The data used by coordinate system transformer classes.
 * <p>
 * The data consists of offset values that are ...
 * </p>
 * <ul>
 * <li>added to coordinate and orientation angle values sent to the vehicle driver.</li>
 * <li>subtracted from coordinate and orientation angle values reported by the vehicle driver.</li>
 * </ul>
 */
public class CoordinateSystemTransformation {

  /**
   * The key of the property for the x offset.
   */
  public static final String PROPKEY_OFFSET_X = "tcs:offsetTransformer.x";
  /**
   * The key of the property for the y offset.
   */
  public static final String PROPKEY_OFFSET_Y = "tcs:offsetTransformer.y";
  /**
   * The key of the property for the z offset.
   */
  public static final String PROPKEY_OFFSET_Z = "tcs:offsetTransformer.z";
  /**
   * The key of the property for the orientation offset.
   */
  public static final String PROPKEY_OFFSET_ORIENTATION = "tcs:offsetTransformer.orientation";

  private static final Logger LOG = LoggerFactory.getLogger(CoordinateSystemTransformation.class);
  private final int offsetX;
  private final int offsetY;
  private final int offsetZ;
  private final double offsetOrientation;

  /**
   * Creates a new instance.
   *
   * @param offsetX The x offset.
   * @param offsetY The y offset.
   * @param offsetZ The z offset.
   * @param offsetOrientation The orientation offset.
   */
  public CoordinateSystemTransformation(
      int offsetX, int offsetY, int offsetZ, double offsetOrientation
  ) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.offsetZ = offsetZ;
    this.offsetOrientation = offsetOrientation;
  }

  /**
   * Returns the x offset.
   *
   * @return The x offset.
   */
  public int getOffsetX() {
    return offsetX;
  }

  /**
   * Returns the y offset.
   *
   * @return The y offset.
   */
  public int getOffsetY() {
    return offsetY;
  }

  /**
   * Returns the z offset.
   *
   * @return The z offset.
   */
  public int getOffsetZ() {
    return offsetZ;
  }

  /**
   * Returns the orientation offset.
   *
   * @return The orientation offset.
   */
  public double getOffsetOrientation() {
    return offsetOrientation;
  }

  /**
   * Creates a {@link CoordinateSystemTransformation} based on offsets given via vehicle properties.
   * <p>
   * The property keys used are the values of the following constants:
   * </p>
   * <ul>
   * <li>{@link #PROPKEY_OFFSET_X}</li>
   * <li>{@link #PROPKEY_OFFSET_Y}</li>
   * <li>{@link #PROPKEY_OFFSET_Z}</li>
   * <li>{@link #PROPKEY_OFFSET_ORIENTATION}</li>
   * </ul>
   *
   * @param vehicle The vehicle.
   * @return An {@link Optional} containing the {@link CoordinateSystemTransformation} or
   * {@link Optional#empty()}, if one of the corresponding property values could not be parsed.
   */
  public static Optional<CoordinateSystemTransformation> fromVehicle(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    try {
      return Optional.of(
          new CoordinateSystemTransformation(
              getPropertyInteger(vehicle, PROPKEY_OFFSET_X, 0),
              getPropertyInteger(vehicle, PROPKEY_OFFSET_Y, 0),
              getPropertyInteger(vehicle, PROPKEY_OFFSET_Z, 0),
              getPropertyDouble(vehicle, PROPKEY_OFFSET_ORIENTATION, 0.0)
          )
      );
    }
    catch (NumberFormatException e) {
      LOG.warn(
          "Could not create coordinate system transformation for vehicle '{}'.",
          vehicle.getName(),
          e
      );
      return Optional.empty();
    }
  }

  private static int getPropertyInteger(Vehicle vehicle, String propertyKey, int defaultValue)
      throws NumberFormatException {
    String property = vehicle.getProperty(propertyKey);
    if (property != null) {
      return Integer.parseInt(property);
    }
    else {
      LOG.debug(
          "{}: Property '{}' is not set. Using default value {}.",
          vehicle.getName(),
          propertyKey,
          defaultValue
      );
      return defaultValue;
    }
  }


  private static double getPropertyDouble(Vehicle vehicle, String propertyKey, double defaultValue)
      throws NumberFormatException {
    String property = vehicle.getProperty(propertyKey);
    if (property != null) {
      return Double.parseDouble(property);
    }
    else {
      LOG.debug(
          "{}: Property '{}' is not set. Using default value {}.",
          vehicle.getName(),
          propertyKey,
          defaultValue
      );
      return defaultValue;
    }
  }
}
