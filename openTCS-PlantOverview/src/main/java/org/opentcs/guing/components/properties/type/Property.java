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

/**
 * Interface für Attribute. Die Attribute arbeiten als Wrapper, d.h. sie
 * erlauben den Zugriff auf ihre Daten, ohne dabei jedoch ein neues
 * Attribut-Objekt zu erzeugen. Die Datentypen String, Boolean, Integer usw.
 * bieten dieses nicht, so dass die Veränderung eines Attributs zu einem neuen
 * Attribut führen würde. Ergebnis wäre das ständige Setzen der Attribute im
 * Datenobjekt.
 * Vorteil der hier eingesetzten Methode ist, dass das Attribut-Objekt selbst
 * stets dasselbe bleibt, dessen Inhalt sich jedoch ändern lässt.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface Property
    extends ModelAttribute,
            Cloneable {

  /**
   * Übernimmt die Werte von dem übergebenen Attribut. Die Eigenschaften
   * Visibility, Editable usw. werden jedoch nicht übernommen.
   *
   * @param property
   */
  void copyFrom(Property property);

  /**
   * Returns a comparable represantation of the value of this property.
   *
   * @return A represantation to compare this property to other ones.
   */
  Object getComparableValue();

  /**
   * Klont das Property.
   *
   * @return
   */
  Object clone();
}
