/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import static com.google.common.base.Preconditions.checkPositionIndex;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.opentcs.algorithms.ResourceUser;
import org.opentcs.algorithms.Scheduler;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.order.Route;

/**
 * A <code>Scheduler</code> implementation that does not really do any resource
 * management - all allocations are granted immediately without checking.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DummyScheduler
    implements Scheduler {

  /**
   * An <code>Executor</code> for <code>CallbackTask</code>s.
   */
  private final Executor callbackExecutor = Executors.newSingleThreadExecutor();

  /**
   * Creates a new DummyScheduler.
   */
  public DummyScheduler() {
  }

  @Override
  public void setRoute(ResourceUser user, Route route) {
    requireNonNull(user, "user");
    requireNonNull(route, "route");
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void setRouteIndex(ResourceUser user, int index) {
    requireNonNull(user, "user");
    checkPositionIndex(index, Integer.MAX_VALUE);
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void claim(ResourceUser resourceUser, Set<TCSResource> resources) {
    requireNonNull(resourceUser, "resourceUser");
    requireNonNull(resources, "resources");
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void allocate(ResourceUser resourceUser, Set<TCSResource> resources) {
    requireNonNull(resourceUser, "resourceUser");
    requireNonNull(resources, "resources");
    // Just schedule the callback for successful allocation.
    callbackExecutor.execute(new CallbackTask(resourceUser, resources));
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void free(ResourceUser resourceUser, Set<TCSResource> resources) {
    requireNonNull(resourceUser, "resourceUser");
    requireNonNull(resources, "resources");
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void allocateNow(ResourceUser resourceUser,
                          Set<TCSResource> resources) {
    requireNonNull(resourceUser, "resourceUser");
    requireNonNull(resources, "resources");
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void unclaim(ResourceUser resourceUser, Set<TCSResource> resources) {
    requireNonNull(resourceUser, "resourceUser");
    requireNonNull(resources, "resources");
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public Map<String, Set<TCSResource>> getAllocations() {
    final Map<String, Set<TCSResource>> result
        = new HashMap<>();
    return result;
  }

  /**
   * A task that merely calls back <code>ResourceUser</code>s.
   */
  private static class CallbackTask
      implements Runnable {

    /**
     * The <code>ResourceUser</code> to call back.
     */
    private final ResourceUser resourceUser;
    /**
     * The resources the <code>ResourceUser</code> is asking for.
     */
    private final Set<TCSResource> resources;

    /**
     * Creates a new CallbackTask.
     *
     * @param newResourceUser The <code>ResourceUser</code> to call back.
     * @param newResources The resources the <code>ResourceUser</code> is asking
     * for.
     */
    CallbackTask(ResourceUser newResourceUser, Set<TCSResource> newResources) {
      resourceUser = newResourceUser;
      resources = newResources;
    }

    @Override
    public void run() {
      resourceUser.allocationSuccessful(resources);
    }
  }
}
