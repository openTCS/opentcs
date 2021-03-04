/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import org.opentcs.access.KernelException;

/**
 * Thrown when there was a problem with interpreting a persisted model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class InvalidModelException
    extends KernelException {

  /**
   * Creates a new InvalidModelException with the given detail message.
   *
   * @param message The detail message.
   */
  public InvalidModelException(String message) {
    super(message);
  }

  /**
   * Creates a new InvalidModelException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public InvalidModelException(String message, Throwable cause) {
    super(message, cause);
  }
}
