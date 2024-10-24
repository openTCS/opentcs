// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.components.properties.type;

import static org.opentcs.util.Assertions.checkArgument;

import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that contains an {@link EnergyLevelThresholdSetModel}.
 */
public class EnergyLevelThresholdSetProperty
    extends
      AbstractComplexProperty {

  public EnergyLevelThresholdSetProperty(
      ModelComponent model,
      EnergyLevelThresholdSetModel energyLevelThresholdSet
  ) {
    super(model);
    fValue = energyLevelThresholdSet;
  }

  @Override
  public Object getComparableValue() {
    return this.toString();
  }

  @Override
  public void copyFrom(Property property) {
    EnergyLevelThresholdSetProperty other = (EnergyLevelThresholdSetProperty) property;
    setValue(other.getValue());
  }

  @Override
  public Object clone() {
    EnergyLevelThresholdSetProperty clone = (EnergyLevelThresholdSetProperty) super.clone();
    clone.setValue(getValue());
    return clone;
  }

  @Override
  public String toString() {
    return String.format(
        "(%s%%, %s%%, %s%%, %s%%)",
        getValue().getEnergyLevelCritical(),
        getValue().getEnergyLevelGood(),
        getValue().getEnergyLevelSufficientlyRecharged(),
        getValue().getEnergyLevelFullyRecharged()
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public EnergyLevelThresholdSetModel getValue() {
    return (EnergyLevelThresholdSetModel) super.getValue();
  }

  @Override
  public void setValue(Object newValue) {
    checkArgument(
        newValue instanceof EnergyLevelThresholdSetModel,
        "newValue is not an instance of EnergyLevelThresholdSetModel"
    );

    super.setValue(newValue);
  }
}
