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
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.VehicleThemeManager;

/**
 * An action to set the default theme in the client.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleThemeAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "undefinedVehicleTheme";
  /**
   * A label indicating no theme has been defined.
   */
  public static final String UNDEFINED
      = ResourceBundleUtil.getBundle().getString("openTCS.undefinedVehTheme");
  /**
   * The application's main view.
   */
  private final OpenTCSView view;
  /**
   * Manages the vehicle themes.
   */
  private final VehicleThemeManager vehicleThemeManager;
  /**
   * The theme to be set when this action is performed.
   */
  private final VehicleTheme theme;

  /**
   * Creates a new instance.
   *
   * @param view The application's main view.
   * @param vehicleThemeManager Manages the vehicle themes.
   * @param theme The theme to be set when this action is performed.
   */
  @Inject
  public VehicleThemeAction(OpenTCSView view,
                            VehicleThemeManager vehicleThemeManager,
                            @Assisted @Nullable VehicleTheme theme) {
    this.view = requireNonNull(view, "view");
    this.vehicleThemeManager = requireNonNull(vehicleThemeManager,
                                              "vehicleThemeManager");
    this.theme = theme;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    vehicleThemeManager.updateDefaultTheme(theme);
    view.updateVehicleThemes();
  }
}
