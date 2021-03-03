/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Attribut für einen booleschen Wert.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BooleanProperty
    extends AbstractProperty {

  /**
   * Creates a new instance of BooleanProperty.
   *
   * @param model
   */
  public BooleanProperty(ModelComponent model) {
    this(model, false);
  }

  /**
   * Konstruktor mit booleschem Wert.
   *
   * @param model
   * @param value
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
