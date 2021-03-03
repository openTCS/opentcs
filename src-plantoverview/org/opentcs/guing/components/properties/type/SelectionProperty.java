/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.properties.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Ein Property, das einen Wert aus einer gegebenen Menge von Werten annehmen
 * kann.
 * Beispiel: links aus der Menge {links, rechts},
 * Nord aus der Menge {Nord, Süd, Ost, West}
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SelectionProperty
    extends AbstractProperty {

  /**
   * Die möglichen Werte.
   */
  private ArrayList fPossibleValues;

  /**
   * Standardkonstruktor.
   *
   * @param model
   */
  public SelectionProperty(ModelComponent model) {
    this(model, new String[] {}, "");
  }

  /**
   * Creates a new instance of SelectionProperty
   *
   * @param model
   *
   * @param possibleValues
   * @param value
   */
  public SelectionProperty(ModelComponent model, Object[] possibleValues, Object value) {
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
  public final void setPossibleValues(Object[] possibleValues) {
    fPossibleValues = new ArrayList();
    fPossibleValues.addAll(Arrays.asList(possibleValues));
  }

  @Override
  public void setValue(Object value) {
    if (fPossibleValues.contains(value)
        || value.equals(ResourceBundleUtil.getBundle().getString("PropertiesCollection.differentValues.text"))) {
      fValue = value;
    }
  }

  @Override
  public String toString() {
    return getValue().toString();
  }

  /**
   * Liefert die Menge der möglichen Werte.
   *
   * @return
   */
  public List getPossibleValues() {
    return fPossibleValues;
  }

  @Override
  public void copyFrom(Property property) {
    SelectionProperty selectionProperty = (SelectionProperty) property;
    setValue(selectionProperty.getValue());
  }
}
