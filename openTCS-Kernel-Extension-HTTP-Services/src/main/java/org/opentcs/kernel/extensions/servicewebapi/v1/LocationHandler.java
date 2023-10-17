/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Handles requests related to locations.
 */
public class LocationHandler {

  private final PlantModelService plantModelService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param plantModelService Used to retrieve and update location instances.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public LocationHandler(PlantModelService plantModelService,
                         KernelExecutorWrapper executorWrapper) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  /**
   * Updates the locked state of the location with the given name.
   *
   * @param locationName The name of the location to update.
   * @param lockedValue The location's new locked state (a boolean as a string).
   * @throws ObjectUnknownException If a location with the given name could not be found.
   */
  public void updateLocationLock(@Nonnull String locationName, String lockedValue)
      throws ObjectUnknownException {
    executorWrapper.callAndWait(() -> {
      Location location = plantModelService.fetchObject(Location.class, locationName);
      if (location == null) {
        throw new ObjectUnknownException("Unknown location: " + locationName);
      }

      plantModelService.updateLocationLock(location.getReference(),
                                           Boolean.parseBoolean(lockedValue));
    });
  }
}
