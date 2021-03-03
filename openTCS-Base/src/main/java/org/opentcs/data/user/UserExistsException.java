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
 * Thrown when a new user account is supposed to be created, but an equivalent
 * user already exists.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UserExistsException
    extends KernelException {

  /**
   * Creates a new UserExistsException with the specified detail message.
   *
   * @param message the detail message.
   */
  public UserExistsException(String message) {
    super(message);
  }

  /**
   * Creates a new UserExistsException with the given detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public UserExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
