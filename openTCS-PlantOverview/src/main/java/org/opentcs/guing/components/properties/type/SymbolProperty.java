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

import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.model.ModelComponent;

/**
 * Ein grafisches Symbol.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SymbolProperty
    extends AbstractComplexProperty {

  /**
   * Der Dateiname des grafischen Symbols.
   */
  private LocationRepresentation fFile;

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public SymbolProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    return fFile;
  }

  /**
   * Setzt den Namen der Datei, welche das grafische Symbol enthält.
   *
   * @param filename der Name der Bilddatei
   */
  public void setLocationRepresentation(LocationRepresentation filename) {
    fFile = filename;
  }

  /**
   * Liefert den Namen der Bilddatei mit dem grafischen Symbol.
   *
   * @return den Namen der Bilddatei
   */
  public LocationRepresentation getLocationRepresentation() {
    return fFile;
  }

  @Override // java.lang.Object
  public String toString() {
    if (fValue != null) {
      return fValue.toString();
    }

    return fFile == null ? "" : fFile.name();
  }

  @Override // AbstractProperty
  public void copyFrom(Property property) {
    SymbolProperty symbolProperty = (SymbolProperty) property;
    symbolProperty.setValue(null); // Text "<different Values>" löschen
    setLocationRepresentation(symbolProperty.getLocationRepresentation());
  }
}
