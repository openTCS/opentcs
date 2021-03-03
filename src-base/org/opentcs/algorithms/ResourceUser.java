/*
 * openTCS copyright information:
 * Copyright (c) 2008 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import java.util.Set;
import org.opentcs.data.model.TCSResource;

/**
 * Defines callback methods for clients of the resource scheduler.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ResourceUser {
  /**
   * Returns an ID string for this <code>ResourceUser</code>.
   * The returned string should be unique among all <code>ResourceUser</code>s
   * in the system. This method may never return <code>null</code>.
   *
   * @return An ID string for this <code>ResourceUser</code>.
   */
  String getId();
  /**
   * Called when resources have been reserved for this
   * <code>ResourceUser</code>.
   *
   * @param resources The resources reserved.
   * @return <code>true</code> if, and only if, this <code>ResourceUser</code>
   * accepts the resources allocated. A return value of <code>false</code>
   * indicates this <code>ResourceUser</code> does not need the given resources
   * (any more), freeing them implicitly.
   */
  boolean allocationSuccessful(Set<TCSResource> resources);
  
  /**
   * Called if it was impossible to allocate resources for this
   * <code>ResourceUser</code>.
   *
   * @param resources The resources which could not be reserved.
   */
  void allocationFailed(Set<TCSResource> resources);
}
