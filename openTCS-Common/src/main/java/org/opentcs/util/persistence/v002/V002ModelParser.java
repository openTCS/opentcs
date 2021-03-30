/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v002;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * The parser for V002 models.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class V002ModelParser {

  /**
   * The file format version this parser works with.
   */
  public static final String VERSION_STRING = "0.0.2";

  /**
   * Reads a model with the given reader and parses it to a {@link V002PlantModelTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link V002PlantModelTO}.
   * @throws IOException If there was an error reading the model.
   */
  public V002PlantModelTO readRaw(Reader reader, String modelVersion)
      throws IOException {

    if (Objects.equals(modelVersion, VERSION_STRING)) {
      return V002PlantModelTO.fromXml(reader);
    }
    else {
      throw new IllegalArgumentException(
          String.format("There is no parser for a model file with version: %s.", modelVersion)
      );
    }
  }
}
