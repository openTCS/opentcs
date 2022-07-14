/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for an integer value.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class IntegerProperty
    extends AbstractProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public IntegerProperty(ModelComponent model) {
    this(model, 0);
  }

  /**
   * Creates a new instance with a value.
   *
   * @param model The model component.
   * @param value The value.
   */
  public IntegerProperty(ModelComponent model, int value) {
    super(model);
    setValue(value);
  }

  @Override
  public Object getComparableValue() {
    return String.valueOf(fValue);
  }

  @Override
  public String toString() {
    return fValue instanceof Integer ? Integer.toString((int) fValue) : (String) fValue;
  }

  @Override
  public void copyFrom(Property property) {
    IntegerProperty integerProperty = (IntegerProperty) property;
    setValue(integerProperty.getValue());
  }
}
