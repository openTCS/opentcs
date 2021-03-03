/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.plugins.themes;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.imageio.ImageIO;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Standard implementation of <code>VehicleTheme</code>.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StandardVehicleTheme2
    implements VehicleTheme {

  /**
   * Map containing images for a specific vehicle state when it's unloaded.
   */
  private final Map<Vehicle.State, Image> stateMapUnloaded
      = new EnumMap<>(Vehicle.State.class);
  /**
   * Map containing images for a specific vehicle state when it's loaded.
   */
  private final Map<Vehicle.State, Image> stateMapLoaded
      = new EnumMap<>(Vehicle.State.class);
  /**
   * The path containing the images.
   */
  private static final String path
      = "/org/opentcs/guing/res/symbols/vehicle/";

  public StandardVehicleTheme2() {
    initMaps();
  }

  @Override
  public Image getImageFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle is null");

    return checkLoaded(vehicle)
        ? stateMapLoaded.get(vehicle.getState())
        : stateMapUnloaded.get(vehicle.getState());
  }

  @Override
  public Image getThemeImage() {
    return stateMapUnloaded.get(Vehicle.State.IDLE);
  }

  @Override
  public String getName() {
    return "Standard vehicle theme 2";
  }

  /**
   * Initializes the maps with values.
   */
  private void initMaps() {
    stateMapUnloaded.put(Vehicle.State.CHARGING, loadImage(path + "unloaded_charging.png"));
    stateMapUnloaded.put(Vehicle.State.ERROR, loadImage(path + "unloaded_error.png"));
    stateMapUnloaded.put(Vehicle.State.EXECUTING, loadImage(path + "unloaded_normal.png"));
    stateMapUnloaded.put(Vehicle.State.IDLE, loadImage(path + "unloaded_normal.png"));
    stateMapUnloaded.put(Vehicle.State.UNAVAILABLE, loadImage(path + "unloaded_normal.png"));
    stateMapUnloaded.put(Vehicle.State.UNKNOWN, loadImage(path + "unloaded_normal.png"));

    stateMapLoaded.put(Vehicle.State.CHARGING, loadImage(path + "loaded_charging.png"));
    stateMapLoaded.put(Vehicle.State.ERROR, loadImage(path + "loaded_error.png"));
    stateMapLoaded.put(Vehicle.State.EXECUTING, loadImage(path + "loaded_normal.png"));
    stateMapLoaded.put(Vehicle.State.IDLE, loadImage(path + "loaded_normal.png"));
    stateMapLoaded.put(Vehicle.State.UNAVAILABLE, loadImage(path + "loaded_normal.png"));
    stateMapLoaded.put(Vehicle.State.UNKNOWN, loadImage(path + "loaded_normal.png"));
  }

  /**
   * Checks if a given vehicle is loaded.
   *
   * @param vehicle The vehicle.
   * @return Flag indicating if it is loaded.
   */
  private boolean checkLoaded(Vehicle vehicle) {
    if (vehicle.getLoadHandlingDevices().isEmpty()) {
      return false;
    }
    else {
      for (LoadHandlingDevice device : vehicle.getLoadHandlingDevices()) {
        if (device.isFull()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Loads an image from the file with the given name.
   *
   * @param fileName The name of the file from which to load the image.
   * @return The image.
   */
  private Image loadImage(String fileName) {
    requireNonNull(fileName, "fileName");

    URL url = getClass().getResource(fileName);
    if (url == null) {
      throw new IllegalArgumentException("Invalid image file name " + fileName);
    }
    try {
      return ImageIO.read(url);
    }
    catch (IOException exc) {
      throw new IllegalArgumentException("Exception loading image", exc);
    }
  }
}
