/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.awt.Rectangle;
import java.util.Locale;
import javax.inject.Inject;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.guing.exchange.ConnectionParamSet;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * Basically implements a wrapper around a configuration store that makes
 * accessing configuration values more comfortable and the configuration easily
 * injectable.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ApplicationConfiguration
    implements ConfigConstants {

  /**
   * This class' configuration store.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(ApplicationConfiguration.class.getName());

  /**
   * Creates a new instance.
   */
  @Inject
  public ApplicationConfiguration() {
  }

  /**
   * Checks whether reported precise positions should be ignored when displaying
   * vehicles.
   *
   * @return Whether reported precise positions should be ignored.
   */
  public boolean getIgnoreVehiclePrecisePosition() {
    return configStore.getBoolean("ignoreVehiclePrecisePosition", false);
  }

  /**
   * Sets reported precise positions to be ignored or not.
   *
   * @param value Whether to ignore precise positions or not.
   */
  public void setIgnoreVehiclePrecisePosition(boolean value) {
    configStore.setBoolean("ignoreVehiclePrecisePosition", value);
  }

  /**
   * Checks whether reported orientation angles should be ignored when
   * displaying vehicles.
   *
   * @return Whether reported orientation angles should be ignored.
   */
  public boolean getIgnoreVehicleOrientationAngle() {
    return configStore.getBoolean("ignoreVehicleOrientationAngle", false);
  }

  /**
   * Sets reported orientation angles to be ignored or not.
   *
   * @param value Whether to ignore orientation angles or not.
   */
  public void setIgnoreVehicleOrientationAngle(boolean value) {
    configStore.setBoolean("ignoreVehicleOrientationAngle", value);
  }

  /**
   * Returns the configured locale.
   *
   * @return The configured locale.
   */
  public Locale getLocale() {
    switch (configStore.getString("locale", "English").toLowerCase()) {
      case "german":
        return Locale.GERMAN;
      case "english":
      default:
        return Locale.ENGLISH;
    }
  }

  /**
   * Sets the configured locale.
   *
   * @param value The configured locale.
   */
  public void setLocale(Locale value) {
    String localeString = "English";
    if (value == Locale.GERMAN) {
      localeString = "German";
    }
    configStore.setString("locale", localeString);
  }

  /**
   * Returns the name of the configured location theme.
   *
   * @return The name of the configured location theme, or the empty string, if
   * none is configured.
   */
  public String getLocationThemeName() {
    return configStore.getString("locationThemeName", "");
  }

  /**
   * Sets the name of the configured location theme.
   *
   * @param value The name of the configured location theme.
   */
  public void setLocationThemeName(String value) {
    configStore.setString("locationThemeName", value);
  }

  /**
   * Returns the name of the configured vehicle theme.
   *
   * @return The name of the configured vehicle theme, or the empty string, if
   * none is configured.
   */
  public String getVehicleThemeName() {
    return configStore.getString("vehicleThemeName", "");
  }

  /**
   * Sets the name of the configured vehicle theme.
   *
   * @param value The name of the configured vehicle theme.
   */
  public void setVehicleThemeName(String value) {
    configStore.setString("vehicleThemeName", value);
  }

  /**
   * Returns the GUI window's configured extended state.
   *
   * @return The GUI windows's configured extended state.
   */
  public int getFrameExtendedState() {
    return configStore.getInt("frameExtendedState", 0);
  }

  /**
   * Sets the GUI window's configured extended state.
   *
   * @param value The configured extended state.
   */
  public void setFrameExtendedState(int value) {
    configStore.setInt("frameExtendedState", value);
  }

  /**
   * Returns the GUI window's configured dimensions.
   *
   * @return The GUI windows's configured dimensions.
   */
  public Rectangle getFrameBounds() {
    int xPos = configStore.getInt("frameBoundsX", 0);
    int yPos = configStore.getInt("frameBoundsY", 0);
    int width = configStore.getInt("frameBoundsWidth", 1024);
    int height = configStore.getInt("frameBoundsHeight", 768);
    return new Rectangle(xPos, yPos, width, height);
  }

  /**
   * Sets the GUI window's configured dimensions.
   *
   * @param value The configured dimensions.
   */
  public void setFrameBounds(Rectangle value) {
    configStore.setInt("frameBoundsX", value.x);
    configStore.setInt("frameBoundsY", value.y);
    configStore.setInt("frameBoundsWidth", value.width);
    configStore.setInt("frameBoundsHeight", value.height);
  }

  /**
   * Returns the name of the last model loaded.
   *
   * @return The name of the last model loaded, or the empty string, if none was
   * saved in the configuration.
   */
  public String getLastLoadedModelName() {
    return configStore.getString("lastLoadedModelName", "");
  }

  /**
   * Sets the name of the last model loaded.
   *
   * @param value The name of the last model loaded.
   */
  public void setLastLoadedModelName(String value) {
    configStore.setString("lastLoadedModelName", value);
  }

  /**
   * Returns a bookmark for the drawing view with the given index.
   *
   * @param index The drawing view's index.
   * @return A bookmark for the drawing view with the given index.
   */
  public ViewBookmark getDrawingViewBookmark(int index) {
    ViewBookmark bookmark = new ViewBookmark();

    bookmark.setCenterX(configStore.getInt("viewBookmark_" + index + "_centerX", 0));
    bookmark.setCenterY(configStore.getInt("viewBookmark_" + index + "_centerY", 0));
    bookmark.setViewScaleX(configStore.getDouble("viewBookmark_" + index + "_scaleX", 0));
    bookmark.setViewScaleY(configStore.getDouble("viewBookmark_" + index + "_scaleY", 0));

    return bookmark;
  }

  /**
   * Sets a bookmark for the drawing view with the given index.
   *
   * @param index The drawing view's index.
   * @param value The bookmark.
   */
  public void setDrawingViewBookmark(int index, ViewBookmark value) {
    configStore.setInt("viewBookmark_" + index + "_centerX", value.getCenterX());
    configStore.setInt("viewBookmark_" + index + "_centerY", value.getCenterY());
    configStore.setDouble("viewBookmark_" + index + "_scaleX", value.getViewScaleX());
    configStore.setDouble("viewBookmark_" + index + "_scaleY", value.getViewScaleY());
  }

  /**
   * Returns the configured kernel connection bookmark with the given index.
   *
   * @param index The bookmark's index.
   * @return The configured kernel connection bookmark with the given index.
   */
  public ConnectionParamSet getConnectionParamSet(int index) {
    String[] configStrings
        = configStore.getString("connectionBookmark_" + index, "").split(":");

    if (configStrings.length != 2) {
      return null;
    }

    try {
      return new ConnectionParamSet(configStrings[0], configStrings[1]);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Sets the kernel connection bookmark with the given index.
   *
   * @param index The bookmark's index.
   * @param value The bookmark.
   */
  public void setConnectionParamSet(int index, ConnectionParamSet value) {
    configStore.setString("connectionBookmark_" + index,
                          value.getHost() + ":" + value.getPort());
  }

  /**
   * Returns the configured number of drawing views.
   *
   * @return The configured number of drawing views.
   */
  public int getDrawingViewCount() {
    return Math.max(1, configStore.getInt("drawingViewCount", 1));
  }

  /**
   * Sets the configured number of drawing views.
   *
   * @param value The number of drawing views.
   */
  public void setDrawingViewCount(int value) {
    configStore.setInt("drawingViewCount", value);
  }

  /**
   * Returns the configured number of transport order views.
   *
   * @return The configured number of transport order views.
   */
  public int getOrderViewCount() {
    return Math.max(1, configStore.getInt("orderViewCount", 1));
  }

  /**
   * Sets the configured number of transport order views.
   *
   * @param value The number of transport order views.
   */
  public void setOrderViewCount(int value) {
    configStore.setInt("orderViewCount", value);
  }

  /**
   * Returns the configured number of order sequence views.
   *
   * @return The configured number of order sequence views.
   */
  public int getOrderSequenceViewCount() {
    return Math.max(1, configStore.getInt("orderSequenceViewCount", 1));
  }

  /**
   * Sets the configured number of order sequence views.
   *
   * @param value The number of order sequence views.
   */
  public void setOrderSequenceViewCount(int value) {
    configStore.setInt("orderSequenceViewCount", value);
  }
}
