/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.util.Optional;
import org.opentcs.access.to.model.PlantModelCreationTO;

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
   * @throws IllegalStateException If persisting the model is not possible for some reason.
   */
  void saveModel(PlantModelCreationTO model)
      throws IllegalStateException;

  /**
   * Reads the model and returns it as a <Code>PlantModelCreationTO</Code>.
   *
   * @return The <Code>PlantModelCreationTO</Code> that contains the data that was read.
   * @throws IllegalStateException If reading the model is not possible for some reason.
   */
  PlantModelCreationTO readModel() throws IllegalStateException;

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
