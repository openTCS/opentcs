/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import org.opentcs.access.KernelRuntimeException;

/**
 * Thrown when a kernel is not available for processing a request.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelUnavailableException
    extends KernelRuntimeException {

  /**
   * Creates a new KernelUnavailableException with the given detail message.
   *
   * @param message The detail message.
   */
  public KernelUnavailableException(String message) {
    super(message);
  }

  /**
   * Creates a new KernelUnavailableException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public KernelUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
