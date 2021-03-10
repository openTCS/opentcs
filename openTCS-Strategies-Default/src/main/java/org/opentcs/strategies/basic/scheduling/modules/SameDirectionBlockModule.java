/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.components.kernel.Scheduler;
import static org.opentcs.components.kernel.Scheduler.PROPKEY_BLOCK_ENTRY_DIRECTION;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.TCSResource;
import org.opentcs.strategies.basic.scheduling.ReservationPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if the resources a client may allocate are part of a
 * {@link Block.Type#SAME_DIRECTION_ONLY} block and whether the client is allowed to drive along
 * the block in the requested direction.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SameDirectionBlockModule
    implements Scheduler.Module {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SameDirectionBlockModule.class);
  /**
   * The reservation pool.
   */
  private final ReservationPool reservationPool;
  /**
   * The plant model service.
   */
  private final InternalPlantModelService plantModelService;
  /**
   * The permissions for all {@link Block.Type#SAME_DIRECTION_ONLY} blocks in a plant model.
   */
  private final Map<Block, BlockPermission> permissions = new HashMap<>();
  /**
   * Whether this module is initialized.
   */
  private boolean initialized;

  @Inject
  public SameDirectionBlockModule(@Nonnull ReservationPool reservationPool,
                                  @Nonnull InternalPlantModelService plantModelService) {
    this.reservationPool = requireNonNull(reservationPool, "reservationPool");
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    Set<Block> blocks = plantModelService.fetchObjects(Block.class);
    for (Block block : blocks) {
      if (block.getType() == Block.Type.SAME_DIRECTION_ONLY) {
        permissions.put(block, new BlockPermission(block));
      }
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

    permissions.clear();

    initialized = false;
  }

  @Override
  public void claim(Scheduler.Client client, List<Set<TCSResource<?>>> claim) {
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
  public boolean mayAllocate(Scheduler.Client client, Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (reservationPool) {
      Set<Block> blocks = filterBlocksContainingResources(resources,
                                                          Block.Type.SAME_DIRECTION_ONLY);
      if (blocks.isEmpty()) {
        return true;
      }

      Path path = selectPath(resources);
      if (path == null) {
        // If there's no path in the requested resources the vehicle won't move and already has
        // permission to be in the block(s)
        return true;
      }

      // Other modules may prevented the last allocation. Therefore discard any previous requests.
      discardPreviousRequests();

      String entryDirectionProperty = path.getProperties()
          .getOrDefault(PROPKEY_BLOCK_ENTRY_DIRECTION, path.getName());

      boolean mayAllocate = true;
      for (Block block : blocks) {
        mayAllocate &= permissions.get(block).enqueueRequest(client, entryDirectionProperty);
      }

      return mayAllocate;
    }
  }

  @Override
  public void prepareAllocation(Scheduler.Client client, Set<TCSResource<?>> resources) {
    permissions.values().forEach(permission -> permission.permitPendingRequests());
  }

  @Override
  public boolean hasPreparedAllocation(Scheduler.Client client, Set<TCSResource<?>> resources) {
    return !permissions.values().stream()
        .filter(permission -> permission.hasPendingRequests())
        .findAny()
        .isPresent();
  }

  @Override
  public void allocationReleased(Scheduler.Client client, Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    synchronized (reservationPool) {
      for (Map.Entry<Block, BlockPermission> entry : permissions.entrySet()) {
        Block block = entry.getKey();
        BlockPermission permission = entry.getValue();

        if (!permission.isPermissionGranted(client)) {
          continue;
        }

        if (blockResourcesAllocatedByClient(block, client)) {
          continue;
        }

        // The client released resources and does no longer hold any resources of this block.
        // We don't need permissions any more.
        permission.removePermissionFor(client);
      }
    }
  }

  private void discardPreviousRequests() {
    permissions.values().forEach(permission -> permission.clearPendingRequests());
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

  @Nullable
  private Path selectPath(Set<TCSResource<?>> resources) {
    for (TCSResource<?> resource : resources) {
      if (resource instanceof Path) {
        return ((Path) resource);
      }
    }

    return null;
  }

  private boolean blockResourcesAllocatedByClient(Block block, Scheduler.Client client) {
    Set<Block> clientBlocks
        = filterBlocksContainingResources(reservationPool.allocatedResources(client),
                                          Block.Type.SAME_DIRECTION_ONLY);
    return clientBlocks.contains(block);
  }

  /**
   * Manages the clients that are permitted to drive along a block by considering the direction
   * clients request to enter the block.
   */
  private class BlockPermission {

    /**
     * The block to manage permissions for.
     */
    private final Block block;
    /**
     * The clients permitted to drive along the block.
     */
    private final Set<Scheduler.Client> clients = new HashSet<>();
    /**
     * The direction vehicles are allowed to enter the block.
     */
    @Nullable
    private String entryDirection;
    /**
     * The queue of pending permission requests.
     */
    private final Queue<PermissionRequest> pendingRequests = new LinkedList<>();

    public BlockPermission(Block block) {
      this.block = requireNonNull(block, "block");
    }

    public void permitPendingRequests() {
      while (hasPendingRequests()) {
        PermissionRequest request = pendingRequests.poll();

        if (clientAlreadyInBlock(request.getClient())) {
          LOG.debug("{}: Permission for {} already granted",
                    block.getName(),
                    request.getClient().getId());
        }
        else if (entryPermissible(request.getEntryDirection())) {
          clients.add(request.getClient());
          this.entryDirection = request.getEntryDirection();
          LOG.debug("{}: Permission granted for {} (entryDirection={})",
                    block.getName(),
                    request.getClient().getId(),
                    request.getEntryDirection());
        }
      }
    }

    public boolean enqueueRequest(Scheduler.Client client, String entryDirection) {
      if (clientAlreadyInBlock(client)
          || entryPermissible(entryDirection)) {
        pendingRequests.add(new PermissionRequest(client, entryDirection));
        return true;
      }

      LOG.debug("{}: Client '{}' with entry direction '{}' (!= '{}') not permissible. ",
                block.getName(),
                client.getId(),
                entryDirection,
                this.entryDirection);
      return false;
    }

    public void clearPendingRequests() {
      pendingRequests.clear();
    }

    public void removePermissionFor(Scheduler.Client client) {
      clients.remove(client);

      if (clients.isEmpty()) {
        entryDirection = null;
      }
    }

    public boolean isPermissionGranted(Scheduler.Client client) {
      return clients.contains(client);
    }

    private boolean hasPendingRequests() {
      return !pendingRequests.isEmpty();
    }

    private boolean clientAlreadyInBlock(Scheduler.Client client) {
      return isPermissionGranted(client);
    }

    private boolean entryPermissible(String entryDirection) {
      return this.entryDirection == null
          || Objects.equals(this.entryDirection, entryDirection);
    }
  }

  private class PermissionRequest {

    /**
     * The requesting client.
     */
    private final Scheduler.Client client;
    /**
     * The entry direction permission is requested for.
     */
    private final String entryDirection;

    /**
     * Creates a new instance.
     *
     * @param client The requesting client.
     * @param entryDirection The entry direction permission is requested for.
     * @param blocks The blocks the client requests permission for.
     */
    public PermissionRequest(Scheduler.Client client, String entryDirection) {
      this.client = requireNonNull(client, "client");
      this.entryDirection = requireNonNull(entryDirection, "entryDirection");
    }

    public Scheduler.Client getClient() {
      return client;
    }

    public String getEntryDirection() {
      return entryDirection;
    }
  }
}
