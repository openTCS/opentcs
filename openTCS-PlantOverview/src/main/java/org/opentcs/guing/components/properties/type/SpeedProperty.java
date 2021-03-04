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
 * Ein Attribut für Geschwindigkeitsangaben.
 * Beispiele: 1 m/s, 2.2 km/h
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SpeedProperty
    extends AbstractQuantity<SpeedProperty.Unit> {

  /**
   * Creates a new instance of SpeedProperty
   *
   * @param model
   */
  public SpeedProperty(ModelComponent model) {
    this(model, 0, Unit.M_S);
  }

  /**
   * Konstruktor mit Wert und Maßeinheit.
   *
   * @param model
   * @param value
   * @param unit
   */
  public SpeedProperty(ModelComponent model, double value, Unit unit) {
    super(model, value, unit, Unit.class, relations());
    // MaxVelocity and MaxReverseVelocity of a path must not be negative.
    setUnsigned(true);
  }

  @Override
  public Object getComparableValue() {
    return String.valueOf(fValue) + getUnit();
  }

  private static List<Relation<Unit>> relations() {
    List<Relation<Unit>> relations = new LinkedList<>();
    relations.add(new Relation<>(Unit.KM_H, Unit.M_S, 3.6));
    relations.add(new Relation<>(Unit.M_S, Unit.MM_S, 0.001));
    return relations;
  }

  @Override
  protected void initValidRange() {
    validRange.setMin(0);
  }

  public static enum Unit {

    KM_H("km/h"),
    M_S("m/s"),
    MM_S("mm/s");

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
