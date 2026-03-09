// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.event;

/**
 * A handler for events emitted by an {@link EventSource}.
 */
public interface EventHandler {

  /**
   * Processes the event object.
   * <p>
   * When used within the kernel application, callers of this method are supposed to ensure that the
   * call is executed in the context of the kernel executor. Implementations of this method, on
   * the other hand, need to take into account that they are executed in the context of the kernel
   * executor.
   * </p>
   *
   * @param event The event object.
   * @see org.opentcs.customizations.kernel.KernelExecutor
   */
  void onEvent(Object event);
}
