/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import org.opentcs.access.KernelException;

/**
 * Thrown when allocating resources for a {@link Scheduler.Client Scheduler.Client} is impossible.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ResourceAllocationException
    extends KernelException {

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
