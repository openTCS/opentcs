/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.scheduling;

import java.util.HashMap;
import java.util.Map;
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
public final class DummyScheduler
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
    if (user == null) {
      throw new NullPointerException("user is null");
    }
    if (route == null) {
      throw new NullPointerException("route is null");
    }
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void setRouteIndex(ResourceUser user, int index) {
    if (user == null) {
      throw new NullPointerException("user is null");
    }
    if (index < 0) {
      throw new IllegalArgumentException("index < 0: " + index);
    }
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void claim(ResourceUser resourceUser, Set<TCSResource> resources) {
    if (resourceUser == null) {
      throw new NullPointerException("resourceUser is null");
    }
    if (resources == null) {
      throw new NullPointerException("resources is null");
    }
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void allocate(ResourceUser resourceUser, Set<TCSResource> resources) {
    if (resourceUser == null) {
      throw new NullPointerException("resourceUser is null");
    }
    if (resources == null) {
      throw new NullPointerException("resources is null");
    }
    // Just schedule the callback for successful allocation.
    callbackExecutor.execute(new CallbackTask(resourceUser, resources));
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void free(ResourceUser resourceUser, Set<TCSResource> resources) {
    if (resourceUser == null) {
      throw new NullPointerException("resourceUser is null");
    }
    if (resources == null) {
      throw new NullPointerException("resources is null");
    }
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void allocateNow(ResourceUser resourceUser,
                          Set<TCSResource> resources) {
    if (resourceUser == null) {
      throw new NullPointerException("resourceUser is null");
    }
    if (resources == null) {
      throw new NullPointerException("resources is null");
    }
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public void unclaim(ResourceUser resourceUser, Set<TCSResource> resources) {
    if (resourceUser == null) {
      throw new NullPointerException("resourceUser is null");
    }
    if (resources == null) {
      throw new NullPointerException("resources is null");
    }
    // Don't do anything else - this is a dummy, after all.
  }

  @Override
  public Map<String,Set<TCSResource>> getAllocations() {
    final Map<String,Set<TCSResource>> result =
            new HashMap<>();
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
