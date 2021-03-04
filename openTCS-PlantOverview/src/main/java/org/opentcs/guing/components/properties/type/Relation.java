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

import java.io.Serializable;

/**
 * Ein Umwandlungsverhältnis zwischen zwei Einheiten.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Relation<U>
    implements Serializable {

  /**
   * Die Maßeinheit von der umgewandelt wird.
   */
  private final U fUnitFrom;
  /**
   * Die Maßeinheit, in die umgewandelt wird.
   */
  private final U fUnitTo;
  /**
   * Das Umwandlungsverhältnis.
   */
  private final double fRelationValue;

  /**
   * Creates a new instance.
   *
   * @param unitFrom
   * @param unitTo
   * @param relationValue
   */
  public Relation(U unitFrom, U unitTo, double relationValue) {
    fUnitFrom = unitFrom;
    fUnitTo = unitTo;
    fRelationValue = relationValue;
  }

  /**
   * Prüft, ob das Umwandlungsverhältnis für die beiden übergebenen Einheiten
   * passend ist. Liefert <code>true</code> zurück, falls ja.
   *
   * @param unitA
   * @param unitB
   * @return
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
   * Liefert das Umwandlungsverhältnis als eine Zahl.
   *
   * @return
   */
  public double relationValue() {
    return fRelationValue;
  }

  /**
   * Liefert die Rechenoperation, die für die Umwandlung der ersten Einheit in
   * die zweite Einheit angewendet werden muss. In Frage kommt die
   * Multiplikation und die Division. Zurückgeliefert wird nicht die Operation
   * im eigentlichen Sinne, sondern nur ein beschreibender Text aus der Menge
   * {"multiplication", "division"}.
   *
   * @param unitFrom
   * @param unitTo
   * @return
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
