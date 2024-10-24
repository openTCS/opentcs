// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application;

import org.opentcs.access.Kernel;

/**
 * Listener interface implemented by classes interested in changes of a
 * connected kernel's state.
 */
public interface KernelStateHandler {

  /**
   * Informs the handler that the kernel is now in the given state.
   *
   * @param state The new state.
   */
  void enterKernelState(Kernel.State state);
}
