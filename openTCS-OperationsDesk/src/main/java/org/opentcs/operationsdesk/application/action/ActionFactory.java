/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.action;

import java.util.Collection;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.operationsdesk.application.action.course.FollowVehicleAction;
import org.opentcs.operationsdesk.application.action.course.IntegrationLevelChangeAction;
import org.opentcs.operationsdesk.application.action.course.PauseAction;
import org.opentcs.operationsdesk.application.action.course.ScrollToVehicleAction;
import org.opentcs.operationsdesk.application.action.course.SendVehicleToLocationAction;
import org.opentcs.operationsdesk.application.action.course.SendVehicleToPointAction;
import org.opentcs.operationsdesk.application.action.course.WithdrawAction;

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

  PauseAction createPauseAction(Collection<VehicleModel> vehicles, boolean pause);
}
