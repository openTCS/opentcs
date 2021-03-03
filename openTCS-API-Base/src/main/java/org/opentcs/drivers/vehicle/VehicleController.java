/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.util.ExplainedBoolean;

/**
 * Provides high-level methods for the system to control a vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleController
    extends Lifecycle,
            Scheduler.Client {

  /**
   * Sets the current drive order for the vehicle associated with this
   * controller.
   *
   * @param newOrder The new drive order.
   * @param orderProperties Properties of the transport order the new drive
   * order is part of.
   * @throws IllegalStateException If this controller already has a drive order.
   */
  void setDriveOrder(@Nullable DriveOrder newOrder, @Nonnull Map<String, String> orderProperties)
      throws IllegalStateException;

  /**
   * Resets the current drive order for the vehicle associated with this controller.
   */
  void clearDriveOrder();

  /**
   * Notifies the controller that the current drive order is to be aborted.
   * After receiving this notification, the controller should not send any
   * further movement commands to the vehicle.
   */
  void abortDriveOrder();

  /**
   * Clears the associated vehicle's command queue and frees all resources reserved for the removed
   * commands/movements.
   */
  void clearCommandQueue();
  
  /**
   * Resets the vehicle's position and precise position to <code>null</code> and frees all resources
   * held by the vehicle.
   */
  void resetVehiclePosition();

  /**
   * Checks if the vehicle would be able to process the given sequence of
   * operations, taking into account its current state.
   *
   * @param operations A sequence of operations that might appear in future
   * commands.
   * @return A <code>Processability</code> telling if the vehicle would be able
   * to process every single operation in the list (in the given order).
   */
  @Nonnull
  ExplainedBoolean canProcess(@Nonnull List<String> operations);

  /**
   * Delivers a generic message to the communication adapter.
   *
   * @param message The message to be delivered.
   */
  void sendCommAdapterMessage(@Nullable Object message);
}
