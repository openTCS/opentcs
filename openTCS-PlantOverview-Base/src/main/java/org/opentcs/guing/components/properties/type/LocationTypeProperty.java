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
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;
import org.opentcs.guing.model.ModelComponent;

/**
 * A property that can take a value from a given set of location types.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class LocationTypeProperty
    extends AbstractProperty
    implements Selectable<String> {

  private List<String> fPossibleValues;
  /**
   * This class's resource bundle.
   */
  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  public LocationTypeProperty(ModelComponent model) {
    this(model, new ArrayList<>(), "");
  }

  public LocationTypeProperty(ModelComponent model, List<String> possibleValues, Object value) {
    super(model);
    this.fPossibleValues = requireNonNull(possibleValues, "possibleValues");
    fValue = value;
  }

  @Override
  public Object getComparableValue() {
    return fValue;
  }

  @Override
  public void setValue(Object value) {
    if (fPossibleValues.contains(value)
        || value.equals(bundle.getString("PropertiesCollection.differentValues.text"))) {
      super.setValue(value);
    }
  }

  @Override
  public List<String> getPossibleValues() {
    return fPossibleValues;
  }

  @Override
  public void setPossibleValues(List<String> possibleValues) {
    fPossibleValues = requireNonNull(possibleValues, "possibleValues");
  }

  @Override
  public void copyFrom(Property property) {
    LocationTypeProperty locTypeProperty = (LocationTypeProperty) property;
    setValue(locTypeProperty.getValue());
  }

  @Override
  public String toString() {
    return getValue().toString();
  }
}
