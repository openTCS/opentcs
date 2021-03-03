/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.user;

import org.opentcs.access.KernelException;

/**
 * Thrown when a user account was supposed to be used in some way, but does not
 * exist.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UserUnknownException
    extends KernelException {

  /**
   * Creates a new UserUnknownException with the specified detail message.
   *
   * @param message the detail message.
   */
  public UserUnknownException(String message) {
    super(message);
  }

  /**
   * Creates a new UserUnknownException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public UserUnknownException(String message, Throwable cause) {
    super(message, cause);
  }
}
