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
import org.opentcs.data.model.Path;
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.PathConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO;

/**
 * Handles requests related to paths.
 */
public class PathHandler {

  private final InternalTCSObjectService objectService;
  private final PathConverter pathConverter;
  private final KernelExecutorWrapper executorWrapper;
  private final PlantModelService plantModelService;

  /**
   * Creates a new instance.
   *
   * @param objectService Used to retrieve path instances.
   * @param pathConverter Provides methods to convert paths to their web API representation.
   * @param plantModelService Used to update path locks.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public PathHandler(
      InternalTCSObjectService objectService,
      PathConverter pathConverter,
      KernelExecutorWrapper executorWrapper,
      PlantModelService plantModelService
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.pathConverter = requireNonNull(pathConverter, "pathConverter");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
  }

  /**
   * Finds all paths and filters them depending on the given parameters.
   *
   * @param names The names of the paths to be retrieved. If a named path could not be found, it
   * will simply be omitted from the result. Filtering for this parameter is disabled if the
   * provided list is empty.
   * @return A list of paths that match the filter.
   */
  @Nonnull
  public List<PathTO> getPaths(
      @Nonnull
      List<String> names
  ) {
    requireNonNull(names, "names");
    if (names.isEmpty()) {
      // Optimization - With no filter, filtering is not necessary.
      return executorWrapper.callAndWait(
          () -> objectService.stream(Path.class)
              .map(pathConverter::convert)
              .sorted(Comparator.comparing(PathTO::getName))
              .toList()
      );
    }

    return executorWrapper.callAndWait(
        () -> objectService.stream(Path.class)
            .filter(Filters.objectNameMatchesOneOf(names))
            .map(pathConverter::convert)
            .sorted(Comparator.comparing(PathTO::getName))
            .collect(Collectors.toList())
    );
  }

  /**
   * Finds the path with the given name.
   *
   * @param name The name of the requested path.
   * @return A single path with the given name.
   * @throws ObjectUnknownException If a path with the given name does not exist.
   */
  @Nonnull
  public PathTO getPathByName(
      @Nonnull
      String name
  )
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(
        () -> objectService.fetch(Path.class, name)
            .map(pathConverter::convert)
            .orElseThrow(() -> new ObjectUnknownException("Unknown path: " + name))
    );
  }

  /**
   * Updates the locked state of the path with the given name.
   *
   * @param pathName The name of the path to update.
   * @param lockedValue The path's new locked state (a boolean as a string).
   * @throws ObjectUnknownException If a path with the given name could not be found.
   */
  public void updatePathLock(
      @Nonnull
      String pathName,
      String lockedValue
  )
      throws ObjectUnknownException {
    executorWrapper.callAndWait(() -> {
      Path path = objectService.fetch(Path.class, pathName)
          .orElseThrow(() -> new ObjectUnknownException("Unknown path: " + pathName));
      plantModelService.updatePathLock(path.getReference(), Boolean.parseBoolean(lockedValue));
    });
  }
}
