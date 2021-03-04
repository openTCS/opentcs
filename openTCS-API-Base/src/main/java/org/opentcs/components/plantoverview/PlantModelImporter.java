/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opentcs.access.to.model.PlantModelCreationTO;

/**
 * Implementations provide a way to import plant model data that is read from some external source
 * or generated.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
