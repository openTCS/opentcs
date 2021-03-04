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
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.data.model.visualization.LocationRepresentation;

/**
 * Standard implementation of <code>LocationTheme</code>.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StandardLocationTheme
    implements LocationTheme {

  /**
   * The path containing the images.
   */
  private static final String path
      = "/org/opentcs/guing/res/symbols/figure/location/";
  /**
   * The available symbols.
   */
  private static final String[] LOCTYPE_REPRESENTATION_SYMBOLS = {
    "TransferStation.20x20.png", // 0
    "WorkingStation.20x20.png", // 1
    "ChargingStation.20x20.png", // 2
    "None.20x20.png", // 3
  };
  /**
   * A map of property values to image file names.
   */
  private final Map<LocationRepresentation, Image> symbolMap
      = new EnumMap<>(LocationRepresentation.class);

  /**
   * Creates a new instance.
   */
  public StandardLocationTheme() {
    initSymbolMap();
  }

  @Override
  public Image getImageFor(LocationRepresentation representation) {
    return symbolMap.get(representation);
  }

  @Override
  public String getName() {
    return "Standard location theme";
  }

  private void initSymbolMap() {
    // NONE: A location without further description
    symbolMap.put(LocationRepresentation.NONE, loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[3]));

    // LOAD_TRANSFER_GENERIC: A generic location for vehicle load transfers.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_GENERIC,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[0]));
    // LOAD_TRANSFER_ALT_1: A location for vehicle load transfers, variant 1.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_1,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[0]));
    // LOAD_TRANSFER_ALT_2: A location for vehicle load transfers, variant 2.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_2,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[0]));
    // LOAD_TRANSFER_ALT_3: A location for vehicle load transfers, variant 3.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_3,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[0]));
    // LOAD_TRANSFER_ALT_4: A location for vehicle load transfers, variant 4.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_4,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[0]));
    // LOAD_TRANSFER_ALT_5: A location for vehicle load transfers, variant 5.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_5,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[0]));

    // WORKING_GENERIC: A location for some generic processing, generic variant.
    symbolMap.put(LocationRepresentation.WORKING_GENERIC,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[1]));
    // WORKING_ALT_1: A location for some generic processing, variant 1.
    symbolMap.put(LocationRepresentation.WORKING_ALT_1,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[1]));
    // WORKING_ALT_2: A location for some generic processing, variant 2.
    symbolMap.put(LocationRepresentation.WORKING_ALT_2,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[1]));

    // RECHARGE_GENERIC: A location for recharging a vehicle, generic variant.
    symbolMap.put(LocationRepresentation.RECHARGE_GENERIC,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[2]));
    // RECHARGE_ALT_1: A location for recharging a vehicle, variant 1.
    symbolMap.put(LocationRepresentation.RECHARGE_ALT_1,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[2]));
    // RECHARGE_ALT_2: A location for recharging a vehicle, variant 2.
    symbolMap.put(LocationRepresentation.RECHARGE_ALT_2,
                  loadImage(path + LOCTYPE_REPRESENTATION_SYMBOLS[2]));
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
