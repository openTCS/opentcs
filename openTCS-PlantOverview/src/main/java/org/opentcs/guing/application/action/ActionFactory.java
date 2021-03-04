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
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A factory for various actions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ActionFactory {

  VehicleAction createVehicleAction(String actionId, VehicleModel model);
}
