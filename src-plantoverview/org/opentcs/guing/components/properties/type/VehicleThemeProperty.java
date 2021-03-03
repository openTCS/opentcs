/*
 *
 * Created on 11.09.2013 11:59:48
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.components.properties.panel.VehicleThemePropertyEditorPanel;
import org.opentcs.guing.model.ModelComponent;

/**
 * A property containing the name of the currently used theme.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class VehicleThemeProperty
    extends AbstractComplexProperty {

  /**
   * The name of the theme.
   */
  private String themeName = "";

  /**
   *
   * @param model
   */
  public VehicleThemeProperty(ModelComponent model) {
    super(model, VehicleThemePropertyEditorPanel.class);
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
