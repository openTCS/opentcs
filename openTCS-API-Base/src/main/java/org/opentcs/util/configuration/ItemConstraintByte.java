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
 * Item Constraint for Bzte type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ItemConstraintByte
    extends ItemConstraintInteger {
  /**
   * Creates a constraint of type Byte .
   * 
   */
  public ItemConstraintByte() {
    this(Byte.MIN_VALUE, Byte.MAX_VALUE);
  }
  
  /**
   * Creates a constraint of type Byte .
   * 
   * @param minValue is the minimum value for Byte Item Value.
   * @param maxValue is the maximum value for Byte Item Value.
   */
  public ItemConstraintByte(byte minValue, byte maxValue) {
    super(minValue, maxValue, ConfigurationDataType.BYTE);
  }
}
