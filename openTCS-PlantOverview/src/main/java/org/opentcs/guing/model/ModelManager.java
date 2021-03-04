/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.guing.storage.ModelReader;

/**
 * Manages (loads, persists and keeps) the driving course model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ModelManager {

  /**
   * Returns the current system model.
   *
   * @return The current system model.
   */
  SystemModel getModel();

  /**
   * Creates a new, empty system model.
   */
  void createEmptyModel();

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
  boolean loadModel(@Nullable File modelFile, ModelReader reader);

  /**
   * Imports a model using the given importer.
   *
   * @param importer The importer to be used.
   * @return <code>true</code> if, and only if, a model was successfully imported.
   */
  boolean importModel(@Nonnull PlantModelImporter importer);

  /**
   * Persists the given system model with the kernel.
   *
   * @param portal The kernel client portal providing the list of existing models.
   * @return Whether the model was actually saved.
   */
  boolean persistModel(KernelServicePortal portal);

  /**
   * Persists the given system model in to a file.
   *
   * @param chooseName Whether a dialog to choose a name shall be shown.
   * @return Whether the model was actually saved.
   */
  boolean persistModel(boolean chooseName);

  /**
   * Exports a model using the given exporter.
   *
   * @param exporter The exporter to be used.
   * @return <code>true</code> if, and only if, the model was successfully exported.
   */
  boolean exportModel(@Nonnull PlantModelExporter exporter);

  /**
   * Creates figures and process adapters for all model components in the current system model.
   */
  void restoreModel();

  /**
   * Loads all model objects from the kernel and creates the corresponding
   * figures.
   *
   * @param portal The kernel client portal.
   */
  void restoreModel(KernelServicePortal portal);
}
