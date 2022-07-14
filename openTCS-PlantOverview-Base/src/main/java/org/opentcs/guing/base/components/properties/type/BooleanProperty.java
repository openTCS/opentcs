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
 * A property for a boolean value.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BooleanProperty
    extends AbstractProperty {

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
