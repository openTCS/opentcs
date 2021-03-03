/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import java.util.Map;
import java.util.Set;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.order.Route;

/**
 * A <code>Scheduler</code> manages resources used by vehicles, preventing
 * both collisions and deadlocks.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Scheduler {

  /**
   * Called by the dispatcher once it knows the new route of a vehicle.
   *
   * @param user The resource user for which the route is defined.
   * @param route The new/current route of the resource user.
   */
  void setRoute(ResourceUser user, Route route);

  /**
   * Called by the vehicle controller whenever the vehicle has finished another
   * step on its current route.
   *
   * @param user The resource user for which the position is updated.
   * @param index The index of the user's current position on its route.
   * @throws IllegalArgumentException If the index is not a valid index for the
   * vehicle's current route.
   */
  void setRouteIndex(ResourceUser user, int index);

  /**
   * Claims a set of resources for a vehicle.
   *
   * @param resourceUser The <code>ResourceUser</code> claiming the resources.
   * @param resources The resources claimed.
   */
  void claim(ResourceUser resourceUser, Set<TCSResource> resources);

  /**
   * Allocates a set of resources for a vehicle.
   *
   * @param resourceUser The <code>ResourceUser</code> requesting the resources.
   * @param resources The resources requested.
   */
  void allocate(ResourceUser resourceUser, Set<TCSResource> resources);

  /**
   * Informs the scheduler that a set of resources are to be allocated for the
   * given <code>ResourceUser</code> <em>immediately</em>, i.e. without
   * blocking.
   * <p>
   * This method should only be called in urgent/emergency cases, for instance
   * if a vehicle has been moved to a different point manually, which has to be
   * reflected by resource allocation in the scheduler.
   * </p>
   * This method does not block, which means that it's safe to call it
   * synchronously.
   *
   * @param resourceUser The <code>ResourceUser</code> requesting the resources.
   * @param resources The resources requested.
   * @throws ResourceAllocationException If it's impossible to allocate the
   * given set of resources for the given <code>ResourceUser</code>.
   */
  void allocateNow(ResourceUser resourceUser, Set<TCSResource> resources)
      throws ResourceAllocationException;

  /**
   * Releases a set of resources allocated by a vehicle.
   *
   * @param resourceUser The <code>ResourceUser</code> releasing the resources.
   * @param resources The resources released. Any resources in the given set not
   * allocated by the given <code>ResourceUser</code> are ignored.
   */
  void free(ResourceUser resourceUser, Set<TCSResource> resources);

  /**
   * Unclaims a set of resources claimed by a vehicle.
   *
   * @param resourceUser The <code>ResourceUser</code> unclaiming the resources.
   * @param resources The resources unclaimed.
   */
  void unclaim(ResourceUser resourceUser, Set<TCSResource> resources);

  /**
   * Returns all resource allocations as a map of <code>ResourceUser</code> IDs
   * to resources.
   *
   * @return All resource allocations as a map of <code>ResourceUser</code> IDs
   * to resources.
   */
  Map<String, Set<TCSResource>> getAllocations();
}
