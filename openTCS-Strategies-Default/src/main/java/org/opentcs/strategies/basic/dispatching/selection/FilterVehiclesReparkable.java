/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Filters vehicles that are reparkable.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FilterVehiclesReparkable
    implements ReparkVehicleSelectionFilter {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  
  /**
   * Creates a new instance.
   * 
   * @param objectService The object service.
   */
  @Inject
  public FilterVehiclesReparkable(TCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }
  
  @Override
  public boolean test(Vehicle vehicle) {
    return reparkable(vehicle);
  }

  private boolean reparkable(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.IDLE)
        && isParkingPosition(vehicle.getCurrentPosition())
        && vehicle.getOrderSequence() == null;
  }
  
  private boolean isParkingPosition(TCSObjectReference<Point> positionRef) {
    if (positionRef == null) {
      return false;
    }

    Point position = objectService.fetchObject(Point.class, positionRef);
    return position.isParkingPosition();
  }
}
