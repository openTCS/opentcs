/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import org.opentcs.components.kernel.Scheduler.Client;
import org.opentcs.data.model.TCSResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains reservation information for a resource - a reference to the
 * <code>ResourceUser</code> currently holding the resource and a counter
 * for how many times the <code>ResouceUser</code> has allocated the resource.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class ReservationEntry {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReservationEntry.class);
  /**
   * Instance of resource that vehicle may claim for exclusive usage.
   */
  private final TCSResource<?> resource;
  /**
   * The client for which the resource is currently reserved.
   */
  private Client client;
  /**
   * The reservation counter.
   * With every allocation the counter will be incremented, with every call to <code>free()</code>
   * it will be decremented.
   */
  private int counter;

  /**
   * Creates a new instance.
   *
   * @param reqResource The resource.
   */
  public ReservationEntry(final TCSResource<?> reqResource) {
    this.resource = requireNonNull(reqResource, "reqResource");
  }

  /**
   * Returns the resource.
   *
   * @return The resource.
   */
  public TCSResource<?> getResource() {
    return resource;
  }

  /**
   * Returns the client currently allocating the resource.
   *
   * @return The client currently allocating the resource, or <code>null</code>, if the resource
   * isn't currently allocated.
   */
  public Client getClient() {
    return client;
  }

  /**
   * Reserves the resource for the given client.
   * Increments the reservation counter for the resource if the user has already allocated this
   * resource before.
   *
   * @param client The allocating client.
   */
  void allocate(Client client) {
    if (this.client == null) {
      LOG.debug("Allocating resource {} for client {}", resource, client.getId());
      this.client = client;
    }
    else if (this.client != client) {
      // The resource is already allocated by someone else - may not happen.
      throw new IllegalStateException("'" + client + "' tried to allocate resource allocated by "
          + this.client);
    }
    else {
      LOG.debug("Incrementing allocation counter for resource {}; client: {}",
                resource,
                client.getId());
    }
    counter++;
  }

  /**
   * Deallocates the resource once, i.e. decrements the allocation counter.
   * If the counter is decremented to zero, the resource is freed and the reference to the client
   * is set to <code>null</code>.
   */
  void free() {
    checkState(counter > 0, "counter is already less than 1");
    counter--;
    if (counter == 0) {
      client = null;
    }
  }

  /**
   * Deallocates the resource completely, i.e. set the allocation counter to zero and the client
   * to <code>null</code>.
   */
  void freeCompletely() {
    counter = 0;
    client = null;
  }

  /**
   * Checks if the resource is currently not allocated by anyone.
   *
   * @return <code>true</code> if, and only if, the resource is not currently
   * allocated by anyone.
   */
  boolean isFree() {
    return (client == null) && (counter == 0);
  }

  /**
   * Checks if the resource is currently allocated by the given client.
   *
   * @param client The client.
   * @return <code>true</code> if, and only if, the resource is currently allocated by the given
   * client.
   */
  boolean isAllocatedBy(Client client) {
    return this.client == client;
  }

  @Override
  public String toString() {
    return "ReservationEntry{"
        + "resource=" + resource
        + ", client=" + client
        + ", counter=" + counter
        + '}';
  }
}
