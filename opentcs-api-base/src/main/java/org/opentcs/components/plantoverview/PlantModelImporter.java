// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.plantoverview;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;
import org.opentcs.access.to.model.PlantModelCreationTO;

/**
 * Implementations provide a way to import plant model data that is read from some external source
 * or generated.
 */
public interface PlantModelImporter {

  /**
   * Imports (or generates) plant model data.
   *
   * @return The imported plant model data. May be empty if the user aborted the import.
   * @throws IOException If there was a problem importing plant model data.
   */
  @Nonnull
  Optional<PlantModelCreationTO> importPlantModel()
      throws IOException;

  /**
   * Returns a (localized) short textual description of this importer.
   *
   * @return A (localized) short textual description of this importer.
   */
  @Nonnull
  String getDescription();
}
