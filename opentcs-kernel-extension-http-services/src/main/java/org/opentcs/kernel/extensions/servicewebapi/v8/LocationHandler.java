// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.LocationConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTO;

/**
 * Handles requests related to locations.
 */
public class LocationHandler {

  private final InternalTCSObjectService objectService;
  private final LocationConverter locationConverter;
  private final PlantModelService plantModelService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param objectService Used to retrieve location instances.
   * @param locationConverter Provides methods to convert locations to their web API representation.
   * @param plantModelService Used to retrieve and update location instances.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public LocationHandler(
      InternalTCSObjectService objectService,
      LocationConverter locationConverter,
      PlantModelService plantModelService,
      KernelExecutorWrapper executorWrapper
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.locationConverter = requireNonNull(locationConverter, "locationConverter");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  /**
   * Finds all locations and filters them depending on the given parameters.
   *
   * @param names The names of the locations to be retrieved. If a named location could not be
   * found, it will simply be omitted from the result. Filtering for this parameter is disabled if
   * the provided list is empty.
   * @return A list of locations that match the filter.
   */
  @Nonnull
  public List<LocationTO> getLocations(
      @Nonnull
      List<String> names
  ) {
    requireNonNull(names, "names");
    if (names.isEmpty()) {
      // Optimization - With no filter, filtering is not necessary.
      return executorWrapper.callAndWait(
          () -> objectService.stream(Location.class)
              .map(locationConverter::convert)
              .sorted(Comparator.comparing(LocationTO::getName))
              .toList()
      );
    }

    return executorWrapper.callAndWait(
        () -> objectService.stream(Location.class)
            .filter(Filters.objectNameMatchesOneOf(names))
            .map(locationConverter::convert)
            .sorted(Comparator.comparing(LocationTO::getName))
            .collect(Collectors.toList())
    );
  }

  /**
   * Finds the location with the given name.
   *
   * @param name The name of the requested location.
   * @return A single location with the given name.
   * @throws ObjectUnknownException If a location with the given name does not exist.
   */
  @Nonnull
  public LocationTO getLocationByName(
      @Nonnull
      String name
  )
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(
        () -> objectService.fetch(Location.class, name)
            .map(locationConverter::convert)
            .orElseThrow(() -> new ObjectUnknownException("Unknown location: " + name))
    );
  }

  /**
   * Updates the locked state of the location with the given name.
   *
   * @param locationName The name of the location to update.
   * @param lockedValue The location's new locked state (a boolean as a string).
   * @throws ObjectUnknownException If a location with the given name could not be found.
   */
  public void updateLocationLock(
      @Nonnull
      String locationName,
      String lockedValue
  )
      throws ObjectUnknownException {
    executorWrapper.callAndWait(() -> {
      Location location = plantModelService.fetch(Location.class, locationName)
          .orElseThrow(() -> new ObjectUnknownException("Unknown location: " + locationName));

      plantModelService.updateLocationLock(
          location.getReference(),
          Boolean.parseBoolean(lockedValue)
      );
    });
  }
}
