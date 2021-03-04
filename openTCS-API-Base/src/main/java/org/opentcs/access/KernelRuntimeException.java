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
 * A runtime exception thrown by the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KernelRuntimeException
    extends RuntimeException
    implements Serializable {

  /**
   * Constructs a new instance with no detail message.
   */
  public KernelRuntimeException() {
    super();
  }

  /**
   * Constructs a new instance with the specified detail message.
   *
   * @param message The detail message.
   */
  public KernelRuntimeException(String message) {
    super(message);
  }

  /**
   * Constructs a new instance with the specified detail message and cause.
   *
   * @param message The detail message.
   * @param cause The exception's cause.
   */
  public KernelRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new instance with the specified cause and a detail
   * message of <code>(cause == null ? null : cause.toString())</code> (which
   * typically contains the class and detail message of <code>cause</code>).
   *
   * @param cause The exception's cause.
   */
  public KernelRuntimeException(Throwable cause) {
    super(cause);
  }
}
