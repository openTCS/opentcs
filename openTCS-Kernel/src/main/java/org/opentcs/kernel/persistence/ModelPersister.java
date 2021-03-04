/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.util.Optional;
import javax.annotation.Nullable;
import org.opentcs.kernel.workingset.Model;

/**
 * Provides methods to persist and load models.
 * Only a single model is persisted at a time.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ModelPersister {

  /**
   * Returns the name the persisted model.
   *
   * @return The model name which is optional as there might be no persisted model.
   * @throws IllegalStateException If reading the model name from the model file failed.
   */
  Optional<String> getPersistentModelName()
      throws IllegalStateException;

  /**
   * Find out if there is a persisted model at the moment.
   *
   * @return True if a model is saved.
   */
  boolean hasSavedModel();

  /**
   * Persists a model according to the actual implementation of this method.
   *
   * @param model The model to be persisted.
   * @param modelName The name under which the model is to be saved.
   * If <code>null</code>, the current name of the model will be used.
   * @throws IllegalStateException If persisting the model is not possible for some reason.
   */
  void saveModel(Model model, @Nullable String modelName)
      throws IllegalStateException;

  /**
   * Loads and returns the persisted model or an empty model (with an empty
   * name) if there is no persisted model.
   *
   * @param model The <code>Model</code> instance into which to load the model
   * data.
   * @throws IllegalStateException If loading the model is not possible for some reason.
   */
  void loadModel(Model model)
      throws IllegalStateException;

  /**
   * Remove currently persisted the model.
   * Has no effect if there is no persisted model.
   *
   * @throws IllegalStateException If removing the model with the given name is not possible for
   * some reason.
   */
  void removeModel()
      throws IllegalStateException;
}
