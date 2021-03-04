/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.data.model.Triple;
import org.opentcs.guing.model.ModelComponent;
import org.slf4j.LoggerFactory;

/**
 * Ein Property fï¿½r einen 3D-Punkt. Der Datentyp fï¿½r die Koordinaten des Punktes
 * ist <code>long</code>.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class TripleProperty
    extends AbstractProperty {

  /**
   * Der Punkt.
   */
  private Triple fTriple;

  /**
   * Creates a new instance of PointProperty.
   *
   * @param model
   */
  public TripleProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    return fTriple.getX() + "," + fTriple.getY() + "," + fTriple.getZ();
  }

  /**
   * Setzt fï¿½r das Attribut einen neuen Wert und eine neue Maï¿½einheit. Eine
   * Ausnahme wird ausgelï¿½st, wenn es sich bei der Maï¿½einheit um keine mï¿½gliche
   * Einheit handelt.
   *
   * @param triple
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
      setValue(foreignTriple.clone());
    }
    catch (Exception e) {
      LoggerFactory.getLogger(TripleProperty.class).error("Exception", e);
    }
  }
}
