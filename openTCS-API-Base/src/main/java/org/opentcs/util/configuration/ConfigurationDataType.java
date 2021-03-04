/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A data type of a configuration item.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public enum ConfigurationDataType {

  /**
   * Marks a configuration item containing a boolean.
   */
  BOOLEAN,
  /**
   * Marks a configuration item containing a byte.
   */
  BYTE,
  /**
   * Marks a configuration item containing a short.
   */
  SHORT,
  /**
   * Marks a configuration item containing an integer.
   */
  INTEGER,
  /**
   * Marks a configuration item containing a long.
   */
  LONG,
  /**
   * Marks a configuration item containing a float.
   */
  FLOAT,
  /**
   * Marks a configuration item containing a double.
   */
  DOUBLE,
  /**
   * Marks a configuration item containing a string.
   */
  STRING,
  /**
   * Marks a configuration item containing a enum value.
   */
  ENUM
}
