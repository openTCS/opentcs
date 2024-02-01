/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;

/**
 * Provides a method to merge two {@link DriveOrder}s.
 */
public interface DriveOrderMerger {

  /**
   * Merges the two given {@link DriveOrder}s.
   *
   * @param orderA A drive order.
   * @param orderB A drive order to be merged with {@code orderA}.
   * @param currentRouteStepIndex The index of the last route step travelled for {@code orderA}.
   * @param vehicle The {@link Vehicle} to merge the drive orders for.
   * @return The (new) merged drive order.
   */
  DriveOrder mergeDriveOrders(DriveOrder orderA,
                              DriveOrder orderB,
                              int currentRouteStepIndex,
                              Vehicle vehicle);
}
