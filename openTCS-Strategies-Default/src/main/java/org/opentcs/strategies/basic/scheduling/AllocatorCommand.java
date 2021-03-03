/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.components.kernel.Scheduler.Client;
import org.opentcs.data.model.TCSResource;

/**
 * A command for the scheduler's allocation task.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class AllocatorCommand
    implements Comparable<AllocatorCommand> {

  /**
   * The command's priority (lesser values represent higher priority).
   */
  private final int priority;
  /**
   * The point of time at which the command was created.
   */
  private final long creationTime;
  /**
   * The scheduler client the command is associated with.
   */
  private final Client client;

  /**
   * Creates a new instance.
   *
   * @param priority The command's priority (lesser values represent higher priority).
   * @param client The scheduler client the command is associated with.
   */
  private AllocatorCommand(int priority, Client client) {
    this.priority = priority;
    this.client = requireNonNull(client, "client");
    this.creationTime = System.currentTimeMillis();
  }

  @Override
  public int compareTo(AllocatorCommand o) {
    // Natural ordering of commands by (1) priority, (2) age and (3) client ID.
    if (priority < o.priority) {
      return -1;
    }
    else if (priority > o.priority) {
      return 1;
    }
    else if (this.creationTime < o.creationTime) {
      return -1;
    }
    else if (this.creationTime > o.creationTime) {
      return 1;
    }
    else {
      return client.getId().compareTo(o.client.getId());
    }
  }

  /**
   * Returns the scheduler client this command is associated with.
   *
   * @return The scheduler client.
   */
  public Client getClient() {
    return client;
  }

  /**
   * Indicates the receiving task should be terminated.
   */
  public static class PoisonPill
      extends AllocatorCommand {

    /**
     * Creates a new instance.
     */
    public PoisonPill() {
      super(1, new DummyClient());
    }
  }

  /**
   * Indicates the receiving task should retry to grant deferred allocations.
   */
  public static class RetryAllocates
      extends AllocatorCommand {

    /**
     * Creates a new instance.
     *
     * @param client The scheduler client this command is associated with.
     */
    public RetryAllocates(Client client) {
      super(2, client);
    }
  }

  /**
   * Indicates the receiving task should try to allocate a set of resources for a client.
   */
  public static class Allocate
      extends AllocatorCommand {

    /**
     * The resources to be allocated.
     */
    private final Set<TCSResource<?>> resources;

    /**
     * Creates a new instance.
     *
     * @param client The scheduler client this command is associated with.
     * @param resources The resources to be allocated.
     */
    public Allocate(Client client, Set<TCSResource<?>> resources) {
      super(4, client);
      this.resources = requireNonNull(resources, "resources");
    }

    /**
     * Returns the resources to be allocated.
     *
     * @return The resources to be allocated.
     */
    public Set<TCSResource<?>> getResources() {
      return resources;
    }
  }

  /**
   * A dummy client for commands not really associated with a client.
   */
  private static class DummyClient
      implements Client {

    @Override
    public String getId() {
      return getClass().getName();
    }

    @Override
    public boolean allocationSuccessful(Set<TCSResource<?>> resources) {
      return false;
    }

    @Override
    public void allocationFailed(Set<TCSResource<?>> resources) {
    }
  }
}
