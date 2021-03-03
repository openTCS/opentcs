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

import org.opentcs.guing.model.ModelComponent;

/**
 * An attribute for coordinates.
 * Examples: 1 mm, 20 cm, 3.4 m, 17.98 km
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CoordinateProperty
    extends LengthProperty {

  /**
   * Creates a new instance of CoordinateProperty.
   *
   * @param model Point- or LocationModel.
   */
  public CoordinateProperty(ModelComponent model) {
    this(model, 0, Unit.MM);
  }

  /**
   * Creates a new instance of CoordinateProperty.
   *
   * @param model Point- or LocationModel.
   * @param value The initial value.
   * @param unit The initial unit.
   */
  public CoordinateProperty(ModelComponent model, double value, Unit unit) {
    super(model, value, unit);
  }
  
  @Override
  protected void initValidRange() {
    validRange.setMin(Double.NEGATIVE_INFINITY);
  }
}
