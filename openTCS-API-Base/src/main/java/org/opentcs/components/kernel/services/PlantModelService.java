/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import java.util.Map;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods concerning the plant model.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface PlantModelService
    extends TCSObjectService {

  /**
   * Creates a new plant model with the objects described in the given transfer object.
   * Implicitly saves/persists the new plant model.
   *
   * @param to The transfer object describing the plant model objects to be created.
   * @throws ObjectUnknownException If any referenced object does not exist.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @throws IllegalStateException If there was a problem persisting the model.
   */
  void createPlantModel(PlantModelCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, KernelRuntimeException,
             IllegalStateException;

  /**
   * Returns the name of the model that is currently loaded in the kernel.
   *
   * @return The name of the currently loaded model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Use {@link #getModelName()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  String getLoadedModelName()
      throws KernelRuntimeException;

  /**
   * Returns the name of the model that is currently loaded in the kernel.
   *
   * @return The name of the currently loaded model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  String getModelName()
      throws KernelRuntimeException;

  /**
   * Returns the model's properties.
   *
   * @return The model's properties.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  Map<String, String> getModelProperties()
      throws KernelRuntimeException;

  /**
   * Returns the name of the model that is stored and could be loaded by the kernel.
   *
   * @return The name of the model, or {@code null} if there is no model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @throws IllegalStateException If retrieving the model name was not possible.
   * @deprecated Fetching a persistent model's name will not be supported any more, as the model
   * would now already be loaded, anyway.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  String getPersistentModelName()
      throws KernelRuntimeException, IllegalStateException;
}
