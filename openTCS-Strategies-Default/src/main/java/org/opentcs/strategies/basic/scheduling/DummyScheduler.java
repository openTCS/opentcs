/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.model.TCSResource;

/**
 * A <code>Scheduler</code> implementation that does not really do any resource management - all
 * allocations are granted immediately without checking.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DummyScheduler
    implements Scheduler {

  /**
   * Executes our <code>CallbackTask</code>s.
   */
  private ExecutorService callbackExecutor;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new DummyScheduler.
   */
  public DummyScheduler() {
  }

  @Override
  public void initialize() {
    callbackExecutor = Executors.newSingleThreadExecutor();
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    callbackExecutor.shutdown();
    initialized = false;
  }

  @Override
  public void claim(Client client, List<Set<TCSResource<?>>> resourceSequence) {
  }

  @Override
  public void updateProgressIndex(Client client, int index) {
  }

  @Override
  public void unclaim(Client client) {
  }

  @Override
  public void allocate(Client resourceUser, Set<TCSResource<?>> resources) {
    requireNonNull(resourceUser, "resourceUser");
    requireNonNull(resources, "resources");
    // Just schedule the callback for successful allocation.
    callbackExecutor.execute(new CallbackTask(resourceUser, resources));
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void free(Client resourceUser, Set<TCSResource<?>> resources) {
  }

  @Override
  public void freeAll(Client client) {
  }

  @Override
  public void allocateNow(Client resourceUser, Set<TCSResource<?>> resources) {
  }

  @Override
  public Map<String, Set<TCSResource<?>>> getAllocations() {
    return new HashMap<>();
  }

  /**
   * A task that merely calls back <code>ResourceUser</code>s.
   */
  private static class CallbackTask
      implements Runnable {

    /**
     * The client to call back.
     */
    private final Client client;
    /**
     * The resources the client is asking for.
     */
    private final Set<TCSResource<?>> resources;

    /**
     * Creates a new CallbackTask.
     *
     * @param newClient The client to call back.
     * @param newResources The resources the client is asking for.
     */
    CallbackTask(Client newClient, Set<TCSResource<?>> newResources) {
      client = newClient;
      resources = newResources;
    }

    @Override
    public void run() {
      client.allocationSuccessful(resources);
    }
  }
}
