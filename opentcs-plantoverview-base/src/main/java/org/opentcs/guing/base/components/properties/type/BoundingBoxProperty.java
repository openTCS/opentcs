/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import static org.opentcs.util.Assertions.checkArgument;

import org.opentcs.guing.base.model.BoundingBoxModel;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property that contains a bounding box.
 */
public class BoundingBoxProperty
    extends
      AbstractComplexProperty {

  public BoundingBoxProperty(ModelComponent model, BoundingBoxModel boundingBox) {
    super(model);
    fValue = boundingBox;
  }

  @Override
  public Object getComparableValue() {
    return this.toString();
  }

  @Override
  public void copyFrom(Property property) {
    BoundingBoxProperty other = (BoundingBoxProperty) property;
    setValue(other.getValue());
  }

  @Override
  public Object clone() {
    BoundingBoxProperty clone = (BoundingBoxProperty) super.clone();
    clone.setValue(getValue());
    return clone;
  }

  @Override
  public String toString() {
    return String.format(
        "(%s, %s, %s), offset: (%s, %s)",
        getValue().getLength(),
        getValue().getWidth(),
        getValue().getHeight(),
        getValue().getReferenceOffset().getX(),
        getValue().getReferenceOffset().getY()
    );
  }

  @Override
  public BoundingBoxModel getValue() {
    return (BoundingBoxModel) super.getValue();
  }

  @Override
  public void setValue(Object newValue) {
    checkArgument(
        newValue instanceof BoundingBoxModel,
        "newValue is not an instance of BoundingBoxModel"
    );

    super.setValue(newValue);
  }
}
