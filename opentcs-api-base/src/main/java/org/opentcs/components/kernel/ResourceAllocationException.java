// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import org.opentcs.access.KernelException;

/**
 * Thrown when allocating resources for a {@link Scheduler.Client Scheduler.Client} is impossible.
 */
public class ResourceAllocationException
    extends
      KernelException {

  /**
   * Creates a new ResourceAllocationException with the given detail message.
   *
   * @param message The detail message.
   */
  public ResourceAllocationException(String message) {
    super(message);
  }

  /**
   * Creates a new ResourceAllocationException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public ResourceAllocationException(String message, Throwable cause) {
    super(message, cause);
  }
}
