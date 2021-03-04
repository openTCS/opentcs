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
public class StandardVehicleTheme
    implements VehicleTheme {

  /**
   * The single image used for representing vehicles, regardless of their state.
   */
  private final Image image;
  /**
   * The path containing the images.
   */
  private static final String PATH = "/org/opentcs/guing/res/symbols/vehicle/Vehicle24.png";

  public StandardVehicleTheme() {
    this.image = loadImage(PATH);
  }

  @Override
  public Image statelessImage(Vehicle vehicle) {
    return image;
  }

  @Override
  public Image statefulImage(Vehicle vehicle) {
    return image;
  }

  @Override
  @Deprecated
  public Image getImageFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    return image;
  }

  @Deprecated
  @Override
  public Image getThemeImage() {
    return image;
  }

  @Deprecated
  @Override
  public String getName() {
    return "Standard vehicle theme";
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
