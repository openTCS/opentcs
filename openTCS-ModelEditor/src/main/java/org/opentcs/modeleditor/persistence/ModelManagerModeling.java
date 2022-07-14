/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.persistence;

import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.guing.common.persistence.ModelFileReader;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Manages (loads, saves and keeps) the driving course model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ModelManagerModeling
    extends ModelManager {

  /**
   * Shows a dialog to select a model and loads it.
   *
   * @param modelFile The nullable model file to be loaded. If it
   * is not present a dialog to select a file will be shown.
   * @return <code>true</code> if, and only if, a model was successfully
   * loaded.
   */
  boolean loadModel(@Nullable File modelFile);

  /**
   * Shows a dialog to select a model and loads it.
   *
   * @param modelFile The nullable model file to be loaded. If it
   * is not present a dialog to select a file will be shown.
   * @param reader The reader which reads and parses the file.
   * @return <code>true</code> if, and only if, a model was successfully
   * loaded.
   */
  boolean loadModel(@Nullable File modelFile, ModelFileReader reader);

  /**
   * Imports a model using the given importer.
   *
   * @param importer The importer to be used.
   * @return <code>true</code> if, and only if, a model was successfully imported.
   */
  boolean importModel(@Nonnull PlantModelImporter importer);

  /**
   * Uploads the given system model to the kernel.
   *
   * @param portal The kernel client portal to upload the model to.
   * @return Whether the model was actually uploaded.
   */
  boolean uploadModel(KernelServicePortal portal);

  /**
   * Exports a model using the given exporter.
   *
   * @param exporter The exporter to be used.
   * @return <code>true</code> if, and only if, the model was successfully exported.
   */
  boolean exportModel(@Nonnull PlantModelExporter exporter);
}
