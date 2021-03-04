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
 * Item Constraint for Integer type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ItemConstraintInteger
    extends ItemConstraint {

  /**
   * Minimum value for Integer .
   */
  private final long minValue;
  /**
   * Maximum value for Integer .
   */
  private final long maxValue;

  /**
   * Creates a constraint of type Integer .
   * 
   */
  public ItemConstraintInteger() {
    this(Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  /**
   * Creates a constraint of type Integer .
   * 
   * @param minValue is the minimum value for Integer Item Value.
   * @param maxValue is the maximum value for Integer Item Value.
   */
  public ItemConstraintInteger(long minValue, long maxValue) {
    super(ConfigurationDataType.INTEGER,minValue,maxValue, null);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  /**
   * Creates a constraint of type Integer .
   * 
   * @param minValue is the minimum value for Integer Item Value.
   * @param maxValue is the maximum value for Integer Item Value.
   * @param type provides the data type of the item.
   */
  public ItemConstraintInteger(long minValue, long maxValue,
                                  ConfigurationDataType type) {
    super(type,minValue,maxValue, null);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public boolean accepts(String value) {
    try {
      int valueInt = Integer.parseInt(value);
      return valueInt >= minValue && valueInt <= maxValue;
    }
    catch (NumberFormatException exp) {
      return false;
    }
  }
}
