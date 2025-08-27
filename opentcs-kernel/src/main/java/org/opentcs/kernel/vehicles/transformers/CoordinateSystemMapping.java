// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;
import org.opentcs.drivers.vehicle.MovementCommandTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The data used by {@link MovementCommandTransformer} and {@link IncomingPoseTransformer} instances
 * provided by {@link CoordinateSystemMapperFactory}.
 * <p>
 * The data describes the position and orientation of a vehicle's coordinate system in relation to
 * the plant model's coordinate system and consists of...
 * </p>
 * <ul>
 * <li>
 * translations in x, y and z direction in relation to the origin of the plant model's coordinate
 * system.
 * (Example: Offsets of x=1000, y=2000 and z=3000 mean that the origin of the vehicle's coordinate
 * system is located at the coordinates x=1000, y=2000 and z=3000 in the plant model's coordinate
 * system.)
 * </li>
 * <li>
 * a rotation of the vehicle's coordinate system along its z-axis in relation to orientation of the
 * plant model's coordinate system
 * (Example: A 90-degree rotation means that the vehicle's coordinate system is rotated 90 degrees
 * counter-clockwise in relation to the plant model's coordinate system.)
 * </li>
 * </ul>
 */
public class CoordinateSystemMapping {

  /**
   * The key of the property for the x translation offset.
   */
  public static final String PROPKEY_TRANSLATION_X = "tcs:coordinateSystemMapper.translation.x";
  /**
   * The key of the property for the y translation offset.
   */
  public static final String PROPKEY_TRANSLATION_Y = "tcs:coordinateSystemMapper.translation.y";
  /**
   * The key of the property for the z translation offset.
   */
  public static final String PROPKEY_TRANSLATION_Z = "tcs:coordinateSystemMapper.translation.z";
  /**
   * The key of the property for the z rotation.
   */
  public static final String PROPKEY_ROTATION_Z = "tcs:coordinateSystemMapper.rotation.z";

  private static final Logger LOG = LoggerFactory.getLogger(CoordinateSystemMapping.class);
  private final int translationX;
  private final int translationY;
  private final int translationZ;
  private final double rotationZ;

  /**
   * Creates a new instance.
   *
   * @param translationX The x translation offset (in mm).
   * @param translationY The y translation offset (in mm).
   * @param translationZ The z translation offset (in mm).
   * @param rotationZ The z rotation (in degrees).
   */
  public CoordinateSystemMapping(
      int translationX, int translationY, int translationZ, double rotationZ
  ) {
    this.translationX = translationX;
    this.translationY = translationY;
    this.translationZ = translationZ;
    this.rotationZ = rotationZ;
  }

  /**
   * Returns the x translation offset (in mm).
   *
   * @return The x translation offset (in mm).
   */
  public int getTranslationX() {
    return translationX;
  }

  /**
   * Returns the y translation offset (in mm).
   *
   * @return The y translation offset (in mm).
   */
  public int getTranslationY() {
    return translationY;
  }

  /**
   * Returns the z translation offset (in mm).
   *
   * @return The z translation offset (in mm).
   */
  public int getTranslationZ() {
    return translationZ;
  }

  /**
   * Returns the z rotation (in degrees).
   *
   * @return The z rotation (in degrees).
   */
  public double getRotationZ() {
    return rotationZ;
  }

  /**
   * Creates a {@link CoordinateSystemMapping} based on vehicle properties.
   * <p>
   * The property keys used are the values of the following constants:
   * </p>
   * <ul>
   * <li>{@link #PROPKEY_TRANSLATION_X}</li>
   * <li>{@link #PROPKEY_TRANSLATION_Y}</li>
   * <li>{@link #PROPKEY_TRANSLATION_Z}</li>
   * <li>{@link #PROPKEY_ROTATION_Z}</li>
   * </ul>
   *
   * @param vehicle The vehicle.
   * @return An {@link Optional} containing the {@link CoordinateSystemMapping} or
   * {@link Optional#empty()}, if one of the corresponding property values could not be parsed.
   */
  public static Optional<CoordinateSystemMapping> fromVehicle(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    try {
      return Optional.of(
          new CoordinateSystemMapping(
              getPropertyInteger(vehicle, PROPKEY_TRANSLATION_X, 0),
              getPropertyInteger(vehicle, PROPKEY_TRANSLATION_Y, 0),
              getPropertyInteger(vehicle, PROPKEY_TRANSLATION_Z, 0),
              getPropertyDouble(vehicle, PROPKEY_ROTATION_Z, 0.0)
          )
      );
    }
    catch (NumberFormatException e) {
      LOG.warn(
          "Could not create coordinate system mapping for vehicle '{}'.",
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
