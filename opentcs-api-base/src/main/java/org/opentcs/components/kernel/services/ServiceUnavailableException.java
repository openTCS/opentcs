// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel.services;

import org.opentcs.access.KernelRuntimeException;

/**
 * Thrown when a (remote) service is not available for processing a request.
 */
public class ServiceUnavailableException
    extends
      KernelRuntimeException {

  /**
   * Creates a new ServiceUnavailableException with the given detail message.
   *
   * @param message The detail message.
   */
  public ServiceUnavailableException(String message) {
    super(message);
  }

  /**
   * Creates a new ServiceUnavailableException with the given detail message and cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public ServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
