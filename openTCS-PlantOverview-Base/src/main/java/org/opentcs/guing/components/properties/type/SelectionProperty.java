/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Property, das einen Wert aus einer gegebenen Menge von Werten annehmen
 * kann.
 * Beispiel: links aus der Menge {links, rechts},
 * Nord aus der Menge {Nord, Süd, Ost, West}
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @param <E> The type of the enum.
 */
public class SelectionProperty<E extends Enum<E>>
    extends AbstractProperty
    implements Selectable<E> {

  /**
   * Die möglichen Werte.
   */
  private List<E> fPossibleValues;
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  /**
   * Standardkonstruktor.
   *
   * @param model
   */
  public SelectionProperty(ModelComponent model) {
    this(model, new ArrayList<>(), "");
  }

  /**
   * Creates a new instance of SelectionProperty
   *
   * @param model
   *
   * @param possibleValues
   * @param value
   */
  public SelectionProperty(ModelComponent model, List<E> possibleValues,
                           Object value) {
    super(model);
    setPossibleValues(possibleValues);
    fValue = value;
  }

  @Override
  public Object getComparableValue() {
    return fValue;
  }

  /**
   * Setzt die möglichen Werte im Nachhinein.
   *
   * @param possibleValues Ein Array mit den möglichen Werte.
   */
  @Override
  public void setPossibleValues(List<E> possibleValues) {
    fPossibleValues = Objects.requireNonNull(possibleValues, "possibleValues is null");
  }

  @Override
  public void setValue(Object value) {
    if (fPossibleValues.contains(value)
        || value.equals(bundle.getString("PropertiesCollection.differentValues.text"))) {
      fValue = value;
    }
  }

  @Override
  public String toString() {
    return getValue().toString();
  }

  @Override
  public List<E> getPossibleValues() {
    return fPossibleValues;
  }

  @Override
  public void copyFrom(Property property) {
    AbstractProperty selectionProperty = (AbstractProperty) property;
    setValue(selectionProperty.getValue());
  }
}
