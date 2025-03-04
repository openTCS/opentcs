// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.model.TCSResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ReservationPool {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReservationPool.class);
  /**
   * All claims.
   */
  private final Map<Scheduler.Client, Queue<Set<TCSResource<?>>>> claimsByClient
      = new HashMap<>();
  /**
   * <code>ReservationEntry</code> instances for each <code>TCSResource</code>.
   */
  private final Map<TCSResource<?>, ReservationEntry> reservations = new HashMap<>();

  /**
   * Creates a new instance.
   */
  @Inject
  public ReservationPool() {
  }

  /**
   * Returns a reservation entry for the given resource.
   *
   * @param resource The resource for which to return the reservation entry.
   * @return The reservation entry for the given resource.
   */
  @Nonnull
  public ReservationEntry getReservationEntry(TCSResource<?> resource) {
    requireNonNull(resource, "resource");

    ReservationEntry entry = reservations.get(resource);
    if (entry == null) {
      entry = new ReservationEntry(resource);
      reservations.put(resource, entry);
    }
    return entry;
  }

  /**
   * Returns the sequence of resource sets claimed by the given client.
   *
   * @param client The client.
   * @return The sequence of resource sets claimed by the given client.
   */
  @Nonnull
  public List<Set<TCSResource<?>>> getClaim(
      @Nonnull
      Scheduler.Client client
  ) {
    requireNonNull(client, "client");

    return claimsByClient.getOrDefault(client, new ArrayDeque<>()).stream()
        .map(resourceSet -> Set.copyOf(resourceSet))
        .collect(Collectors.toList());
  }

  /**
   * Sets the sequence of claimed resource sets for the given client.
   *
   * @param client The client.
   * @param resources The sequence of claimed resources.
   */
  public void setClaim(
      @Nonnull
      Scheduler.Client client,
      @Nonnull
      List<Set<TCSResource<?>>> resources
  ) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    claimsByClient.put(client, new ArrayDeque<>(resources));
  }

  /**
   * Removes the given resource set from the head of the sequence of claimed resource sets for the
   * given client.
   *
   * @param client The client.
   * @param resources The resource set to be removed from the head of the client's claim sequence.
   * @throws IllegalArgumentException If the given resource set is not the head of the client's
   * claim sequence.
   */
  public void unclaim(
      @Nonnull
      Scheduler.Client client,
      @Nonnull
      Set<TCSResource<?>> resources
  )
      throws IllegalArgumentException {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    if (!claimsByClient.containsKey(client) || claimsByClient.get(client).isEmpty()) {
      return;
    }

    if (!isNextInClaim(client, resources)) {
      throw new IllegalArgumentException(
          String.format(
              "Resources to unclaim and head of claimed resource don't match: %s != %s",
              resources,
              claimsByClient.get(client).peek()
          )
      );
    }

    claimsByClient.get(client).remove();
  }

  /**
   * Checks whether the given resource set is at the head of the given client's claim sequence.
   *
   * @param client The client.
   * @param resources The resources to be checked.
   * @return <code>true</code> if, and only if, the given resource set is at the head of the given
   * client's claim sequence.
   */
  public boolean isNextInClaim(
      @Nonnull
      Scheduler.Client client,
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    if (!claimsByClient.containsKey(client) || claimsByClient.get(client).isEmpty()) {
      return false;
    }

    if (!Objects.equals(resources, claimsByClient.get(client).peek())) {
      return false;
    }

    return true;
  }

  /**
   * Returns all resources allocated by the given client.
   *
   * @param client The client for which to return all allocated resources.
   * @return All resources allocated by the given client.
   */
  @Nonnull
  public Set<TCSResource<?>> allocatedResources(
      @Nonnull
      Scheduler.Client client
  ) {
    requireNonNull(client, "client");

    return reservations.entrySet().stream()
        .filter(entry -> entry.getValue().isAllocatedBy(client))
        .map(entry -> entry.getKey())
        .collect(Collectors.toSet());
  }

  /**
   * Checks if all resources in the given set of resources are be available for the given client.
   *
   * @param resources The set of resources to be checked.
   * @param client The client for which to check.
   * @return <code>true</code> if, and only if, all resources in the given set
   * are available for the given client.
   */
  public boolean resourcesAvailableForUser(
      @Nonnull
      Set<TCSResource<?>> resources,
      @Nonnull
      Scheduler.Client client
  ) {
    requireNonNull(resources, "resources");
    requireNonNull(client, "client");

    for (TCSResource<?> curResource : resources) {
      // Check if the resource is available.
      ReservationEntry entry = getReservationEntry(curResource);
      if (!entry.isFree() && !entry.isAllocatedBy(client)) {
        LOG.debug(
            "{}: Resource {} unavailable, reserved by {}",
            client.getId(),
            curResource.getName(),
            entry.getClient().getId()
        );
        return false;
      }
    }
    return true;
  }

  public void free(
      @Nonnull
      Scheduler.Client client,
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    LOG.debug("{}: Releasing resources: {}", client.getId(), resources);
    for (TCSResource<?> curResource : getFreeableResources(resources, client)) {
      getReservationEntry(curResource).free();
    }
  }

  public void freeAll(
      @Nonnull
      Scheduler.Client client
  ) {
    requireNonNull(client, "client");

    reservations.values().stream()
        .filter(reservationEntry -> reservationEntry.isAllocatedBy(client))
        .forEach(reservationEntry -> reservationEntry.freeCompletely());
  }

  @Nonnull
  public Map<String, Set<TCSResource<?>>> getAllocations() {
    final Map<String, Set<TCSResource<?>>> result = new HashMap<>();
    for (Map.Entry<TCSResource<?>, ReservationEntry> curEntry : reservations.entrySet()) {
      final TCSResource<?> curResource = curEntry.getKey();
      final Scheduler.Client curUser = curEntry.getValue().getClient();
      if (curUser != null) {
        Set<TCSResource<?>> userResources = result.get(curUser.getId());
        if (userResources == null) {
          userResources = new HashSet<>();
        }
        userResources.add(curResource);
        result.put(curUser.getId(), userResources);
      }
    }
    return result;
  }

  public void clear() {
    claimsByClient.clear();
    reservations.clear();
  }

  /**
   * Returns a set of resources that is a subset of the given set of resources and is reserved/could
   * be released by the given client.
   *
   * @param resources The set of resources to be filtered for resources that could be released.
   * @param client The client that should be able to release the returned resources.
   * @return A set of resources that is a subset of the given set of resources and is reserved/could
   * be released by the given client.
   */
  @Nonnull
  private Set<TCSResource<?>> getFreeableResources(
      @Nonnull
      Set<TCSResource<?>> resources,
      @Nonnull
      Scheduler.Client client
  ) {
    // Make sure we're freeing only resources that are allocated by us.
    final Set<TCSResource<?>> freeableResources = new HashSet<>();
    for (TCSResource<?> curRes : resources) {
      ReservationEntry entry = getReservationEntry(curRes);
      if (!entry.isAllocatedBy(client)) {
        LOG.warn("{}: Freed resource not reserved: {}, entry: {}", client.getId(), curRes, entry);
      }
      else {
        freeableResources.add(curRes);
      }
    }
    return freeableResources;
  }
}
