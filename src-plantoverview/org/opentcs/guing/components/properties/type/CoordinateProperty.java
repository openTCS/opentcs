/**
 * (c): IML, IFAK.
 *
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
   * @param isXAxis If this property will represent the x-axis.
   */
  public CoordinateProperty(ModelComponent model, boolean isXAxis) {
    this(model, 0, Unit.MM, isXAxis);
  }

  /**
   * Creates a new instance of CoordinateProperty.
   *
   * @param model Point- or LocationModel.
   * @param value The initial value.
   * @param unit The initial unit.
   * @param isXAxis If this property will represent the x-axis.
   */
  public CoordinateProperty(ModelComponent model, double value, Unit unit, boolean isXAxis) {
    super(model, value, unit);
  }
  
  @Override
  protected void initValidRange() {
    validRange.setMin(Double.NEGATIVE_INFINITY);
  }
}
