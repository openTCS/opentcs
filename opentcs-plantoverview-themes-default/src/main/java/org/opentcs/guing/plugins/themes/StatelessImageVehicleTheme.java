// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.themes;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.data.model.Vehicle;

/**
 * An implementation of <code>VehicleTheme</code> using a single image, disregarding vehicles'
 * states.
 */
public class StatelessImageVehicleTheme
    implements
      VehicleTheme {

  /**
   * The path containing the images.
   */
  private static final String PATH
      = "/org/opentcs/guing/plugins/themes/symbols/vehicle/Vehicle24.png";
  /**
   * The single image used for representing vehicles, regardless of their state.
   */
  private final Image image;

  /**
   * Creates a new instance.
   */
  public StatelessImageVehicleTheme() {
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

  @Override
  public String label(Vehicle vehicle) {
    String name = vehicle.getName();

    // Find digits.
    Pattern p = Pattern.compile("\\d+");
    Matcher m = p.matcher(name);

    // If at least one group of digits was found, use the first one.
    if (m.find()) {
      return m.group();
    }

    return name;
  }

  @Override
  public int labelOffsetX() {
    return -8;
  }

  @Override
  public int labelOffsetY() {
    return 5;
  }

  @Override
  public Color labelColor() {
    return Color.BLUE;
  }

  @Override
  public Font labelFont() {
    return new Font("Dialog", Font.BOLD, 12);
  }
}
