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
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Filters a set of peripheral jobs for job related to a vehicle and/or a transport order.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class PeripheralJobFilter
    implements Predicate<PeripheralJob> {

  /**
   * The vehicle a peripheral job must be related to.
   */
  @Nullable
  private final String relatedVehicle;

  /**
   * The transport order a peripheral job must be related to.
   */
  @Nullable
  private final String relatedTransportOrder;

  public PeripheralJobFilter(String relatedVehicle, String relatedTransportOrder) {
    this.relatedVehicle = relatedVehicle;
    this.relatedTransportOrder = relatedTransportOrder;
  }

  @Override
  public boolean test(PeripheralJob job) {
    if (relatedVehicle != null && !isVehicleRelatedToJob(relatedVehicle, job)) {
      return false;
    }
    if (relatedTransportOrder != null
        && !isTransportOrderRelatedToJob(relatedTransportOrder, job)) {
      return false;
    }
    return true;
  }

  private boolean isVehicleRelatedToJob(String vehicle, PeripheralJob job) {
    return job.getRelatedVehicle().getName().equals(vehicle);
  }

  private boolean isTransportOrderRelatedToJob(String transportOrder, PeripheralJob job) {
    return job.getRelatedTransportOrder().getName().equals(transportOrder);
  }

}
