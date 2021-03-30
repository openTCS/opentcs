/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules;

import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.strategies.basic.scheduling.ReservationPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if the resources a client may allocate are part of a
 * {@link Block.Type#SINGLE_VEHICLE_ONLY} block and whether the expanded resources are all available
 * to the client.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SingleVehicleBlockModule
    implements Scheduler.Module {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SingleVehicleBlockModule.class);
  /**
   * The reservation pool.
   */
  private final ReservationPool reservationPool;
  /**
   * The plant model service.
   */
  private final InternalPlantModelService plantModelService;
  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * Whether this module is initialized.
   */
  private boolean initialized;

  @Inject
  public SingleVehicleBlockModule(@Nonnull ReservationPool reservationPool,
                                  @Nonnull InternalPlantModelService plantModelService,
                                  @Nonnull @GlobalSyncObject Object globalSyncObject) {
    this.reservationPool = requireNonNull(reservationPool, "reservationPool");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

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

    initialized = false;
  }

  @Override
  public void claim(Scheduler.Client client,
                    List<Set<TCSResource<?>>> claim) {
  }

  @Override
  public void unclaim(Scheduler.Client client) {
  }

  @Override
  public void setAllocationState(Scheduler.Client client,
                                 Set<TCSResource<?>> alloc,
                                 List<Set<TCSResource<?>>> remainingClaim) {
  }

  @Override
  public boolean mayAllocate(Scheduler.Client client,
                             Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (globalSyncObject) {
      Set<Block> blocks = filterBlocksContainingResources(resources,
                                                          Block.Type.SINGLE_VEHICLE_ONLY);

      if (blocks.isEmpty()) {
        LOG.debug("{}: No blocks to be checked, allocation allowed.", client.getId());
        return true;
      }

      Set<TCSResource<?>> resourcesExpanded = expandResources(resources);
      resourcesExpanded = filterRelevantResources(resourcesExpanded, blocks);

      LOG.debug("{}: Checking resource availability: {}", client.getId(), resources);
      if (!reservationPool.resourcesAvailableForUser(resourcesExpanded, client)) {
        LOG.debug("{}: Resources unavailable.", client.getId());
        return false;
      }

      LOG.debug("{}: Resources available, allocation allowed.", client.getId());
      return true;
    }
  }

  @Override
  public void prepareAllocation(Scheduler.Client client,
                                Set<TCSResource<?>> resources) {
  }

  @Override
  public boolean hasPreparedAllocation(Scheduler.Client client,
                                       Set<TCSResource<?>> resources) {
    return true;
  }

  @Override
  public void allocationReleased(Scheduler.Client client,
                                 Set<TCSResource<?>> resources) {
  }

  private Set<Block> filterBlocksContainingResources(Set<TCSResource<?>> resources,
                                                     Block.Type type) {
    Set<Block> result = new HashSet<>();
    Set<Block> blocks = plantModelService.fetchObjects(Block.class,
                                                       block -> block.getType() == type);
    for (TCSResource<?> resource : resources) {
      for (Block block : blocks) {
        if (block.getMembers().contains(resource.getReference())) {
          result.add(block);
        }
      }
    }
    return result;
  }

  private Set<TCSResource<?>> filterRelevantResources(Set<TCSResource<?>> resources,
                                                      Set<Block> blocks) {
    Set<TCSResourceReference<?>> blockResources = blocks.stream()
        .flatMap(block -> block.getMembers().stream())
        .collect(Collectors.toSet());

    return resources.stream()
        .filter(resource -> blockResources.contains(resource.getReference()))
        .collect(Collectors.toSet());
  }

  /**
   * Returns the given set of resources after expansion (by resolution of blocks, for instance) by
   * the kernel.
   *
   * @param resources The set of resources to be expanded.
   * @return The given set of resources after expansion (by resolution of
   * blocks, for instance) by the kernel.
   */
  private Set<TCSResource<?>> expandResources(Set<TCSResource<?>> resources) {
    requireNonNull(resources, "resources");
    // Build a set of references
    Set<TCSResourceReference<?>> refs = resources.stream()
        .map((resource) -> resource.getReference())
        .collect(Collectors.toSet());
    // Let the kernel expand the resources for us.
    Set<TCSResource<?>> result = plantModelService.expandResources(refs);
    LOG.debug("Set {} expanded to {}", resources, result);
    return result;
  }

}
