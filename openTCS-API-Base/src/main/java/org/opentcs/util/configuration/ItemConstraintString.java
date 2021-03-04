/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

/**
 * Item Constraint for String type value.
 *
 * @author Preity Gupta (Fraunhofer IML)
 */
public class ItemConstraintString
    extends ItemConstraint {

  /**
   * Creates a constraint of type String .
   * 
   */
  public ItemConstraintString() {
    super(ConfigurationDataType.STRING,0,0,null);
  }

  @Override
  public boolean accepts(String value) {
    return value != null;
  }
}
