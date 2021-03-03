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
 * Ein Attribut für Längenangaben.
 * Beispiele: 1 mm, 20 cm, 3.4 m, 17.98 km
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LengthProperty
    extends AbstractQuantity<LengthProperty.Unit> {

  /**
   * Creates a new instance of LengthProperty
   *
   * @param model
   */
  public LengthProperty(ModelComponent model) {
    this(model, 0, Unit.MM);
  }

  /**
   * Konstruktor mit Wert und Maßeinheit.
   *
   * @param model
   * @param value
   * @param unit
   */
  public LengthProperty(ModelComponent model, double value, Unit unit) {
    super(model, value, unit, Unit.class, relations());
  }

  @Override // Property
  public Object getComparableValue() {
    return String.valueOf(fValue) + getUnit();
  }

  private static List<Relation<Unit>> relations() {
    List<Relation<Unit>> relations = new LinkedList<>();
    relations.add(new Relation<>(Unit.MM, Unit.CM, 10));
    relations.add(new Relation<>(Unit.CM, Unit.M, 100));
    relations.add(new Relation<>(Unit.M, Unit.KM, 1000));
    return relations;
  }

  @Override
  protected void initValidRange() {
    validRange.setMin(0);
  }

  public static enum Unit {

    MM("mm"),
    CM("cm"),
    M("m"),
    KM("km");

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
