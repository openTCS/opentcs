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
 * Thrown when an object was supposed to be returned/removed/modified, but could
 * not be found.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ObjectUnknownException
    extends KernelRuntimeException {

  /**
   * Creates a new ObjectExistsException with the given detail message.
   *
   * @param message The detail message.
   */
  public ObjectUnknownException(String message) {
    super(message);
  }

  /**
   * Creates a new ObjectExistsException for the given object reference.
   *
   * @param ref The object reference.
   */
  public ObjectUnknownException(TCSObjectReference<?> ref) {
    super("Object unknown: " + (ref == null ? "<null>" : ref.toString()));
  }

  /**
   * Creates a new ObjectExistsException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public ObjectUnknownException(String message, Throwable cause) {
    super(message, cause);
  }
}
