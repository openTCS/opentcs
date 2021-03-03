/*
 *
 * Created on 21.08.2013 09:44:24
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.components.properties.panel.CoursePointPropertyEditorPanel;
import org.opentcs.guing.model.ModelComponent;

/**
 * Property, das einen Punkt enthält.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class CoursePointProperty
    extends AbstractComplexProperty {

  private String pointName;

  /**
   * Creates a new instance.
   *
   * @param model
   */
  public CoursePointProperty(ModelComponent model) {
    super(model, CoursePointPropertyEditorPanel.class);
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
