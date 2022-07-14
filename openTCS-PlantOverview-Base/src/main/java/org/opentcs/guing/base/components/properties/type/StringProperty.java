/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import static java.util.Objects.requireNonNull;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for a string.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StringProperty
    extends AbstractProperty {

  /**
   * The string.
   */
  private String fText;

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public StringProperty(ModelComponent model) {
    this(model, "");
  }

  /**
   * Creates a new instance with a value.
   *
   * @param model The model component.
   * @param text The text.
   */
  public StringProperty(ModelComponent model, String text) {
    super(model);
    fText = requireNonNull(text, "text");
  }

  @Override
  public Object getComparableValue() {
    return fText;
  }

  /**
   * Set the string.
   *
   * @param text the new string.
   */
  public void setText(String text) {
    fText = text;
  }

  /**
   * Returns the string.
   *
   * @return The String.
   */
  public String getText() {
    return fText;
  }

  @Override
  public String toString() {
    return fText;
  }

  @Override
  public void copyFrom(Property property) {
    StringProperty stringProperty = (StringProperty) property;
    setText(stringProperty.getText());
  }
}
