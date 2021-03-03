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
import javax.annotation.Nullable;
import org.opentcs.access.Kernel;
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
   * Persists the given system model with the kernel.
   *
   * @param kernel The kernel providing the list of existing models.
   * @return Whether the model was actually saved.
   */
  boolean persistModel(Kernel kernel);

  /**
   * Persists the given system model in to a file.
   *
   * @param chooseName Whether a dialog to choose a name shall be shown.
   * @return Whether the model was actually saved.
   */
  boolean persistModel(boolean chooseName);

  void restoreModel();

  /**
   * Loads all model objects from the kernel and creates the corresponding
   * figures.
   *
   * @param kernel The kernel.
   */
  void restoreModel(Kernel kernel);
}
