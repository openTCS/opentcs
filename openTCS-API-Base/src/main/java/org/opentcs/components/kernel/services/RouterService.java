/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;

/**
 * Provides methods concerning the {@link Router}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RouterService {

  /**
   * Updates a path's lock state.
   *
   * @param ref A reference to the path to be updated.
   * @param locked Indicates whether the path is to be locked ({@code true}) or unlocked
   * ({@code false}).
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updatePathLock(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Notifies the router that the topology has changed in a significant way and needs to be
   * re-evaluated.
   *
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void updateRoutingTopology()
      throws KernelRuntimeException;
}
