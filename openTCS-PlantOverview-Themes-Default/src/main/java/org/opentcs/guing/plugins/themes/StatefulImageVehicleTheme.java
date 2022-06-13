/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.themes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.imageio.ImageIO;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.data.model.Vehicle;

/**
 * An implementation of <code>VehicleTheme</code> using different images for different vehicle
 * states.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatefulImageVehicleTheme
    implements VehicleTheme {

  /**
   * The path containing the images.
   */
  private static final String PATH = "/org/opentcs/guing/plugins/themes/symbols/vehicle/";
  /**
   * The font to be used for labels.
   */
  private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
  /**
   * Map containing images for a specific vehicle state when it's in a default state.
   */
  private final Map<Vehicle.State, Image> stateMapDefault
      = new EnumMap<>(Vehicle.State.class);
  /**
   * Map containing images for a specific vehicle state when it's loaded.
   */
  private final Map<Vehicle.State, Image> stateMapLoaded
      = new EnumMap<>(Vehicle.State.class);
  /**
   * Map containing images for a specific vehicle state when it's paused.
   */
  private final Map<Vehicle.State, Image> stateMapPaused
      = new EnumMap<>(Vehicle.State.class);
  /**
   * Map containing images for a specific vehicle state when it's loaded and paused.
   */
  private final Map<Vehicle.State, Image> stateMapLoadedPaused
      = new EnumMap<>(Vehicle.State.class);

  public StatefulImageVehicleTheme() {
    initMaps();
  }

  @Override
  public Image statelessImage(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return stateMapDefault.get(Vehicle.State.IDLE);
  }

  @Override
  public Image statefulImage(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (loaded(vehicle)) {
      return vehicle.isPaused()
          ? stateMapLoadedPaused.get(vehicle.getState())
          : stateMapLoaded.get(vehicle.getState());
    }
    else {
      return vehicle.isPaused()
          ? stateMapPaused.get(vehicle.getState())
          : stateMapDefault.get(vehicle.getState());
    }
  }

  @Override
  public Font labelFont() {
    return LABEL_FONT;
  }

  @Override
  public Color labelColor() {
    return Color.BLUE;
  }

  @Override
  public int labelOffsetY() {
    return 25;
  }

  @Override
  public int labelOffsetX() {
    return -15;
  }

  @Override
  public String label(Vehicle vehicle) {
    return vehicle.getName();
  }

  /**
   * Initializes the maps with values.
   */
  private void initMaps() {
    stateMapDefault.put(Vehicle.State.CHARGING, loadImage(PATH + "charging.png"));
    stateMapDefault.put(Vehicle.State.ERROR, loadImage(PATH + "error.png"));
    stateMapDefault.put(Vehicle.State.EXECUTING, loadImage(PATH + "normal.png"));
    stateMapDefault.put(Vehicle.State.IDLE, loadImage(PATH + "normal.png"));
    stateMapDefault.put(Vehicle.State.UNAVAILABLE, loadImage(PATH + "normal.png"));
    stateMapDefault.put(Vehicle.State.UNKNOWN, loadImage(PATH + "normal.png"));

    stateMapLoaded.put(Vehicle.State.CHARGING, loadImage(PATH + "charging_loaded.png"));
    stateMapLoaded.put(Vehicle.State.ERROR, loadImage(PATH + "error_loaded.png"));
    stateMapLoaded.put(Vehicle.State.EXECUTING, loadImage(PATH + "normal_loaded.png"));
    stateMapLoaded.put(Vehicle.State.IDLE, loadImage(PATH + "normal_loaded.png"));
    stateMapLoaded.put(Vehicle.State.UNAVAILABLE, loadImage(PATH + "normal_loaded.png"));
    stateMapLoaded.put(Vehicle.State.UNKNOWN, loadImage(PATH + "normal_loaded.png"));

    stateMapPaused.put(Vehicle.State.CHARGING, loadImage(PATH + "charging_paused.png"));
    stateMapPaused.put(Vehicle.State.ERROR, loadImage(PATH + "error_paused.png"));
    stateMapPaused.put(Vehicle.State.EXECUTING, loadImage(PATH + "normal_paused.png"));
    stateMapPaused.put(Vehicle.State.IDLE, loadImage(PATH + "normal_paused.png"));
    stateMapPaused.put(Vehicle.State.UNAVAILABLE, loadImage(PATH + "normal_paused.png"));
    stateMapPaused.put(Vehicle.State.UNKNOWN, loadImage(PATH + "normal_paused.png"));

    stateMapLoadedPaused.put(Vehicle.State.CHARGING, loadImage(PATH + "charging_loaded_paused.png"));
    stateMapLoadedPaused.put(Vehicle.State.ERROR, loadImage(PATH + "error_loaded_paused.png"));
    stateMapLoadedPaused.put(Vehicle.State.EXECUTING, loadImage(PATH + "normal_loaded_paused.png"));
    stateMapLoadedPaused.put(Vehicle.State.IDLE, loadImage(PATH + "normal_loaded_paused.png"));
    stateMapLoadedPaused.put(Vehicle.State.UNAVAILABLE, loadImage(PATH + "normal_loaded_paused.png"));
    stateMapLoadedPaused.put(Vehicle.State.UNKNOWN, loadImage(PATH + "normal_loaded_paused.png"));
  }

  /**
   * Checks if a given vehicle is loaded.
   *
   * @param vehicle The vehicle.
   * @return Flag indicating if it is loaded.
   */
  private boolean loaded(Vehicle vehicle) {
    return vehicle.getLoadHandlingDevices().stream()
        .anyMatch(lhd -> lhd.isFull());
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
