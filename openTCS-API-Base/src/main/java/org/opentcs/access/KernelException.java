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
 * An exception thrown by the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelException
    extends Exception
    implements Serializable {

  /**
   * Constructs a new instance with no detail message.
   */
  public KernelException() {
    super();
  }

  /**
   * Constructs a new instance with the specified detail message.
   *
   * @param message The detail message.
   */
  public KernelException(String message) {
    super(message);
  }

  /**
   * Constructs a new instance with the specified detail message and cause.
   *
   * @param message The detail message.
   * @param cause The exception's cause.
   */
  public KernelException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new instance with the specified cause and a detail
   * message of <code>(cause == null ? null : cause.toString())</code> (which
   * typically contains the class and detail message of <code>cause</code>).
   *
   * @param cause The exception's cause.
   */
  public KernelException(Throwable cause) {
    super(cause);
  }
}
