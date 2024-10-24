// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import java.awt.Color;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A color property.
 */
public final class ColorProperty
    extends
      AbstractProperty {

  /**
   * The color.
   */
  private Color fColor;

  /**
   * Create a new instance with a color.
   *
   * @param model The model component.
   * @param color The color.
   */
  public ColorProperty(ModelComponent model, Color color) {
    super(model);
    setColor(color);
  }

  /**
   * Set the color.
   *
   * @param color The color
   */
  public void setColor(Color color) {
    fColor = color;
  }

  /**
   * Returns the color.
   *
   * @return The color.
   */
  public Color getColor() {
    return fColor;
  }

  @Override // Property
  public Object getComparableValue() {
    return fColor;
  }

  @Override // AbstractProperty
  public void copyFrom(Property property) {
    ColorProperty colorProperty = (ColorProperty) property;
    setColor(colorProperty.getColor());
  }
}
