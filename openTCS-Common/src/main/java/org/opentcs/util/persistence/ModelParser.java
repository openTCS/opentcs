/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Objects;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.util.persistence.v002.V002PlantModelTO;
import org.opentcs.util.persistence.v002.V002TOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for parsing {@link PlantModelCreationTO}s from/to a file.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ModelParser {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelParser.class);
  /**
   * The charset to use for the reader/writer.
   */
  private static final Charset CHARSET = Charset.forName("UTF-8");

  /**
   * Reads a model from the given file and parses it to a {@link PlantModelCreationTO} instance.
   *
   * @param file The model file to read.
   * @return The parsed {@link PlantModelCreationTO}.
   * @throws IOException If there was an error reading the model file.
   * @throws IllegalArgumentException If there is no parser for the version of the model file.
   */
  public PlantModelCreationTO readModel(File file)
      throws IOException {
    String modelVersion = peekModelVersion(file);

    LOG.debug("File '{}' contains a model version '{}'.", file.getAbsolutePath(), modelVersion);

    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                                                                  CHARSET))) {
      if (Objects.equals(modelVersion, V002TOMapper.VERSION_STRING)) {
        return new V002TOMapper().map(V002PlantModelTO.fromXml(reader));
      }
      else {
        throw new IllegalArgumentException(
            String.format("There is no parser for a model file with version: %s.", modelVersion)
        );
      }
    }
  }

  /**
   * Writes the given {@link PlantModelCreationTO} to the given file.
   *
   * @param model The model.
   * @param file The file to write the model to.
   * @throws IOException If there was an error writing to the model file.
   */
  public void writeModel(PlantModelCreationTO model, File file)
      throws IOException {
    try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                                                                   CHARSET))) {
      V002TOMapper mapper = new V002TOMapper();
      V002PlantModelTO mappedModel = mapper.map(model);
      mappedModel.toXml(writer);
    }
  }

  private String peekModelVersion(File file)
      throws IOException {
    try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                                                                  CHARSET))) {
      return ProbePlantModelTO.fromXml(reader).getVersion();
    }
  }
}
