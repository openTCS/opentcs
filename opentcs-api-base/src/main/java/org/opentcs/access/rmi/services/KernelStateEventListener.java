// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.services;

import org.opentcs.access.Kernel;

/**
 * A listener for events concerning kernel state changes.
 */
interface KernelStateEventListener {

  /**
   * Called when the kernel state changes to {@link Kernel.State#SHUTDOWN}.
   */
  void onKernelShutdown();
}
