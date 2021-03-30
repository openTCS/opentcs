/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * Utility methods concerning colors/colored elements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Colors {

  /**
   * Prevents undesired instantiation.
   */
  private Colors() {
    // Do nada.
  }

  public static final List<Color> defaultColors() {
    List<Color> colors = new ArrayList<>();
    // Farbbezeichnungen aus CorelDraw-Palette
    colors.add(new Color(255, 0, 0));  // Rot
    colors.add(new Color(0, 0, 255));  // Blau
    colors.add(new Color(0, 255, 255));  // Cyan
    colors.add(new Color(255, 255, 0));  // Gelb
    colors.add(new Color(255, 0, 255));  // Magenta
    colors.add(new Color(153, 0, 204));  // Lila
    colors.add(new Color(255, 102, 0));  // Orange
    colors.add(new Color(204, 204, 255));  // Taubenblau
    colors.add(new Color(153, 153, 255));  // Pastelbalu
    colors.add(new Color(0, 51, 153));  // Marineblau
    colors.add(new Color(51, 204, 102));  // Hellgrün
    colors.add(new Color(0, 102, 51));  // Waldgrün
    colors.add(new Color(102, 255, 204));  // Türkis
    colors.add(new Color(255, 204, 0));  // Dunkelgelb
    colors.add(new Color(255, 153, 255));  // Hellviolett
    colors.add(new Color(255, 102, 102));  // Tropischrosa

    return colors;
  }

  /**
   * Returns a (preferredly unused) color for a new block.
   *
   * @param blocks The existing blocks.
   * @return The color to be used.
   */
  public static Color unusedBlockColor(List<BlockModel> blocks) {
    requireNonNull(blocks, "blocks");

    List<Color> colors = defaultColors();

    List<Color> usedColors = new ArrayList<>();
    for (BlockModel block : blocks) {
      usedColors.add(block.getPropertyColor().getColor());
    }
    for (Color color : colors) {
      if (!usedColors.contains(color)) {
        return color;
      }
    }

    return colors.get(0);
  }

  /**
   * Returns a (preferredly unused) color for a new vehicle.
   *
   * @param vehicles The existing vehicles.
   * @return The color to be used.
   */
  public static Color unusedVehicleColor(List<VehicleModel> vehicles) {
    requireNonNull(vehicles, "vehicles");

    List<Color> colors = defaultColors();

    List<Color> usedColors = new ArrayList<>();
    for (VehicleModel vehicle : vehicles) {
      usedColors.add(vehicle.getPropertyRouteColor().getColor());
    }
    for (Color color : colors) {
      if (!usedColors.contains(color)) {
        return color;
      }
    }

    return colors.get(0);
  }
}
