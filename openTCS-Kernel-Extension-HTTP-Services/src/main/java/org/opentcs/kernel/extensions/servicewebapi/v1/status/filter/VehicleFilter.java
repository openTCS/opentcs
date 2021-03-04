/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.filter;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class VehicleFilter
    implements Predicate<Vehicle> {

  /**
   * The processing state of the requested vehicles.
   */
  @Nullable
  private final String procState;

  public VehicleFilter(String procState) {
    this.procState = procState;
  }

  @Override
  public boolean test(Vehicle vehicle) {
    boolean accept = true;
    if (procState != null && !procState.equals(vehicle.getProcState().name())) {
      accept = false;
    }
    return accept;
  }

}
