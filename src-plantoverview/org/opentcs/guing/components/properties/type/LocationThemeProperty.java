/*
 *
 * Created on 24.07.2013 10:29:22
 */
package org.opentcs.guing.components.properties.type;

import org.opentcs.guing.components.properties.panel.LocationThemePropertyEditorPanel;
import org.opentcs.guing.model.ModelComponent;

/**
 * A property containing the name of the currently used theme.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class LocationThemeProperty
    extends AbstractComplexProperty {

  /**
   * The name of the theme.
   */
  private String themeName = "";

  /**
   * Creates a new instance.
   *
   * @param model
   */
  public LocationThemeProperty(ModelComponent model) {
    super(model, LocationThemePropertyEditorPanel.class);
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
    LocationThemeProperty symbolProperty = (LocationThemeProperty) property;
    setTheme(symbolProperty.getTheme());
  }
}
