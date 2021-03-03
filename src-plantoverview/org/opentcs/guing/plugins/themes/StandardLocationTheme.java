/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.themes;

import java.util.EnumMap;
import java.util.Map;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.util.gui.plugins.LocationTheme;

/**
 * Standard implementation of <code>LocationTheme</code>.
 *
 * @author Philipp Seifert (Fraunhofer IML)
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
  };
  /**
   * A map of property values to image file names.
   */
  private final Map<LocationRepresentation, String> symbolMap
      = new EnumMap<>(LocationRepresentation.class);

  /**
   * Creates a new instance.
   */
  public StandardLocationTheme() {
    initSymbolMap();
  }

  @Override
  public String getImagePathFor(LocationRepresentation representation) {
    String relativePath = symbolMap.get(representation);
    return (relativePath == null) ? null : path + relativePath;
  }

  @Override
  public String getName() {
    return "Standard location theme";
  }

  private void initSymbolMap() {
    // LOAD_TRANSFER_GENERIC: A generic location for vehicle load transfers.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_GENERIC,
                  LOCTYPE_REPRESENTATION_SYMBOLS[0]);
    // LOAD_TRANSFER_ALT_1: A location for vehicle load transfers, variant 1.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_1,
                  LOCTYPE_REPRESENTATION_SYMBOLS[0]);
    // LOAD_TRANSFER_ALT_2: A location for vehicle load transfers, variant 2.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_2,
                  LOCTYPE_REPRESENTATION_SYMBOLS[0]);
    // LOAD_TRANSFER_ALT_3: A location for vehicle load transfers, variant 3.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_3,
                  LOCTYPE_REPRESENTATION_SYMBOLS[0]);
    // LOAD_TRANSFER_ALT_4: A location for vehicle load transfers, variant 4.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_4,
                  LOCTYPE_REPRESENTATION_SYMBOLS[0]);
    // LOAD_TRANSFER_ALT_5: A location for vehicle load transfers, variant 5.
    symbolMap.put(LocationRepresentation.LOAD_TRANSFER_ALT_5,
                  LOCTYPE_REPRESENTATION_SYMBOLS[0]);

    // WORKING_GENERIC: A location for some generic processing, generic variant.
    symbolMap.put(LocationRepresentation.WORKING_GENERIC,
                  LOCTYPE_REPRESENTATION_SYMBOLS[1]);
    // WORKING_ALT_1: A location for some generic processing, variant 1.
    symbolMap.put(LocationRepresentation.WORKING_ALT_1,
                  LOCTYPE_REPRESENTATION_SYMBOLS[1]);
    // WORKING_ALT_2: A location for some generic processing, variant 2.
    symbolMap.put(LocationRepresentation.WORKING_ALT_2,
                  LOCTYPE_REPRESENTATION_SYMBOLS[1]);

    // RECHARGE_GENERIC: A location for recharging a vehicle, generic variant.
    symbolMap.put(LocationRepresentation.RECHARGE_GENERIC,
                  LOCTYPE_REPRESENTATION_SYMBOLS[2]);
    // RECHARGE_ALT_1: A location for recharging a vehicle, variant 1.
    symbolMap.put(LocationRepresentation.RECHARGE_ALT_1,
                  LOCTYPE_REPRESENTATION_SYMBOLS[2]);
    // RECHARGE_ALT_2: A location for recharging a vehicle, variant 2.
    symbolMap.put(LocationRepresentation.RECHARGE_ALT_2,
                  LOCTYPE_REPRESENTATION_SYMBOLS[2]);
  }
}
