/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.data.model.Triple;
import org.opentcs.guing.base.model.ModelComponent;
import org.slf4j.LoggerFactory;

/**
 * A property for a 3 dimensional point.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class TripleProperty
    extends AbstractProperty {

  /**
   * The point.
   */
  private Triple fTriple;

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public TripleProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    return fTriple.getX() + "," + fTriple.getY() + "," + fTriple.getZ();
  }

  /**
   * Set the value of this property.
   *
   * @param triple The triple.
   */
  public void setValue(Triple triple) {
    fTriple = triple;
  }

  @Override
  public Triple getValue() {
    return fTriple;
  }

  @Override
  public String toString() {
    return fTriple == null ? "null"
        : String.format("(%d, %d, %d)", fTriple.getX(), fTriple.getY(), fTriple.getZ());
  }

  @Override
  public void copyFrom(Property property) {
    TripleProperty tripleProperty = (TripleProperty) property;

    try {
      Triple foreignTriple = tripleProperty.getValue();
      setValue(foreignTriple);
    }
    catch (Exception e) {
      LoggerFactory.getLogger(TripleProperty.class).error("Exception", e);
    }
  }
}
