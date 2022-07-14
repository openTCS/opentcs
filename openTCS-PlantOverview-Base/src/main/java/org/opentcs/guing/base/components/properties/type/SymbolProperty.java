/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for a graphical symbol.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class SymbolProperty
    extends AbstractComplexProperty {

  /**
   * The location representation.
   */
  private LocationRepresentation locationRepresentation;

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
    return locationRepresentation;
  }

  /**
   * Set the location representation for this property.
   *
   * @param locationRepresentation The location representation.
   */
  public void setLocationRepresentation(LocationRepresentation locationRepresentation) {
    this.locationRepresentation = locationRepresentation;
  }

  /**
   * Returns the location representation for this property.
   *
   * @return The location representation.
   */
  public LocationRepresentation getLocationRepresentation() {
    return locationRepresentation;
  }

  @Override // java.lang.Object
  public String toString() {
    if (fValue != null) {
      return fValue.toString();
    }

    return locationRepresentation == null ? "" : locationRepresentation.name();
  }

  @Override // AbstractProperty
  public void copyFrom(Property property) {
    SymbolProperty symbolProperty = (SymbolProperty) property;
    symbolProperty.setValue(null);
    setLocationRepresentation(symbolProperty.getLocationRepresentation());
  }
}
