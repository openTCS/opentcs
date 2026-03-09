// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.event;

/**
 * A source of events that can be subscribed to.
 */
public interface EventSource {

  /**
   * Subscribes the given listener to events emitted by this source.
   * <p>
   * When used within the kernel application, callers of this method are supposed to ensure that the
   * call is executed in the context of the kernel executor. Implementations of this method, on
   * the other hand, need to take into account that they are executed in the context of the kernel
   * executor.
   * </p>
   *
   * @param listener The listener to be subscribed.
   * @see org.opentcs.customizations.kernel.KernelExecutor
   */
  void subscribe(EventHandler listener);

  /**
   * Unsubscribes the given listener.
   * <p>
   * When used within the kernel application, callers of this method are supposed to ensure that the
   * call is executed in the context of the kernel executor. Implementations of this method, on
   * the other hand, need to take into account that they are executed in the context of the kernel
   * executor.
   * </p>
   *
   * @param listener The listener to be unsubscribed.
   * @see org.opentcs.customizations.kernel.KernelExecutor
   */
  void unsubscribe(EventHandler listener);
}
