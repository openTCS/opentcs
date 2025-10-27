// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import org.semver4j.Semver;
import org.semver4j.SemverException;

/**
 * The parser for V6 models.
 */
public class V6ModelParser {

  /**
   * The maximum supported schema version for model files.
   */
  private static final Semver V6_SUPPORTED_VERSION = new Semver(V6PlantModelTO.VERSION_STRING);

  /**
   * Creates a new instance.
   */
  public V6ModelParser() {
  }

  /**
   * Reads a model with the given reader and parses it to a {@link V6PlantModelTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link V6PlantModelTO}.
   * @throws IOException If there was an error reading the model.
   */
  public V6PlantModelTO readRaw(
      @Nonnull
      Reader reader,
      @Nonnull
      String modelVersion
  )
      throws IOException {
    requireNonNull(reader, "reader");
    requireNonNull(modelVersion, "modelVersion");

    Semver fileVersionNumber;
    try {
      fileVersionNumber = new Semver(modelVersion);
    }
    catch (SemverException e) {
      throw new IOException(e);
    }

    if (fileVersionNumber.getMajor() == V6_SUPPORTED_VERSION.getMajor()
        && fileVersionNumber.isLowerThanOrEqualTo(V6_SUPPORTED_VERSION)) {
      return V6PlantModelTO.fromXml(reader);
    }
    else {
      throw new IllegalArgumentException(
          String.format("There is no parser for a model file with version: %s.", modelVersion)
      );
    }
  }
}
