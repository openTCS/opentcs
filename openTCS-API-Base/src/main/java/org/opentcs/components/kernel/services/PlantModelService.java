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
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PlantModel;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods concerning the plant model.
 */
public interface PlantModelService
    extends TCSObjectService {

  /**
   * Returns a representation of the plant model's current state.
   *
   * @return The complete plant model.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  PlantModel getPlantModel()
      throws KernelRuntimeException;

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
   * Updates a location's lock state.
   *
   * @param ref A reference to the location to be updated.
   * @param locked Indicates whether the location is to be locked ({@code true}) or unlocked
   * ({@code false}).
   * @throws ObjectUnknownException If the referenced location does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updateLocationLock(TCSObjectReference<Location> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Updates a path's lock state.
   *
   * @param ref A reference to the path to be updated.
   * @param locked Indicates whether the path is to be locked ({@code true}) or unlocked
   * ({@code false}).
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void updatePathLock(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException {
  }
}
