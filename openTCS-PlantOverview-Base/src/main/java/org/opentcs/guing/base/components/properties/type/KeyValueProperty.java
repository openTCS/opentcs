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
 * A property containing a key-value pair.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class KeyValueProperty
    extends AbstractComplexProperty {

  /**
   * The key.
   */
  private String fKey;

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public KeyValueProperty(ModelComponent model) {
    this(model, "", "");
  }

  /**
   * Creates a new instance with a key and value.
   *
   * @param model The model component.
   * @param key The key.
   * @param value The value.
   */
  public KeyValueProperty(ModelComponent model, String key, String value) {
    super(model);
    fKey = key;
    fValue = value;
  }

  @Override
  public Object getComparableValue() {
    return fKey + fValue;
  }

  /**
   * Set the key and the value.
   *
   * @param key The key
   * @param value The value
   */
  public void setKeyAndValue(String key, String value) {
    fKey = key;
    fValue = value;
  }

  /**
   * Returns the key.
   *
   * @return The key of this property.
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
