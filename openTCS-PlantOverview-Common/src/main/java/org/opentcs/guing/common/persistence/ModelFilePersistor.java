/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.persistence;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.swing.filechooser.FileFilter;
import org.opentcs.access.to.model.PlantModelCreationTO;

/**
 * Interface to persist a {@link PlantModelCreationTO} to a local file.
 */
public interface ModelFilePersistor {

  /**
   * Persist the model to the given file.
   *
   * @param model The model to be serialized.
   * @param file The file to serialize into.
   * @return {@code true} if, and only if, the model was successfully serialized
   * @throws java.io.IOException If an exception occurs
   */
  boolean serialize(PlantModelCreationTO model, File file)
      throws IOException;

  /**
   * Returns the filter that declares which files are supported with this persistor.
   *
   * @return The filter that declares which files are supported with this persistor
   */
  @Nonnull
  FileFilter getDialogFileFilter();
}
