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
 * Base implementation for a property.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractProperty
    extends AbstractModelAttribute
    implements Property {

  /**
   * The value of this property.
   */
  protected Object fValue;

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public AbstractProperty(ModelComponent model) {
    super(model);
  }

  /**
   * Sets the value.
   *
   * @param newValue The new value.
   */
  public void setValue(Object newValue) {
    fValue = newValue;
  }

  /**
   * Returns the value of this property.
   *
   * @return The value.
   */
  public Object getValue() {
    return fValue;
  }

  @Override
  public void copyFrom(Property property) {
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException exc) {
      throw new RuntimeException("Unexpected exception", exc);
    }
  }
}
