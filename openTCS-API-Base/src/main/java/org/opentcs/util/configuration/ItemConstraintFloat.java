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
 * Item Constraint for Float type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ItemConstraintFloat
    extends ItemConstraint {

  /**
   * Minimum value for Float .
   */
  private final float minValue;
  /**
   * Maximum value for Float .
   */
  private final float maxValue;

  /**
   * Creates a constraint of type Float .
   * 
   */
  public ItemConstraintFloat() {
    this(Float.MIN_VALUE, Float.MAX_VALUE);
  }

  /**
   * Creates a constraint of type Float .
   * 
   * @param minValue is the minimum value for Float Item Value.
   * @param maxValue is the maximum value for Float Item Value.
   */
  public ItemConstraintFloat(float minValue, float maxValue) {
    super(ConfigurationDataType.FLOAT,minValue,maxValue, null);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public boolean accepts(String value) {
    try {
      float valueFloat = Float.parseFloat(value);
      return valueFloat >= minValue && valueFloat <= maxValue;
    }
    catch (NumberFormatException exp) {
      return false;
    }
  }
}
