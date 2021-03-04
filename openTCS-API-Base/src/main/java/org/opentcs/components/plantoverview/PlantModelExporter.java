/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.opentcs.access.to.model.PlantModelCreationTO;

/**
 * Implementations provide a way to export plant model data, for instance to write it to a file in a
 * third-party format or to a database.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface PlantModelExporter {

  /**
   * Exports the given plant model data.
   *
   * @param model The plant model data to be exported.
   * @throws IOException If there was a problem exporting plant model data.
   */
  void exportPlantModel(@Nonnull PlantModelCreationTO model)
      throws IOException;

  /**
   * Returns a (localized) short textual description of this importer.
   *
   * @return A (localized) short textual description of this importer.
   */
  @Nonnull
  String getDescription();
}
