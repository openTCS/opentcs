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
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.LocationType;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.LocationTypeConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTypeTO;

/**
 * Handles requests related to location types.
 */
public class LocationTypeHandler {

  private final InternalTCSObjectService objectService;
  private final LocationTypeConverter locationTypeConverter;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param objectService Used to retrieve location type instances.
   * @param locationTypeConverter Provides methods to convert location types to their web API
   * representation.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public LocationTypeHandler(
      InternalTCSObjectService objectService,
      LocationTypeConverter locationTypeConverter,
      KernelExecutorWrapper executorWrapper
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.locationTypeConverter = requireNonNull(locationTypeConverter, "locationTypeConverter");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  /**
   * Finds all location types and filters them depending on the given parameters.
   *
   * @param names The names of the location types to be retrieved. If a named location type could
   * not be found, it will simply be omitted from the result. Filtering for this parameter is
   * disabled if the provided list is empty.
   * @return A list of location types that match the filter.
   */
  @Nonnull
  public List<LocationTypeTO> getLocationTypes(
      @Nonnull
      List<String> names
  ) {
    requireNonNull(names, "names");
    if (names.isEmpty()) {
      // Optimization - With no filter, filtering is not necessary.
      return executorWrapper.callAndWait(
          () -> objectService.stream(LocationType.class)
              .map(locationTypeConverter::convert)
              .sorted(Comparator.comparing(LocationTypeTO::getName))
              .toList()
      );
    }

    return executorWrapper.callAndWait(
        () -> objectService.stream(LocationType.class)
            .filter(Filters.objectNameMatchesOneOf(names))
            .map(locationTypeConverter::convert)
            .sorted(Comparator.comparing(LocationTypeTO::getName))
            .collect(Collectors.toList())
    );
  }

  /**
   * Finds the location type with the given name.
   *
   * @param name The name of the requested location type.
   * @return A single location type with the given name.
   * @throws ObjectUnknownException If a location type with the given name does not exist.
   */
  @Nonnull
  public LocationTypeTO getLocationTypeByName(
      @Nonnull
      String name
  )
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(
        () -> objectService.fetch(LocationType.class, name)
            .map(locationTypeConverter::convert)
            .orElseThrow(() -> new ObjectUnknownException("Unknown location type: " + name))
    );
  }
}
