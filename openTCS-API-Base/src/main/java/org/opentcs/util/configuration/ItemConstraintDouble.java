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
 * Item Constraint for Double type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ItemConstraintDouble
    extends ItemConstraint {
  /**
   * Minimum value for Double .
   */
  private final double minValue;
  /**
   * Maximum value for Double .
   */
  private final double maxValue;

  /**
   * Creates a constraint of type Double .
   * 
   */
  public ItemConstraintDouble() {
    this(Double.MIN_VALUE, Double.MAX_VALUE);
  }

  /**
   * Creates a constraint of type Double .
   * 
   * @param minValue is the minimum value for Double Item Value.
   * @param maxValue is the maximum value for Double Item Value.
   */
  public ItemConstraintDouble(double minValue, double maxValue) {
    super(ConfigurationDataType.DOUBLE,minValue,maxValue,null);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public boolean accepts(String value) {
    try {
      double valueDouble = Double.parseDouble(value);
      return valueDouble >= minValue && valueDouble <= maxValue;
    }
    catch (NumberFormatException exp) {
      return false;
    }
  }
}
