/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action;

import org.opentcs.guing.application.action.course.VehicleAction;
import org.opentcs.guing.application.action.view.LocationThemeAction;
import org.opentcs.guing.application.action.view.VehicleThemeAction;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.util.gui.plugins.LocationTheme;
import org.opentcs.util.gui.plugins.VehicleTheme;

/**
 * A factory for various actions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ActionFactory {

  VehicleAction createVehicleAction(String actionId, VehicleModel model);

  /**
   * Creates an action for setting the given location theme.
   *
   * @param theme The location theme to be set when the action is performed.
   * @return An action for setting the given location theme.
   */
  LocationThemeAction createLocationThemeAction(LocationTheme theme);

  /**
   * Creates an action for setting the given vehicle theme.
   *
   * @param theme The vehicle theme to be set when the action is performed.
   * @return An action for setting the given vehicle theme.
   */
  VehicleThemeAction createVehicleThemeAction(VehicleTheme theme);
}
