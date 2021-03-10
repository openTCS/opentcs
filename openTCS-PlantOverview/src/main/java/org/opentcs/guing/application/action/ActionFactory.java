/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action;

import java.util.Collection;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.action.course.FollowVehicleAction;
import org.opentcs.guing.application.action.course.IntegrationLevelChangeAction;
import org.opentcs.guing.application.action.course.ScrollToVehicleAction;
import org.opentcs.guing.application.action.course.SendVehicleToLocationAction;
import org.opentcs.guing.application.action.course.SendVehicleToPointAction;
import org.opentcs.guing.application.action.course.WithdrawAction;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A factory for various actions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ActionFactory {

  ScrollToVehicleAction createScrollToVehicleAction(VehicleModel vehicleModel);

  FollowVehicleAction createFollowVehicleAction(VehicleModel vehicleModel);

  SendVehicleToPointAction createSendVehicleToPointAction(VehicleModel vehicleModel);

  SendVehicleToLocationAction createSendVehicleToLocationAction(VehicleModel vehicleModel);

  WithdrawAction createWithdrawAction(Collection<VehicleModel> vehicles, boolean immediateAbort);

  IntegrationLevelChangeAction createIntegrationLevelChangeAction(Collection<VehicleModel> vehicles,
                                                                  Vehicle.IntegrationLevel level);
}
