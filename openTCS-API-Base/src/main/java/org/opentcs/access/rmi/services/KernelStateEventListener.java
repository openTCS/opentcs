/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import org.opentcs.access.Kernel;

/**
 * A listener for events concerning kernel state changes.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
interface KernelStateEventListener {

  /**
   * Called when the kernel state changes to {@link Kernel.State#SHUTDOWN}.
   */
  void onKernelShutdown();
}
