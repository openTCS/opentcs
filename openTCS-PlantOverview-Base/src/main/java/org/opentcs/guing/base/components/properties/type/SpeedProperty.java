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
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for speeds.
 */
public class SpeedProperty
    extends AbstractQuantity<SpeedProperty.Unit> {

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public SpeedProperty(ModelComponent model) {
    this(model, 0, Unit.M_S);
  }

  /**
   * Creates a new instance with a value and a unit.
   *
   * @param model The model component.
   * @param value The value.
   * @param unit The unit.
   */
  public SpeedProperty(ModelComponent model, double value, Unit unit) {
    super(model, value, unit, Unit.class, relations());
    setUnsigned(true);
  }

  @Override
  public Object getComparableValue() {
    return String.valueOf(fValue) + getUnit();
  }

  private static List<Relation<Unit>> relations() {
    List<Relation<Unit>> relations = new ArrayList<>();
    relations.add(new Relation<>(Unit.KM_H, Unit.M_S, 3.6));
    relations.add(new Relation<>(Unit.M_S, Unit.MM_S, 0.001));
    return relations;
  }

  @Override
  protected void initValidRange() {
    validRange.setMin(0);
  }

  /**
   * Supported speed units.
   */
  public enum Unit {
    /**
     * Kilometers per hour.
     */
    KM_H("km/h"),
    /**
     * Meters per second.
     */
    M_S("m/s"),
    /**
     * Millimeters per second.
     */
    MM_S("mm/s");

    private final String displayString;

    Unit(String displayString) {
      this.displayString = displayString;
    }

    @Override
    public String toString() {
      return displayString;
    }
  }
}
