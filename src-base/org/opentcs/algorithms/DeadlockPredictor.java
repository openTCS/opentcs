/*
 * openTCS copyright information:
 * Copyright (c) 2010 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import java.util.List;
import java.util.Set;
import org.opentcs.data.model.TCSResource;

/**
 * A <code>DeadlockPredictor</code> manages resources used by vehicles,
 * preventing deadlocks.
 * A system simulation state is updated according to the real world state.
 * Resource requests, allocations and deallocations are performed to achieve
 * deadlock free simulation results.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface DeadlockPredictor {

  /**
   * Sets a vehicle's route, represented by a sequence of resource sets
   * corresponding to one <code>Step</code> each.
   * Such a sequence is also called <i>total claim</i>. Total claim is
   * equivalent to the route computed by Dijkstra's algorithm.
   *
   * @param resourceUser The <code>ResourceUser</code> the resource sequence is
   * assigned to.
   * @param resourcesSequence The resource sequence, i.e. total claim.
   */
  void setRoute(ResourceUser resourceUser,
                List<Set<TCSResource<?>>> resourcesSequence);

  /**
   * Updates the simulation state to the real system state.
   * If any simulation results have already been computed, the simulation state
   * corresponding to the real state will be looked up in the system state tree.
   * If no simulation results are available, the new root allocation state will
   * be generated according to the new input data and simulation will be
   * started. Call this method when
   * <ul>
   * <li>simulation is started for the first time</li>
   * <li>the real system state has been changed, so the changes influence the
   * simulation results, i.e. the simulation should be restarted. (Vehicle
   * failure, unexpected allocation state etc. require the simulation state to
   * be adapted to the new real allocation state.)</li>
   * </ul>
   *
   * @param resourceUser The resource user
   * @param alloc The current allocation.
   * @param remainingClaim The remaining claim of a given resource user.
   */
  void configureSystemData(ResourceUser resourceUser,
                           Set<TCSResource<?>> alloc,
                           List<Set<TCSResource<?>>> remainingClaim);

  /**
   * Checks if the resulting system state is safe if the given set of resources
   * would be allocated by the given resource user.
   *
   * @param resourceUser The <code>ResourceUser</code> requesting resources set.
   * @param resSet The requested resource set.
   * @return <code>true</code> if no deadlock is predicted for the given
   * resource allocation.
   */
  boolean isAllocationAdmissible(ResourceUser resourceUser,
                                 Set<TCSResource> resSet);
}
