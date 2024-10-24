// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.plantoverview;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import org.opentcs.access.to.model.PlantModelCreationTO;

/**
 * Implementations provide a way to export plant model data, for instance to write it to a file in a
 * third-party format or to a database.
 */
public interface PlantModelExporter {

  /**
   * Exports the given plant model data.
   *
   * @param model The plant model data to be exported.
   * @throws IOException If there was a problem exporting plant model data.
   */
  void exportPlantModel(
      @Nonnull
      PlantModelCreationTO model
  )
      throws IOException;

  /**
   * Returns a (localized) short textual description of this importer.
   *
   * @return A (localized) short textual description of this importer.
   */
  @Nonnull
  String getDescription();
}
