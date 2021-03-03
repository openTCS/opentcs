/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.IOException;
import java.util.Set;
import org.opentcs.kernel.workingset.Model;

/**
 * Provides methods to persist models, list persisted models and load them.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ModelPersister {
  /**
   * Returns the names of all available models.
   *
   * @return The names of all available models. If no models are available, the
   * returned list will be empty.
   */
  Set<String> getModelNames();
  /**
   * Persists a model according to the actual implementation of this method.
   *
   * @param model The model to be persisted.
   * @param modelName The name under which the model is to be saved.
   * @param overwrite If <code>true</code>, an existing model with the given
   * name will be overwritten; if <code>false</code> and a model with the given
   * name exists, an IOException will be thrown.
   * @throws IOException If persisting the model is not possible for some
   * reason.
   */
  void saveModel(Model model, String modelName, boolean overwrite)
  throws IOException;
  
  /**
   * Loads and returns the model with the given name.
   *
   * @param modelName The name of the model to be returned.
   * @param model The <code>Model</code> instance into which to load the model
   * data.
   * @throws IOException If loading the model is not possible for some reason.
   */
  void loadModel(String modelName, Model model)
  throws IOException;
  
  /**
   * Removes the model with the given name.
   *
   * @param modelName The name of the model to be removed.
   * @throws IOException If removing the model with the given name is not
   * possible for some reason.
   */
  void removeModel(String modelName)
  throws IOException;
}
