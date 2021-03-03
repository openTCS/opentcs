/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to set the default theme in the client.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationThemeAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "undefinedLocationTheme";
  /**
   * A label to indicate the theme is undefined.
   */
  public static final String UNDEFINED
      = ResourceBundleUtil.getBundle().getString("openTCS.undefinedTheme");
  /**
   * The application's main view.
   */
  private final OpenTCSView view;
  /**
   * Manages the location themes.
   */
  private final LocationThemeManager locationThemeManager;
  /**
   * The theme to be set when this action is performed.
   */
  private final LocationTheme theme;

  @Inject
  public LocationThemeAction(OpenTCSView view,
                             LocationThemeManager locationThemeManager,
                             @Assisted @Nullable LocationTheme theme) {
    this.view = requireNonNull(view, "view");
    this.locationThemeManager = requireNonNull(locationThemeManager,
                                               "locationThemeManager");
    this.theme = theme;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    locationThemeManager.updateDefaultTheme(theme);
    view.updateLocationThemes();
  }
}
