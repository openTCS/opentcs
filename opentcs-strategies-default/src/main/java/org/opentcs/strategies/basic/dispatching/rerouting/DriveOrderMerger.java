// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.rerouting;

import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * Provides a method to merge two {@link DriveOrder}s.
 */
public interface DriveOrderMerger {

  /**
   * Merges the two given {@link DriveOrder}s.
   *
   * @param orderA A drive order.
   * @param orderB A drive order to be merged with {@code orderA}.
   * @param originalOrder The transport order to merge the drive orders for.
   * @param currentRouteStepIndex The index of the last route step travelled for {@code orderA}.
   * @param vehicle The {@link Vehicle} to merge the drive orders for.
   * @return The (new) merged drive order.
   */
  DriveOrder mergeDriveOrders(
      DriveOrder orderA,
      DriveOrder orderB,
      TransportOrder originalOrder,
      int currentRouteStepIndex,
      Vehicle vehicle
  );
}
