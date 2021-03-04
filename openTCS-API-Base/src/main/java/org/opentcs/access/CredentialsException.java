/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;

/**
 * Thrown when there are insufficient user permissions to perform an operation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CredentialsException
    extends KernelRuntimeException
    implements Serializable {

  /**
   * Constructs a CredentialsException with no detail message.
   */
  public CredentialsException() {
    super();
  }

  /**
   * Constructs a CredentialsException with the specified detail message.
   *
   * @param message The detail message.
   */
  public CredentialsException(String message) {
    super(message);
  }

  /**
   * Constructs a CredentialsException with the specified detail message and
   * cause.
   *
   * @param message The detail message.
   * @param cause The exception's cause.
   */
  public CredentialsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a CredentialsException with the specified cause and a detail
   * message of <code>(cause == null ? null : cause.toString())</code> (which
   * typically contains the class and detail message of <code>cause</code>).
   *
   * @param cause The exception's cause.
   */
  public CredentialsException(Throwable cause) {
    super(cause);
  }
}
