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
 * Basisimplementierung für ein Property. Stellt Methoden bereit, um die
 * Veränderbarkeit eines Attributs und die Sichtbarkeit eingestellt werden kann.
 * Außerdem ist die Verwaltung eines Hilfstextes zu dem Attribut implementiert.
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
   *
   */
  private boolean isCollectionAndHasDifferentValues;

  /**
   * Creates a new instance of AbstractProperty
   * @param model
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

  /**
   * Sets whether this property is part of <code>PropertiesCollection</code>
   * and the selected values are different.
   *
   * @param newValue True if the selected objects differ in this property.
   */
  public void setIsCollectionAndHasDifferentValues(boolean newValue) {
    isCollectionAndHasDifferentValues = newValue;
  }

  /**
   * Returns whether this property is part of <code>PropertiesCollection</code>
   * and the selected values are different.
   *
   * @return True or false.
   */
  public boolean isCollectionAndHasDifferentValues() {
    return isCollectionAndHasDifferentValues;
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
