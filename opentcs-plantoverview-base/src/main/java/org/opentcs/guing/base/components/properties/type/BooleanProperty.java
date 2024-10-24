// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for a boolean value.
 */
public class BooleanProperty
    extends
      AbstractProperty {

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public BooleanProperty(ModelComponent model) {
    this(model, false);
  }

  /**
   * Creates a new property with a value.
   *
   * @param model The model component.
   * @param value The value.
   */
  @SuppressWarnings("this-escape")
  public BooleanProperty(ModelComponent model, boolean value) {
    super(model);
    setValue(value);
  }

  @Override // Property
  public Object getComparableValue() {
    return String.valueOf(fValue);
  }

  @Override
  public String toString() {
    return getValue().toString();
  }

  @Override
  public void copyFrom(Property property) {
    BooleanProperty booleanProperty = (BooleanProperty) property;
    setValue(booleanProperty.getValue());
  }
}
