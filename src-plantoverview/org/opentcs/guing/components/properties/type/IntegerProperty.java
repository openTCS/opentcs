/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Attribut für einen ganzzahligen Wert.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class IntegerProperty
    extends AbstractProperty {

  /**
   * Creates a new instance of BooleanProperty
   *
   * @param model
   */
  public IntegerProperty(ModelComponent model) {
    this(model, 0);
  }

  /**
   * Konstruktor mit ganzzahligen Wert.
   *
   * @param model
   * @param value
   */
  public IntegerProperty(ModelComponent model, int value) {
    super(model);
    setValue(value);
  }

  @Override
  public Object getComparableValue() {
    return String.valueOf(fValue);
  }

  @Override
  public String toString() {
    return fValue instanceof Integer ? Integer.toString((int) fValue) : (String) fValue;
  }

  @Override
  public void copyFrom(Property property) {
    IntegerProperty integerProperty = (IntegerProperty) property;
    setValue(integerProperty.getValue());
  }
}
