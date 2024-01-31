/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.configuration;

/**
 * Thrown when a configuration error occured.
 */
public class ConfigurationException
    extends RuntimeException {

  /**
   * Constructs a new instance with no detail message.
   */
  public ConfigurationException() {
  }

  /**
   * Constructs a new instance with the specified detail message.
   *
   * @param message The detail message.
   */
  public ConfigurationException(String message) {
    super(message);
  }

  /**
   * Constructs a new instance with the specified detail message and cause.
   *
   * @param message The detail message.
   * @param cause The exception's cause.
   */
  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new instance with the specified cause and a detail
   * message of <code>(cause == null ? null : cause.toString())</code> (which
   * typically contains the class and detail message of <code>cause</code>).
   *
   * @param cause The exception's cause.
   */
  public ConfigurationException(Throwable cause) {
    super(cause);
  }
}
