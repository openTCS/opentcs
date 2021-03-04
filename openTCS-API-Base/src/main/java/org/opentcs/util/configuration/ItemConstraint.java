/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.util.Objects;
import java.util.Set;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Constraints on a configuration item.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public abstract class ItemConstraint {

  /**
   * Data Type constraint.
   */
  private final ConfigurationDataType type;
  /**
   * Enum Values for Enum data types.
   */
  private final Set<String> enumValues;
  /**
   * Minimum value for Integer data types.
   */
  private final double minValue;
  /**
   * Maximum value for Integer data types.
   */
  private final double maxValue;

  /**
   * Creates item constraint and sets the data type .
   *
   * @param newType provides the data type of the item.
   * @param values provides the enum values incase the data type is enum.
   * @param minVal It is the minimum Value for the ConfugrationData Type
   * specified by newType.
   * @param maxVal It is the maximum Value for the ConfugrationData Type
   * specified by newType.
   */
  public ItemConstraint(ConfigurationDataType newType,double minVal,
                        double maxVal, Set<String> values) {
    type = Objects.requireNonNull(newType, "newType is null");
    enumValues = values;
    minValue = minVal;
    maxValue= maxVal;
  }

  /**
   * Checks if the value lies within a specific range .
   * 
   * @param value of the configuration item .
   * @return a boolean value, true if the value is acceptable
   * and false otherwise .
   */
  public abstract boolean accepts(String value);

  /**
   * Returns the Data type of the value.
   * 
   * @return type returns the data type of the item.
   */
  public final ConfigurationDataType getType() {
    return type;
  }

  /**
   * Returns elements of the enum.
   * 
   * @return enumValues returns the elements of the enum.
   */
  public final Set<String> getEnum() {
    return enumValues;
  }
  /**
   * Returns minimun value of the item.
   * 
   * @return minValue returns the minimun value of the item.
   */
  public final double getMinVal() {
    return minValue;
  }
  /**
   * Returns maximum value of the item.
   * 
   * @return maxValue returns the maximum value of the item.
   */
  public final double getMaxVal() {
    return maxValue;
  }
}
