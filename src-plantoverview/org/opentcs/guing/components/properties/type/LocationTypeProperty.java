/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
*/
package org.opentcs.guing.components.properties.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A property that can take a value from a given set of location types.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class LocationTypeProperty extends AbstractProperty implements Selectable<String> {
  
  private List<String> fPossibleValues;

  public LocationTypeProperty(ModelComponent model) {
    this(model, new ArrayList<>(), "");
  }
  
  public LocationTypeProperty(ModelComponent model, List<String> possibleValues, Object value) {
    super(model);
    setPossibleValues(possibleValues);
    fValue = value;
  }

  @Override
  public Object getComparableValue() {
    return fValue;
  }
  
  @Override
  public final void setPossibleValues(List<String> possibleValues) {
    fPossibleValues = Objects.requireNonNull(possibleValues, "possibleValues is null");
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

  @Override
  public List getPossibleValues() {
    return fPossibleValues;
  }

  @Override
  public void copyFrom(Property property) {
    LocationTypeProperty locTypeProperty = (LocationTypeProperty) property;
    setValue(locTypeProperty.getValue());
  }
}
