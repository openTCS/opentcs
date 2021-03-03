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
 * A property containing a key-value pair.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KeyValueProperty
    extends AbstractComplexProperty {

  /**
   * Der Schlüssel.
   */
  private String fKey;

  /**
   * Creates a new instance of KeyValueProperty
   * @param model
   */
  public KeyValueProperty(ModelComponent model) {
    this(model, "", "");
  }

  /**
   * Creates a new instance of KeyValueProperty.
   *
   * @param model
   * @param key der Schlüssel
   * @param value der Wert
   */
  public KeyValueProperty(ModelComponent model, String key, String value) {
    super(model);
    fKey = key;
    fValue = value;
  }

  @Override
  public Object getComparableValue() {
    return fKey + fValue; // besser: toString() ???
  }

  /**
   * Setzt den Schlüssel und den Wert.
   *
   * @param key der Schlüssel
   * @param value der Wert
   */
  public void setKeyAndValue(String key, String value) {
    fKey = key;
    fValue = value;
  }

  /**
   * Liefert den Schlüssel.
   *
   * @return den Schlüssel
   */
  public String getKey() {
    return fKey;
  }

  @Override
  public String getValue() {
    return (String) fValue;
  }

  @Override
  public String toString() {
    return fKey + "=" + fValue;
  }

  @Override
  public void copyFrom(Property property) {
    KeyValueProperty keyValueProperty = (KeyValueProperty) property;
    setKeyAndValue(keyValueProperty.getKey(), keyValueProperty.getValue());
  }
}
