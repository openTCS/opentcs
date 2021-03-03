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
 * Item Constraint for Long type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
public class ItemConstraintLong
    extends ItemConstraintInteger {

  /**
   * Creates a constraint of type Long .
   * 
   */
  public ItemConstraintLong() {
    this(Long.MIN_VALUE, Long.MAX_VALUE);
  }

  /**
   * Creates a constraint of type Long .
   * 
   * @param minValue is the minimum value for Long Item Value.
   * @param maxValue is the maximum value for Long Item Value.
   */
  public ItemConstraintLong(long minValue, long maxValue) {
    super(minValue, maxValue, ConfigurationDataType.LONG);
  }
}
