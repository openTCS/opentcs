/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.io.Serializable;

/**
 * A conversion relationship between two units.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Relation<U>
    implements Serializable {

  /**
   * The source unit.
   */
  private final U fUnitFrom;
  /**
   * The destination unit.
   */
  private final U fUnitTo;
  /**
   * The conversion relationship.
   */
  private final double fRelationValue;

  /**
   * Creates a new instance.
   *
   * @param unitFrom The unit from which to convert.
   * @param unitTo The unit to which to convert.
   * @param relationValue The relation value between the two units.
   */
  public Relation(U unitFrom, U unitTo, double relationValue) {
    fUnitFrom = unitFrom;
    fUnitTo = unitTo;
    fRelationValue = relationValue;
  }

  /**
   * Checks if this relation is applicable to the specified units.
   *
   * @param unitA The first unit.
   * @param unitB The second unit.
   * @return {@code true}, if the two given units are covered by this relation, otherwise
   * {@code false}.
   */
  public boolean fits(U unitA, U unitB) {
    if (fUnitFrom.equals(unitA) && fUnitTo.equals(unitB)) {
      return true;
    }

    if (fUnitFrom.equals(unitB) && fUnitTo.equals(unitA)) {
      return true;
    }

    return false;
  }

  /**
   * Returns the conversion relationship as a number.
   *
   * @return The relation value between the two units. 
   */
  public double relationValue() {
    return fRelationValue;
  }

  /**
   * Returns the operation used for the conversion of the first unit into the second unit.
   *
   * @param unitFrom The unit from which to convert.
   * @param unitTo The unit to which to convert.
   * @return The operation that is to be used for the conversion.
   */
  public Operation getOperation(U unitFrom, U unitTo) {
    if (unitFrom.equals(fUnitFrom)) {
      return Operation.DIVISION;
    }
    else {
      return Operation.MULTIPLICATION;
    }
  }

  public static enum Operation {
    DIVISION,
    MULTIPLICATION
  }
}
