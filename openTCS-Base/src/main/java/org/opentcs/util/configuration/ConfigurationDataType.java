/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

/**
 * A data type of a configuration item.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
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
