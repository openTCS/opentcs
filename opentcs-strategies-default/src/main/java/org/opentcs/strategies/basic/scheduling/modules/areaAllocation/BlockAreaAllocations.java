// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.GeometryCollection;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;

/**
 * Ensures that block areas are allocated by a single vehicle at a time. (With a block are being
 * the union of the areas of that block's members.)
 */
public class BlockAreaAllocations
    implements
      Lifecycle {

  private final AreaProvider areaProvider;
  private final InternalPlantModelService plantModelService;
  private final BlockStore blockStore = new BlockStore();
  private boolean initialized = false;

  /**
   * Creates a new instance.
   *
   * @param areaProvider Provides areas related to resources.
   * @param plantModelService The plant model service to use.
   */
  @Inject
  public BlockAreaAllocations(
      AreaProvider areaProvider,
      InternalPlantModelService plantModelService
  ) {
    this.areaProvider = requireNonNull(areaProvider, "areaProvider");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    blockStore.init();

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    blockStore.clear();

    initialized = false;
  }

  /**
   * Checks if the given vehicle is allowed to allocate the given resources.
   * <p>
   * This method considers:
   * </p>
   * <ol>
   * <li>The areas associated with the given resource set and the "expanded areas" of all blocks
   * which the given resources are members of.</li>
   * <li>The areas associated with resources allocated by other vehicles (including the
   * "expanded areas" of blocks the other vehicles may occupy).</li>
   * </ol>
   * Allocation of the given resources is allowed only if there are no intersections between the two
   * areas mentioned above.
   *
   * @param vehicleRef The vehicle reference.
   * @param envelopeKey The envelope key.
   * @param resources The set of resources.
   * @return {@code true}, if allocation of the given resources is allowed, otherwise {@code false}.
   */
  public boolean isAreaAllocationAllowed(
      @Nonnull
      TCSObjectReference<Vehicle> vehicleRef,
      @Nonnull
      String envelopeKey,
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(vehicleRef, "vehicleRef");
    requireNonNull(envelopeKey, "envelopeKey");
    requireNonNull(resources, "resources");

    Set<Block> requestedBlocks = blockStore.getBlocksContainingResources(resources);

    Set<Vehicle> otherVehicles = plantModelService.fetchObjects(
        Vehicle.class,
        vehicle -> !Objects.equals(vehicle.getName(), vehicleRef.getName())
            && !vehicle.getAllocatedResources().isEmpty()
    );

    for (Vehicle otherVehicle : otherVehicles) {
      Set<Block> occupiedBlocks = blockStore.getBlocksOccupiedBy(otherVehicle);

      // Skip checks of combinations where none of the vehicles are in any block, as these checks
      // are expected to be performed elsewhere.
      if (requestedBlocks.isEmpty() && occupiedBlocks.isEmpty()) {
        continue;
      }

      // Skip checks of combinations where the requested blocks and the blocks occupied by the
      // current (other) vehicle are identical (i.e. none of the two vehicles request or occupy
      // any block "exclusively"). For vehicles requesting an allocation inside a block that another
      // vehicle has already allocated resources in, these checks are expected to be performed
      // elsewhere.
      if (Objects.equals(requestedBlocks, occupiedBlocks)) {
        continue;
      }

      // Without an envelope key there are no areas to be compared.
      // XXX Remove this check with openTCS 7.0 once envelope key became non-null.
      if (otherVehicle.getEnvelopeKey() == null) {
        continue;
      }

      // Determine the blocks that the current combination of vehicles share. Resources in these
      // blocks are ignored, as their allocation is expected to be handled elsewhere.
      Set<Block> sharedBlocks = setIntersection(requestedBlocks, occupiedBlocks);

      // Expand the requested resources, but filter the ones that are in any "shared" blocks.
      Set<TCSResource<?>> expandedRequestedResources
          = expandResourcesIgnoringBlocks(resources, sharedBlocks);

      // Expand resources allocated by the other vehicle, but filter the ones that are in any
      // "shared" blocks.
      Set<TCSResource<?>> expandedOccupiedResources
          = expandResourcesIgnoringBlocks(otherVehicle.getAllocatedResources(), sharedBlocks);

      // Determine the areas effectively requested and occupied by both vehicles.
      GeometryCollection requestedArea
          = areaProvider.getAreas(envelopeKey, expandedRequestedResources);
      GeometryCollection occupiedArea
          = areaProvider.getAreas(otherVehicle.getEnvelopeKey(), expandedOccupiedResources);

      if (requestedArea.intersects(occupiedArea)) {
        return false;
      }
    }

    return true;
  }

  private <T> Set<T> setIntersection(Set<T> setA, Set<T> setB) {
    Set<T> result = new HashSet<>(setA);
    result.retainAll(setB);
    return result;
  }

  private Set<TCSResource<?>> expandResourcesIgnoringBlocks(
      Set<TCSResource<?>> resources,
      Set<Block> blocksToIgnore
  ) {
    Set<TCSResource<?>> result = new HashSet<>(
        plantModelService.expandResources(
            resources.stream()
                .map(TCSResource::getReference)
                .collect(Collectors.toSet())
        )
    );
    result.removeIf(resource -> containedInBlocks(resource, blocksToIgnore));
    return result;
  }

  private Set<TCSResource<?>> expandResourcesIgnoringBlocks(
      List<Set<TCSResourceReference<?>>> resources,
      Set<Block> blocksToIgnore
  ) {
    Set<TCSResource<?>> result = new HashSet<>(
        plantModelService.expandResources(
            resources.stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
        )
    );
    result.removeIf(resource -> containedInBlocks(resource, blocksToIgnore));
    return result;
  }

  private boolean containedInBlocks(TCSResource<?> resource, Set<Block> blocks) {
    return blocks.stream().anyMatch(block -> block.getMembers().contains(resource.getReference()));
  }

  /**
   * A local store for information on blocks and their members.
   */
  private class BlockStore {

    private final Map<Block, Set<TCSObject<?>>> resourcesByBlocks = new HashMap<>();

    BlockStore() {
    }

    public void init() {
      for (Block block : plantModelService.fetchObjects(Block.class)) {
        resourcesByBlocks.put(
            block,
            block.getMembers().stream()
                .map(this::toTcsObject)
                .collect(Collectors.toUnmodifiableSet())
        );
      }
    }

    public void clear() {
      resourcesByBlocks.clear();
    }

    public Set<Block> getBlocksOccupiedBy(Vehicle vehicle) {
      return getBlocksContainingResourceReferences(
          vehicle.getAllocatedResources().stream()
              .flatMap(Set::stream)
              .collect(Collectors.toSet())
      );
    }

    public Set<Block> getBlocksContainingResources(Set<TCSResource<?>> resources) {
      return resourcesByBlocks.entrySet().stream()
          .filter(entry -> !Collections.disjoint(entry.getValue(), resources))
          .map(Map.Entry::getKey)
          .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private TCSObject<?> toTcsObject(TCSResourceReference<?> resourceRef) {
      if (resourceRef.getReferentClass() == Point.class) {
        return plantModelService.fetchObject(Point.class, (TCSObjectReference<Point>) resourceRef);
      }
      else if (resourceRef.getReferentClass() == Path.class) {
        return plantModelService.fetchObject(Path.class, (TCSObjectReference<Path>) resourceRef);
      }
      else if (resourceRef.getReferentClass() == Location.class) {
        return plantModelService.fetchObject(
            Location.class,
            (TCSObjectReference<Location>) resourceRef
        );
      }
      else {
        throw new IllegalArgumentException("Unsupported resource type: " + resourceRef);
      }
    }

    private Set<Block> getBlocksContainingResourceReferences(
        Set<TCSResourceReference<?>> resources
    ) {
      return resourcesByBlocks.keySet().stream()
          .filter(block -> !Collections.disjoint(block.getMembers(), resources))
          .collect(Collectors.toSet());
    }
  }
}
