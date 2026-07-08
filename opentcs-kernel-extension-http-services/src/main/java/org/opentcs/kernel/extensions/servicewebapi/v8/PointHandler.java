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
import org.opentcs.data.model.Point;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.PointConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PointTO;

/**
 * Handles requests related to points.
 */
public class PointHandler {

  private final InternalTCSObjectService objectService;
  private final PointConverter pointConverter;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param objectService Used to retrieve point instances.
   * @param pointConverter Provides methods to convert points to their web API representation.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public PointHandler(
      InternalTCSObjectService objectService,
      PointConverter pointConverter,
      KernelExecutorWrapper executorWrapper
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.pointConverter = requireNonNull(pointConverter, "pointConverter");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  /**
   * Finds all points and filters them depending on the given parameters.
   *
   * @param names The names of the points to be retrieved. If a named point could not be found, it
   * will simply be omitted from the result. Filtering for this parameter is disabled if the
   * provided list is empty.
   * @return A list of points that match the filter.
   */
  @Nonnull
  public List<PointTO> getPoints(
      @Nonnull
      List<String> names
  ) {
    requireNonNull(names, "names");
    if (names.isEmpty()) {
      // Optimization - With no filter, filtering is not necessary.
      return executorWrapper.callAndWait(
          () -> objectService.stream(Point.class)
              .map(pointConverter::convert)
              .sorted(Comparator.comparing(PointTO::getName))
              .toList()
      );
    }

    return executorWrapper.callAndWait(
        () -> objectService.stream(Point.class)
            .filter(Filters.objectNameMatchesOneOf(names))
            .map(pointConverter::convert)
            .sorted(Comparator.comparing(PointTO::getName))
            .collect(Collectors.toList())
    );
  }

  /**
   * Finds the point with the given name.
   *
   * @param name The name of the requested point.
   * @return A single point with the given name.
   * @throws ObjectUnknownException If a point with the given name does not exist.
   */
  @Nonnull
  public PointTO getPointByName(
      @Nonnull
      String name
  )
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(
        () -> objectService.fetch(Point.class, name)
            .map(pointConverter::convert)
            .orElseThrow(() -> new ObjectUnknownException("Unknown point: " + name))
    );
  }
}
