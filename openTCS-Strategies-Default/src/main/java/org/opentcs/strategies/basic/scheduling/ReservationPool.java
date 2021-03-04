/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.model.TCSResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class ReservationPool {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReservationPool.class);
  /**
   * <code>ReservationEntry</code> instances for each <code>TCSResource</code>.
   */
  private final Map<TCSResource<?>, ReservationEntry> reservations = new HashMap<>();

  /**
   * Creates a new instance.
   */
  public ReservationPool() {
  }

  /**
   * Returns a reservation entry for the given resource.
   *
   * @param resource The resource for which to return the reservation entry.
   * @return The reservation entry for the given resource.
   */
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
   * Returns all resources allocated by the given client.
   *
   * @param client The client for which to return all allocated resources.
   * @return All resources allocated by the given client.
   */
  public Set<TCSResource<?>> allocatedResources(Scheduler.Client client) {
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
  public boolean resourcesAvailableForUser(Set<TCSResource<?>> resources, Scheduler.Client client) {
    requireNonNull(resources, "resources");
    requireNonNull(client, "client");

    for (TCSResource<?> curResource : resources) {
      // Check if the resource is available.
      ReservationEntry entry = getReservationEntry(curResource);
      if (!entry.isFree() && !entry.isAllocatedBy(client)) {
        LOG.debug("{}: Resource unavailable: {}", client.getId(), entry.getResource());
        return false;
      }
    }
    return true;
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
  private Set<TCSResource<?>> getFreeableResources(Set<TCSResource<?>> resources,
                                                   Scheduler.Client client) {
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

  public void free(Scheduler.Client client, Set<TCSResource<?>> resources) {
    requireNonNull(client, "client");
    requireNonNull(resources, "resources");

    LOG.debug("{}: Releasing resources: {}", client.getId(), resources);
    Set<TCSResource<?>> freeableResources = getFreeableResources(resources,
                                                                 client);
    for (TCSResource<?> curResource : freeableResources) {
      getReservationEntry(curResource).free();
    }
  }

  public void freeAll(Scheduler.Client client) {
    requireNonNull(client, "client");

    reservations.values().stream()
        .filter(reservationEntry -> reservationEntry.isAllocatedBy(client))
        .forEach(reservationEntry -> reservationEntry.freeCompletely());
  }

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
    reservations.clear();
  }
}
