/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
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
 * Standard implementation of <code>VehicleTheme</code>.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StandardVehicleTheme2
    implements VehicleTheme {

  /**
   * The path containing the images.
   */
  private static final String PATH = "/org/opentcs/guing/res/symbols/vehicle/";
  /**
   * The font to be used for labels.
   */
  private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
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

  public StandardVehicleTheme2() {
    initMaps();
  }

  @Override
  public Image statelessImage(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return stateMapUnloaded.get(Vehicle.State.IDLE);
  }

  @Override
  public Image statefulImage(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return loaded(vehicle)
        ? stateMapLoaded.get(vehicle.getState())
        : stateMapUnloaded.get(vehicle.getState());
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

  @Override
  @Deprecated
  public Image getImageFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return loaded(vehicle)
        ? stateMapLoaded.get(vehicle.getState())
        : stateMapUnloaded.get(vehicle.getState());
  }

  @Deprecated
  @Override
  public Image getThemeImage() {
    return stateMapUnloaded.get(Vehicle.State.IDLE);
  }

  @Deprecated
  @Override
  public String getName() {
    return "Standard vehicle theme 2";
  }

  /**
   * Initializes the maps with values.
   */
  private void initMaps() {
    stateMapUnloaded.put(Vehicle.State.CHARGING, loadImage(PATH + "unloaded_charging.png"));
    stateMapUnloaded.put(Vehicle.State.ERROR, loadImage(PATH + "unloaded_error.png"));
    stateMapUnloaded.put(Vehicle.State.EXECUTING, loadImage(PATH + "unloaded_normal.png"));
    stateMapUnloaded.put(Vehicle.State.IDLE, loadImage(PATH + "unloaded_normal.png"));
    stateMapUnloaded.put(Vehicle.State.UNAVAILABLE, loadImage(PATH + "unloaded_normal.png"));
    stateMapUnloaded.put(Vehicle.State.UNKNOWN, loadImage(PATH + "unloaded_normal.png"));

    stateMapLoaded.put(Vehicle.State.CHARGING, loadImage(PATH + "loaded_charging.png"));
    stateMapLoaded.put(Vehicle.State.ERROR, loadImage(PATH + "loaded_error.png"));
    stateMapLoaded.put(Vehicle.State.EXECUTING, loadImage(PATH + "loaded_normal.png"));
    stateMapLoaded.put(Vehicle.State.IDLE, loadImage(PATH + "loaded_normal.png"));
    stateMapLoaded.put(Vehicle.State.UNAVAILABLE, loadImage(PATH + "loaded_normal.png"));
    stateMapLoaded.put(Vehicle.State.UNKNOWN, loadImage(PATH + "loaded_normal.png"));
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
