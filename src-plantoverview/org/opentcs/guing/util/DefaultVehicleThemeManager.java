/*
 *
 * Created on 20.08.2013 11:50:30
 */
package org.opentcs.guing.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.gui.plugins.VehicleTheme;
import org.opentcs.util.gui.plugins.VehicleThemeRegistry;

/**
 * Provides utility for vehicle themes.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DefaultVehicleThemeManager
    implements VehicleThemeManager {

  /**
   * This class's logger.
   */
  private static final Logger logger
      = Logger.getLogger(DefaultVehicleThemeManager.class.getName());
  /**
   * This class's configuration store.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(OpenTCSView.class.getName());
  /**
   * The manager instance.
   */
  private static final DefaultVehicleThemeManager instance
      = new DefaultVehicleThemeManager();
  /**
   * Config store key.
   */
  private static final String CONFIGSTOREKEY = ConfigConstants.VEHICLE_THEME;
  /**
   * The available themes.
   */
  private final List<VehicleTheme> themes;
  /**
   * The default theme.
   */
  private VehicleTheme defaultTheme;
  /**
   * Default theme saved by the client.
   */
  private VehicleTheme defaultConfigStoreTheme;
  /**
   * The theme property defined by the visual layout.
   */
  private VehicleThemeProperty themeProperty;

  /**
   * Creates a new instance.
   */
  private DefaultVehicleThemeManager() {
    this.themes = (new VehicleThemeRegistry()).getThemes();
    evaluateClientDefaultTheme();
  }

  /**
   * Returns the single instance of this theme manager.
   *
   * @return The single instance of this theme manager.
   */
  public static VehicleThemeManager getInstance() {
    return instance;
  }

  @Override
  public List<VehicleTheme> getThemes() {
    return themes;
  }

  @Override
  public void setThemeProperty(VehicleThemeProperty property) {
    defaultTheme = null;
    defaultConfigStoreTheme = null;
    themeProperty = property;
    String configStoreValue = configStore.getString(CONFIGSTOREKEY, "undefined");

    if (!configStoreValue.equals("undefined")) {
      for (VehicleTheme theme : themes) {
        if (theme.getName().equals(configStoreValue)) {
          defaultTheme = theme;
          defaultConfigStoreTheme = theme;
        }
      }
    }
    else {
      for (VehicleTheme theme : themes) {
        if (theme.getClass().getName().equals(themeProperty.getTheme())) {
          defaultTheme = theme;
        }
      }
    }

    if (defaultTheme == null) {
      if (!themes.isEmpty()) {
        logger.log(Level.WARNING, "Theme with name {0} not found. Using {1}",
                   new Object[] {themeProperty.getTheme(), themes.get(0).getClass().getName()});
        defaultTheme = themes.get(0);
      }
      else {
        logger.log(Level.SEVERE, "Theme with name {0} not found and no other factory available.", themeProperty.getTheme());
      }
    }
  }

  @Override
  @Deprecated
  public Set<String> getAllImagePaths() {
    // XXX This method probably does not belong into this class.
    Set<String> images = new HashSet<>();

    for (VehicleTheme theme : getThemes()) {
      images.addAll(theme.getAllImagePaths());
    }

    return images;
  }

  @Override
  public void updateDefaultTheme(VehicleTheme theme) {
    if (themes.contains(theme)) {
      defaultTheme = theme;
      configStore.setString(CONFIGSTOREKEY, theme.getName());
    }
    else {
      configStore.setString(CONFIGSTOREKEY, "undefined");
      updateDefaultTheme();
    }
  }

  @Override
  public void updateDefaultTheme() {
    setThemeProperty(themeProperty);
  }

  @Override
  public VehicleTheme getDefaultTheme() {
    return defaultTheme;
  }

  @Override
  public VehicleTheme getDefaultConfigStoreTheme() {
    return defaultConfigStoreTheme;
  }

  private void evaluateClientDefaultTheme() {
    String configStoreValue = configStore.getString(CONFIGSTOREKEY, "undefined");
    if (configStoreValue.equals("undefined")) {
      return;
    }

    for (VehicleTheme theme : themes) {
      if (theme.getName().equals(configStoreValue)) {
        defaultTheme = theme;
        defaultConfigStoreTheme = theme;
      }
    }
  }
}
