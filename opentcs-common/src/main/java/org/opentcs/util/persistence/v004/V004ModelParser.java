/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v004;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * The parser for V004 models.
 */
public class V004ModelParser {

  /**
   * Creates a new instance.
   */
  public V004ModelParser() {
  }

  /**
   * Reads a model with the given reader and parses it to a {@link V004PlantModelTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link V004PlantModelTO}.
   * @throws IOException If there was an error reading the model.
   */
  public V004PlantModelTO readRaw(Reader reader, String modelVersion)
      throws IOException {
    if (Objects.equals(modelVersion, V004PlantModelTO.VERSION_STRING)) {
      return V004PlantModelTO.fromXml(reader);
    }
    else {
      throw new IllegalArgumentException(
          String.format("There is no parser for a model file with version: %s.", modelVersion)
      );
    }
  }
}
