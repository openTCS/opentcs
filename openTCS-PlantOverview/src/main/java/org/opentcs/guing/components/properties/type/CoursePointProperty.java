/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.model.ModelComponent;

/**
 * Property containing a point in the driving course.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CoursePointProperty
    extends AbstractComplexProperty {

  private String pointName;

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public CoursePointProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    return pointName;
  }

  /**
   * Setzt den Namen des Punktes.
   *
   * @param name Der Name des Punktes.
   */
  public void setPointName(String name) {
    pointName = name;
  }

  /**
   * Liefert den Namen des Punktes.
   *
   * @return Den Namen des Punktes.
   */
  public String getPointName() {
    return pointName;
  }

  @Override
  public String toString() {
    return pointName;
  }

  @Override
  public void copyFrom(Property property) {
    CoursePointProperty symbolProperty = (CoursePointProperty) property;
    setPointName(symbolProperty.getPointName());
  }
}
