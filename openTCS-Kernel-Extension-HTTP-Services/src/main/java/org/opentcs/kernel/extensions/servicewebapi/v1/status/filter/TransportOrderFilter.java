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
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class TransportOrderFilter
    implements Predicate<TransportOrder> {

  /**
   * The name of the intended vehicle of the transport order.
   */
  @Nullable
  private final String intendedVehicle;

  public TransportOrderFilter(String intendedVehicle) {
    this.intendedVehicle = intendedVehicle;
  }

  @Override
  public boolean test(TransportOrder transportOrder) {
    boolean accept = true;

    if (intendedVehicle != null
        && intendedVehicleDiffers(transportOrder.getIntendedVehicle())) {
      accept = false;
    }
    return accept;
  }

  private boolean intendedVehicleDiffers(@Nullable TCSObjectReference<Vehicle> vehicleReference) {
    return vehicleReference == null || !intendedVehicle.equals(vehicleReference.getName());
  }
}
