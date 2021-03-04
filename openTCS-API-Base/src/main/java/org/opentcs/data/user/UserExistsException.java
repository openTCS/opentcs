/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.user;

import org.opentcs.access.KernelException;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Thrown when a new user account is supposed to be created, but an equivalent
 * user already exists.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated User management via kernel interaction will not be supported in the future.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
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
