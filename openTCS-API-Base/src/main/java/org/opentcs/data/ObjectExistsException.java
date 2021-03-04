/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import org.opentcs.access.KernelRuntimeException;

/**
 * Thrown when an object was supposed to be created or renamed, but another
 * object with the same ID/name/attributes already exists.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ObjectExistsException
    extends KernelRuntimeException {

  /**
   * Creates a new ObjectExistsException with the given detail message.
   *
   * @param message The detail message.
   */
  public ObjectExistsException(String message) {
    super(message);
  }

  /**
   * Creates a new ObjectExistsException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public ObjectExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
