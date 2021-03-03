/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.scheduling;

import java.util.logging.Logger;
import org.opentcs.algorithms.ResourceUser;
import org.opentcs.data.model.TCSResource;

/**
 * Contains reservation information for a resource - a reference to the
 * <code>ResourceUser</code> currently holding the resource and a counter
 * for how many times the <code>ResouceUser</code> has allocated the resource.
 * 
 * @author Stefan Walter (Fraunhofer IML)
 */
final class ReservationEntry {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(ReservationEntry.class.getName());
  /**
   * Instance of resource that vehicle may claim for exclusive usage.
   */
  private final TCSResource resource;
  /**
   * The <code>ResourceUser</code> for which the resource is currently
   * reserved.
   */
  private ResourceUser resourceUser;
  /**
   * The reservation counter. With every allocation the counter will be
   * incremented, with every call to <code>free()</code> it will be
   * decremented.
   */
  private int counter;

  /**
   * Creates a new ReservationEntry.
   *
   * @param reqResource The resource.
   */
  public ReservationEntry(final TCSResource reqResource) {
    assert reqResource != null;
    this.resource = reqResource;
  }

  /**
   * Returns the resource.
   *
   * @return The resource.
   */
  public TCSResource getResource() {
    return resource;
  }

  /**
   * Returns the <code>ResourceUser</code> currently allocating the resource.
   *
   * @return The <code>ResourceUser</code> currently allocating the resource, or
   * <code>null</code>, if the resource isn't currently allocated.
   */
  public ResourceUser getResourceUser() {
    return resourceUser;
  }

  /**
   * Reserves the resource for the given <code>ResourceUser</code>.
   * Increments the reservation counter for the resource if the user has
   * already allocated this resource before.
   *
   * @param newResourceUser The allocating <code>ResourceUser</code>.
   */
  void allocate(ResourceUser newResourceUser) {
    if (resourceUser == null) {
      log.fine("Allocating resource " + resource.toString()
          + " for user " + newResourceUser.getId());
      resourceUser = newResourceUser;
    }
    else if (resourceUser != newResourceUser) {
      // The resource is already allocated by someone else - may not happen.
      log.severe("resourceUser != newResourceUser");
      throw new IllegalStateException("resourceUser != newResourceUser");
    }
    else {
      log.fine("Incrementing allocation counter for resource "
          + resource.toString() + "; user: " + resourceUser.getId());
    }
    counter++;
  }

  /**
   * Deallocates the resource once, i.e. decrements the allocation counter. If
   * the counter is decremented to zero the resource is freed and the
   * reference to the <code>ResourceUser</code> is set to <code>null</code>.
   */
  void free() {
    assert counter > 0;
    counter--;
    if (counter == 0) {
      resourceUser = null;
    }
  }

  /**
   * Checks if the resource is currently not allocated by anyone.
   *
   * @return <code>true</code> if, and only if, the resource is not currently
   * allocated by anyone.
   */
  boolean isFree() {
    return (resourceUser == null) && (counter == 0);
  }

  /**
   * Checks if the resource is currently allocated by the given user.
   *
   * @param user The user.
   * @return <code>true</code> if, and only if, the resource is currently
   * allocated by the given user.
   */
  boolean isAllocatedBy(ResourceUser user) {
    return resourceUser == user;
  }
}
