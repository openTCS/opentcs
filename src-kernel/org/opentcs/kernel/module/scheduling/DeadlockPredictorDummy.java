/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.scheduling;

import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.algorithms.DeadlockPredictor;
import org.opentcs.algorithms.ResourceUser;
import org.opentcs.data.model.TCSResource;

/**
 * A dummy <code>DeadlockPredictor</code> implementation that allows all
 * allocations.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class DeadlockPredictorDummy
    implements DeadlockPredictor {
  
  /**
   * Creates a new instance.
   */
  @Inject
  public DeadlockPredictorDummy() {
    // Do nada.
  }

  /**
   * Does not do anything.
   *
   * @param resourceUser A resource user.
   * @param alloc A set of resources.
   * @param remainingClaim A sequence of resource sets.
   */
  @Override
  public void configureSystemData(ResourceUser resourceUser,
                                  Set<TCSResource<?>> alloc,
                                  List<Set<TCSResource<?>>> remainingClaim) {
    // Do nada.
  }

  /**
   * Always returns <code>true</code>.
   * 
   * @param resourceUser A resource user.
   * @param resSet A set of resources.
   * @return <code>true</code>, always.
   */
  @Override
  public boolean isAllocationAdmissible(ResourceUser resourceUser,
                                        Set<TCSResource> resSet) {
    return true;
  }

  /**
   * Does not do anything.
   *
   * @param resourceUser A resource user.
   * @param resourcesSequence A sequence of resource sets.
   */
  @Override
  public void setRoute(ResourceUser resourceUser,
                       List<Set<TCSResource<?>>> resourcesSequence) {
    // Do nada.
  }
}
