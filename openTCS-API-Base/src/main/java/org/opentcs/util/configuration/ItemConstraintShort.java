/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

/**
 * Item Constraint for Short type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
public class ItemConstraintShort
    extends ItemConstraintInteger {

  /**
   * Creates a constraint of type Short .
   * 
   */
  public ItemConstraintShort() {
    this(Short.MIN_VALUE, Short.MAX_VALUE);
  }

  /**
   * Creates a constraint of type Short .
   *
   * @param minValue is the minimum value for Short Item Value.
   * @param maxValue is the maximum value for Short Item Value.
   */
  public ItemConstraintShort(short minValue, short maxValue) {
    super(minValue, maxValue, ConfigurationDataType.SHORT);
  }
}
