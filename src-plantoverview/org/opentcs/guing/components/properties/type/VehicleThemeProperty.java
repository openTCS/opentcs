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
 * A property containing the name of the currently used theme.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleThemeProperty
    extends AbstractComplexProperty {

  /**
   * The name of the theme.
   */
  private String themeName = "";

  /**
   * Creates a new instance.
   *
   * @param model The model component this property belongs to.
   */
  public VehicleThemeProperty(ModelComponent model) {
    super(model);
  }

  @Override
  public Object getComparableValue() {
    return themeName;
  }

  /**
   * Sets the theme.
   *
   * @param theme Name of the theme.
   */
  public void setTheme(String theme) {
    this.themeName = theme;
  }

  /**
   * Returns the theme.
   *
   * @return Name of the theme.
   */
  public String getTheme() {
    return themeName;
  }

  @Override
  public String toString() {
    String[] parts = themeName.split("\\.");
    return parts.length == 0 ? themeName : parts[parts.length - 1];
  }

  @Override
  public void copyFrom(Property property) {
    VehicleThemeProperty symbolProperty = (VehicleThemeProperty) property;
    setTheme(symbolProperty.getTheme());
  }
}
