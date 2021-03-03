/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.UnsupportedKernelOpException;
import org.opentcs.algorithms.DeadlockPredictor;
import org.opentcs.algorithms.ResourceAllocationException;
import org.opentcs.algorithms.ResourceUser;
import org.opentcs.algorithms.Scheduler;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;

/**
 * A <code>BasicScheduler</code> implements the basic simple
 * scheduler strategy for resources used by vehicles, preventing
 * both collisions and deadlocks.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class BasicScheduler
    implements Scheduler {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(BasicScheduler.class.getName());
  /**
   * A Kernel instance for expanding resource sets.
   */
  private final LocalKernel localKernel;
  /**
   * A deadlock predictor instance.
   */
  private final DeadlockPredictor deadlockPredictor;
  /**
   * <code>ReservationEntry</code> instances for each <code>TCSResource</code>.
   */
  private final Map<TCSResource, ReservationEntry> reservations =
      new HashMap<>();
  /**
   * A list of inquiring tasks waiting for their resources.
   */
  private final List<ResourceUser> inquiringTasks = new LinkedList<>();
  /**
   * The scheduler tasks executor.
   */
  private final Executor tasksExecutor;
  /**
   * A boolean flag indicating whether it is safe to initiate a
   * <code>notify()</code> cascade in <code>free()</code>. It is used to prevent
   * deadlocks and race conditions in <code>free()</code>.
   */
  private boolean releasable = true;
  /**
   * A mapping of existing resource users to their routes.
   */
  private final Map<ResourceUser, Route> routesByUser = new HashMap<>();

  /**
   * Creates a new BasicScheduler instance.
   *
   * @param kernel A kernel instance for expanding resource sets.
   * @param deadlockPredictor The deadlock predictor to be used.
   */
  @Inject
  public BasicScheduler(LocalKernel kernel,
                        DeadlockPredictor deadlockPredictor) {
    this.localKernel = requireNonNull(kernel, "kernel");
    this.deadlockPredictor = requireNonNull(deadlockPredictor,
                                            "deadlockPredictor");
//    tasksExecutor = Executors.newCachedThreadPool();
    // Create a thread pool with a timeout of 10 seconds. This is only a hack to
    // make the kernel die faster on shutdown.
    tasksExecutor = new ThreadPoolExecutor(0,
                                           Integer.MAX_VALUE,
                                           10L,
                                           TimeUnit.SECONDS,
                                           new SynchronousQueue<Runnable>());
  }

  // Dispatcher
  @Override
  public void setRoute(ResourceUser user, Route route) {
    Objects.requireNonNull(user, "user is null");
    Objects.requireNonNull(route, "route is null");

    // Update deadlock predictor.
    List<Step> steps = route.getSteps();
    List<Set<TCSResource<?>>> resourceSequence = new ArrayList<>();
    for (Step step : steps) {
      Set<TCSResource<?>> stepSet = new HashSet<>();
      stepSet.add(step.getDestinationPoint());
      stepSet.add(step.getPath());
      resourceSequence.add(stepSet);
    }
    deadlockPredictor.setRoute(user, resourceSequence);
    routesByUser.put(user, route);
    Set<TCSResource<?>> allocatedResources = allocatedResources(user);
    deadlockPredictor.configureSystemData(user,
                                          allocatedResources,
                                          resourceSequence);
  }

  // Vehicle controller
  @Override
  public void setRouteIndex(ResourceUser user, int index) {
    Objects.requireNonNull(user, "user is null");
    if (index < 0) {
      throw new IllegalArgumentException("index < 0: " + index);
    }

    // Update deadlock predictor.
    if (routesByUser.containsKey(user)) {
      Route route = routesByUser.get(user);
      List<Step> steps = route.getSteps();
      List<Step> remainingClaimRoute = steps.subList(index + 1, steps.size());
      List<Set<TCSResource<?>>> remainingClaimResourceSequence =
          new ArrayList<>();
      Set<TCSResource<?>> stepSet;
      for (Step step : remainingClaimRoute) {
        stepSet = new HashSet<>();
        stepSet.add(step.getDestinationPoint());
        stepSet.add(step.getPath());
        remainingClaimResourceSequence.add(stepSet);
      }
      Set<TCSResource<?>> allocatedResources = allocatedResources(user);
      deadlockPredictor.configureSystemData(user,
                                            allocatedResources,
                                            remainingClaimResourceSequence);
    }
  }

  @Override
  public void claim(ResourceUser resourceUser, Set<TCSResource> resources) {
    throw new UnsupportedKernelOpException("claim() not implemented");
  }

  @Override
  public void allocate(ResourceUser resourceUser, Set<TCSResource> resources) {
    Objects.requireNonNull(resourceUser, "resourceUser is null");
    Objects.requireNonNull(resources, "resources is null");

    // Create the new allocation task to allocate the resources.
    tasksExecutor.execute(new SchedulerTask(resourceUser, resources));
  }

  @Override
  public void allocateNow(ResourceUser resourceUser, Set<TCSResource> resources)
      throws ResourceAllocationException {
    Objects.requireNonNull(resourceUser, "resourceUser is null");
    Objects.requireNonNull(resources, "resources is null");

    synchronized (reservations) {
      // Check if all resources are available.
      final Set<TCSResource> availableResources = new HashSet<>();
      for (TCSResource curResource : resources) {
        ReservationEntry entry = getReservationEntry(curResource);
        if (!entry.isFree() && !entry.getResourceUser().equals(resourceUser)) {
          log.severe("Resource unavailable: " + curResource.getName());
          // XXX DO something about it?!
        }
        else {
          availableResources.add(curResource);
        }
      }
      // Allocate all requested resources that are available.
      for (TCSResource curResource : availableResources) {
        getReservationEntry(curResource).allocate(resourceUser);
      }
    }
  }

  @Override
  public void free(ResourceUser resourceUser, Set<TCSResource> resources) {
    Objects.requireNonNull(resourceUser, "resourceUser is null");
    Objects.requireNonNull(resources, "resources is null");

    synchronized (inquiringTasks) {
      boolean freeSuccessful = false;
      // Loop until we could free the resources.
      do {
        if (releasable) {
          log.fine("releasable flag set, releasing resources...");
          Set<TCSResource> freeableResources =
              getFreeableResources(resources, resourceUser);
          // Decrement the reservation counter for freed resources.
          log.fine("Releasing resources: " + freeableResources);
          for (TCSResource curRes : freeableResources) {
            getReservationEntry(curRes).free();
          }
          checkWaitingRequests();
          freeSuccessful = true;
        }
        else {
          log.fine("releasable flag not set, waiting...");
          try {
            inquiringTasks.wait();
            log.fine("Woken up, trying again...");
          }
          catch (InterruptedException exc) {
            throw new IllegalStateException("Unexpectedly interrupted", exc);
          }
        }
      } while (!freeSuccessful);
    }
  }

  @Override
  public void unclaim(ResourceUser resourceUser, Set<TCSResource> resources) {
    throw new UnsupportedKernelOpException("unclaim() not implemented");
  }

  @Override
  public Map<String, Set<TCSResource>> getAllocations() {
    final Map<String, Set<TCSResource>> result = new HashMap<>();
    for (Map.Entry<TCSResource, ReservationEntry> curEntry : reservations.entrySet()) {
      final TCSResource curResource = curEntry.getKey();
      final ResourceUser curUser = curEntry.getValue().getResourceUser();
      if (curUser != null) {
        Set<TCSResource> userResources = result.get(curUser.getId());
        if (userResources == null) {
          userResources = new HashSet<>();
        }
        userResources.add(curResource);
        result.put(curUser.getId(), userResources);
      }
    }
    return result;
  }

  // Private methods start here.
  /**
   * Returns a set of resources that is a subset of the given set of resources
   * and is reserved/could be released by the given resource user.
   *
   * @param resources The set of resources to be filtered for resources that
   * could be released.
   * @param resourceUser The resource user that should be able to release the
   * returned resources.
   * @return A set of resources that is a subset of the given set of resources
   * and is reserved/could be released by the given resource user.
   */
  private Set<TCSResource> getFreeableResources(Set<TCSResource> resources,
                                                ResourceUser resourceUser) {
    // Make sure we're freeing only resources that are allocated by us.
    final Set<TCSResource> freeableResources = new HashSet<>();
    for (TCSResource curRes : resources) {
      ReservationEntry entry = getReservationEntry(curRes);
      if (entry.isFree() || !entry.getResourceUser().equals(resourceUser)) {
        log.warning("Freed resource not reserved: " + entry.getResource());
      }
      else {
        freeableResources.add(curRes);
      }
    }
    return freeableResources;
  }

  /**
   * Returns the given set of resources after expansion (by resolution of
   * blocks, for instance) by the kernel.
   * 
   * @param resources The set of resources to be expanded.
   * @return The given set of resources after expansion (by resolution of
   * blocks, for instance) by the kernel.
   */
  private Set<TCSResource> expandResources(Set<TCSResource> resources) {
    assert resources != null;
    // Build a set of references
    Set<TCSResourceReference> refs = new HashSet<>();
    for (TCSResource curResource : resources) {
      refs.add(curResource.getReference());
    }
    // Let the kernel expand the resources for us.
    try {
      Set<TCSResource> result = localKernel.expandResources(refs);
      log.fine("Set " + resources + " expanded to " + result);
      return result;
    }
    catch (ObjectUnknownException exc) {
      throw new IllegalStateException("Unexpected exception", exc);
    }
  }

  /**
   * Returns a reservation entry for the given resource.
   *
   * @param resource The resource for which to return the reservation entry.
   * @return The reservation entry for the given resource.
   */
  private ReservationEntry getReservationEntry(TCSResource resource) {
    log.finer("method entry");
    assert resource != null;
    ReservationEntry entry = reservations.get(resource);
    if (entry == null) {
      entry = new ReservationEntry(resource);
      reservations.put(resource, entry);
    }
    return entry;
  }

  /**
   * Returns all resources allocated by the given resource user.
   *
   * @param user The resource user for which to return all allocated resources.
   * @return All resources allocated by the given resource user.
   */
  private Set<TCSResource<?>> allocatedResources(ResourceUser user) {
    assert user != null;
    Set<TCSResource<?>> result = new HashSet<>();
    for (Map.Entry<TCSResource, ReservationEntry> entry : reservations.entrySet()) {
      if (user.equals(entry.getValue().getResourceUser())) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  /**
   * Checks if all resources in the given set of resources are be available for
   * the given resource user.
   *
   * @param resources The set of resources to be checked.
   * @param resourceUser The resource user for which to check.
   * @return <code>true</code> if, and only if, all resources in the given set
   * are available for the given resource user.
   */
  private boolean resourcesAvailableForUser(Set<TCSResource> resources,
                                            ResourceUser resourceUser) {
    assert resources != null;
    assert resourceUser != null;
    for (TCSResource curResource : resources) {
      // Check if the resource is available.
      ReservationEntry entry = getReservationEntry(curResource);
      if (!entry.isFree() && !entry.isAllocatedBy(resourceUser)) {
        log.fine(resourceUser.getId() + ": Resource unavailable: "
            + entry.getResource());
        return false;
      }
    }
    return true;
  }

  /**
   * Wakes up the first task waiting for resources to check if it can allocate
   * the resources now.
   */
  private void checkWaitingRequests() {
    if (!inquiringTasks.isEmpty()) {
      ResourceUser firstResourceUser = inquiringTasks.get(0);
      synchronized (firstResourceUser) {
        log.fine("Notifying first inquiring task");
        firstResourceUser.notify();
        releasable = false;
      }
    }
  }

  /**
   * Checks if the required resources are available and allocates them if they
   * are. If they are not, <code>wait()</code>s until notified by someone
   * <code>free()</code>ing resources.
   */
  private final class SchedulerTask
      implements Runnable {

    /**
     * The required resources.
     */
    private final Set<TCSResource> resources;
    /**
     * The expanded set of required resources.
     */
    private final Set<TCSResource> resourcesExpanded;
    /**
     * The vehicle controller this task is associated with.
     */
    private final ResourceUser resourceUser;

    /**
     * Creates a new SchedulerTask.
     *
     * @param newResourceUser The client requesting the resources.
     * @param reqResources The set of resources required.
     */
    SchedulerTask(ResourceUser newResourceUser, Set<TCSResource> reqResources) {
      assert newResourceUser != null;
      assert reqResources != null;
      resourceUser = newResourceUser;
      resources = reqResources;
      resourcesExpanded = expandResources(resources);
    }

    @Override
    public void run() {
      synchronized (resourceUser) {
        boolean doCascade = false;
        boolean allocationAdmissible = false;
        while (!allocationAdmissible) {
          synchronized (inquiringTasks) {
            int index = 0;
            log.fine(resourceUser.getId() + ": Checking if all resources are "
                + "available...");
            // Check if the resources in the expanded set are all available and
            // if we may actually allocate them.
            allocationAdmissible =
                resourcesAvailableForUser(resourcesExpanded, resourceUser)
                && deadlockPredictor.isAllocationAdmissible(resourceUser, resources);

            if (allocationAdmissible) {
              log.fine(resourceUser.getId() + ": All resources available");
              // Allocate resources.
              for (TCSResource curRes : resources) {
                getReservationEntry(curRes).allocate(resourceUser);
              }
              // If this task is in the queue of waiting tasks, remove it now
              // that we have allocated all required resources.
              if (inquiringTasks.contains(resourceUser)) {
                index = inquiringTasks.indexOf(resourceUser);
                log.fine(resourceUser.getId() + ": Removing task from waiting"
                    + " queue");
                inquiringTasks.remove(resourceUser);
              }
            }
            else {
              log.fine(resourceUser.getId() + ": Resources unavailable");
              if (!inquiringTasks.contains(resourceUser)) {
                log.fine(resourceUser.getId() + ": Joining waiting queue");
                inquiringTasks.add(resourceUser);
              }
              // If the task is already in the queue, determine the index of its
              // successor - we have to wake it up so it can check for available
              // resources, too.
              else {
                log.fine(resourceUser.getId() + ": Task already in queue");
                index = inquiringTasks.indexOf(resourceUser) + 1;
              }
            }
            // Wake up the successor of this task in the waiting queue so it can
            // check for available resources, too.
            if (doCascade) {
              // If this task has a successor, wake it up.
              if (inquiringTasks.size() > index) {
                ResourceUser neighbor = inquiringTasks.get(index);
                log.fine(resourceUser.getId() + ": Synchronizing on successor");
                synchronized (neighbor) {
                  log.fine(resourceUser.getId() + ": Waking up successor");
                  neighbor.notify();
                }
              }
              // If this task is the last one in the waiting queue, end the
              // cascade and set the flag to allow the next free() call.
              else {
                log.fine(resourceUser.getId() + ": End of cascade reached, "
                    + "setting releasable flag");
                releasable = true;
                inquiringTasks.notify();
              }
            }
          }
          // If we couldn't allocate the resources, wait for someone to free
          // them.
          if (!allocationAdmissible) {
            try {
              log.fine(resourceUser.getId() + ": Waiting for resources...");
              resourceUser.wait();
              log.fine(resourceUser.getId() + ": Woken up");
              doCascade = true;
            }
            catch (InterruptedException exc) {
              throw new IllegalStateException("Unexpectedly interrupted", exc);
            }
          }
        }
        log.fine(resourceUser.getId()
            + ": Allocation successful, calling back ResourceUser");
        // If the resource user doesn't want the resources any more, free them.
        if (!resourceUser.allocationSuccessful(resources)) {
          log.warning(resourceUser.getId()
              + ": ResourceUser didn't want allocated resources, freeing them");
          synchronized (inquiringTasks) {
            for (TCSResource curResource : resources) {
              getReservationEntry(curResource).free();
            }
          }
          // XXX Shouldn't we cascade here? We freed/didn't use the resources...
        }
      }
    }
  }
}
