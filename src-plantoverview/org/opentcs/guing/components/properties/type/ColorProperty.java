/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import java.awt.Color;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Farbattribut.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public final class ColorProperty
    extends AbstractProperty {

  /**
   * Die Farbe.
   */
  private Color fColor;

  /**
   * Creates a new instance of ColorProperty
   *
   * @param model
   */
  public ColorProperty(ModelComponent model) {
    this(model, Color.white);
  }

  /**
   * Konstruktor mit Übergabe einer Farbe.
   *
   * @param model
   *
   * @param color
   */
  public ColorProperty(ModelComponent model, Color color) {
    super(model);
    setColor(color);
  }

  /**
   * Setzt die Farbe.
   *
   * @param color
   */
  public void setColor(Color color) {
    fColor = color;
  }

  /**
   * Liefert die Farbe.
   *
   * @return
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
