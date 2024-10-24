// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data;

import org.opentcs.access.KernelRuntimeException;

/**
 * Thrown when an object was supposed to be returned/removed/modified, but could
 * not be found.
 */
public class ObjectUnknownException
    extends
      KernelRuntimeException {

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
