/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.PeripheralOperationModel;

/**
 * A property that contains a list of Peripheral operations.
 */
public class PeripheralOperationsProperty
    extends AbstractComplexProperty {

  public PeripheralOperationsProperty(ModelComponent model,
                                      List<PeripheralOperationModel> operations) {
    super(model);
    fValue = operations;
  }

  @Override
  public Object getComparableValue() {
    return this.toString();
  }

  @Override
  public String toString() {
    return getValue().stream()
        .map(op -> op.getLocationName() + ": " + op.getOperation())
        .collect(Collectors.joining(", "));
  }

  @Override
  public void copyFrom(Property property) {
    PeripheralOperationsProperty other = (PeripheralOperationsProperty) property;
    setValue(new ArrayList<>(other.getValue()));
  }

  @Override
  public Object clone() {
    PeripheralOperationsProperty clone = (PeripheralOperationsProperty) super.clone();
    clone.setValue(new ArrayList<>(getValue()));
    return clone;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<PeripheralOperationModel> getValue() {
    return (List) super.getValue();
  }
}
