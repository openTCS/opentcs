/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.properties.type;

import java.util.LinkedList;
import java.util.List;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein Attribut für Winkelangaben.
 * Beispiele: 0.1 rad, 30 deg
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AngleProperty
    extends AbstractQuantity<AngleProperty.Unit> {

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public AngleProperty(ModelComponent model) {
    this(model, Double.NaN, Unit.DEG);
  }

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   * @param value The property value.
   * @param unit The value's unit.
   */
  public AngleProperty(ModelComponent model, double value, Unit unit) {
    super(model, value, unit, Unit.class, relations());
  }

  @Override
  public Object getComparableValue() {
    return String.valueOf(fValue) + getUnit();
  }

  @Override
  public void setValue(Object newValue) {
    if (newValue instanceof Double) {
      super.setValue(((double) newValue) % 360);
    }
    else {
      super.setValue(newValue);
    }
  }

  @Override
  protected void initValidRange() {
    // max not neccessary as setValue computes % 360
    validRange.setMin(0);
  }

  private static List<Relation<Unit>> relations() {
    List<Relation<Unit>> relations = new LinkedList<>();
    relations.add(new Relation<>(Unit.DEG, Unit.RAD, 180.0 / Math.PI));
    relations.add(new Relation<>(Unit.RAD, Unit.DEG, Math.PI / 180.0));
    return relations;
  }

  public static enum Unit {
    DEG("deg"),
    RAD("rad");

    private final String displayString;

    private Unit(String displayString) {
      this.displayString = displayString;
    }

    @Override
    public String toString() {
      return displayString;
    }
  }
}
