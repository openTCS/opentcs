/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.swing.filechooser.FileFilter;
import org.opentcs.guing.model.SystemModel;

/**
 * Interface for classes that persist <code>ModelComponents</code> to local files.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public interface ModelFilePersistor {

  /**
   * Persist a system model.
   *
   * @param systemModel The system model to be serialized.
   * @param modelName The model name
   * @param file The file to serialize into
   * @param ignoreError Whether errors should be ignored
   * @return <code>true</code> if, and only if, the model was successfully serialized
   * @throws java.io.IOException If an exception occurs
   */
  public boolean serialize(SystemModel systemModel, String modelName, File file, boolean ignoreError)
      throws IOException;

  /**
   * Returns the filter that declares which files are supported with this persistor.
   *
   * @return The filter that declares which files are supported with this persistor
   */
  @Nonnull
  public FileFilter getDialogFileFilter();
}
