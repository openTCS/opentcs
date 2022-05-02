/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides high-level methods for the kernel to control a vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface VehicleController
    extends Lifecycle,
            Scheduler.Client {

  /**
   * Sets/Updates the current transport order for the vehicle associated with this controller.
   * <p>
   * The controller is expected to process the transport order's current drive order.
   * Once processing of this drive order is finished, it sets the vehicle's processing state to
   * {@link Vehicle.ProcState#AWAITING_ORDER} to signal this.
   * This method will then be called for either the next drive order in the same transport order or
   * a new transport order.
   * </p>
   * <p>
   * This method may also be called again for the same/current drive order in case <em>any</em>
   * future part of the route to be taken for the transport order has changed.
   * In case of such an update, the continuity of the transport order's route is guaranteed, which
   * means that the previously given route and the one given in {@code newOrder} match up to the
   * last point already sent to the vehicle associated with this controller.
   * Beyond that point the routes may diverge.
   * </p>
   *
   * @param newOrder The new or updated transport order.
   * @throws IllegalArgumentException If {@code newOrder} cannot be processed for some reason, e.g.
   * because it has already been partly processed and the route's continuity is not given.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void setTransportOrder(@Nonnull TransportOrder newOrder)
      throws IllegalArgumentException {
  }

  /**
   * Sets the current drive order for the vehicle associated with this
   * controller.
   *
   * @param newOrder The new drive order.
   * @param orderProperties Properties of the transport order the new drive
   * order is part of.
   * @throws IllegalStateException If this controller already has a drive order.
   * @deprecated Use {@link #setTransportOrder(org.opentcs.data.order.TransportOrder)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void setDriveOrder(@Nonnull DriveOrder newOrder,
                             @Nonnull Map<String, String> orderProperties)
      throws IllegalStateException {
  }

  /**
   * Updates the current drive order for the vehicle associated with this controller.
   * <p>
   * An update is only allowed, if the continuity of the current drive order is guaranteed.
   * The continuity of the current drive order is guaranteed, if the routes of both the current
   * drive order and the {@code newOrder} match to the point where the vehicle associated with this
   * controller is currently reported at. Beyond that point the routes may diverge.
   * </p>
   *
   * @param newOrder The new drive order.
   * @param orderProperties Properties of the transport order the new drive order is part of.
   * @throws IllegalStateException If the {@code newOrder} would not guarantee the current drive
   * order's continuity.
   * @deprecated Use {@link #setTransportOrder(org.opentcs.data.order.TransportOrder)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void updateDriveOrder(@Nonnull DriveOrder newOrder,
                                @Nonnull Map<String, String> orderProperties)
      throws IllegalStateException {
  }

  /**
   * Notifies the controller that the current transport order is to be aborted.
   * After receiving this notification, the controller should not send any further movement commands
   * to the vehicle.
   *
   * @param immediate If <code>true</code>, immediately reset the current transport order for the
   * vehicle associated with this controller, clears the vehicle's command queue implicitly and
   * frees all resources reserved for the removed commands/movements.
   * (Note that this is unsafe, as the vehicle might be moving and clearing the command queue might
   * overlap with the vehicle's movement/progress.)
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void abortTransportOrder(boolean immediate) {
    if (immediate) {
      clearDriveOrder();
    }
    else {
      abortDriveOrder();
    }
  }

  /**
   * Resets the current drive order for the vehicle associated with this controller.
   * At the end of this method, {@link #clearCommandQueue()} is called implicitly.
   *
   * @deprecated Use {@link #abortTransportOrder(boolean)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void clearDriveOrder() {
  }

  /**
   * Notifies the controller that the current drive order is to be aborted.
   * After receiving this notification, the controller should not send any
   * further movement commands to the vehicle.
   *
   * @deprecated Use {@link #abortTransportOrder(boolean)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void abortDriveOrder() {
  }

  /**
   * Clears the associated vehicle's command queue and frees all resources reserved for the removed
   * commands/movements.
   *
   * @deprecated Use {@link #abortTransportOrder(boolean)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void clearCommandQueue() {
  }

  /**
   * Checks if the vehicle would be able to process the given transport order, taking into account
   * its current state.
   *
   * @param order The transport order to be checked.
   * @return An <code>ExplainedBoolean</code> indicating whether the vehicle would be able to
   * process given order.
   */
  @Nonnull
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default ExplainedBoolean canProcess(@Nonnull TransportOrder order) {
    return canProcess(
        order.getFutureDriveOrders().stream()
            .map(driveOrder -> driveOrder.getDestination().getOperation())
            .collect(Collectors.toList())
    );
  }

  /**
   * Checks if the vehicle would be able to process the given sequence of operations, taking into
   * account its current state.
   *
   * @param operations A sequence of operations that might appear in future commands.
   * @return An <code>ExplainedBoolean</code> indicating whether the vehicle would be able to
   * process every single operation in the list (in the given order).
   * @deprecated Use {@link #canProcess(org.opentcs.data.order.TransportOrder)} instead.
   */
  @Nonnull
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default ExplainedBoolean canProcess(@Nonnull List<String> operations) {
    return new ExplainedBoolean(false, "VehicleController default implementation");
  }

  /**
   * Notifies the implementation that the vehicle's <em>paused</em> state in the kernel has changed.
   * If pausing between points in the plant model is supported by the vehicle, the communication
   * adapter may want to inform the vehicle about this change of state.
   *
   * @param paused The vehicle's new paused state.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void onVehiclePaused(boolean paused) {
  }

  /**
   * Delivers a generic message to the communication adapter.
   *
   * @param message The message to be delivered.
   */
  void sendCommAdapterMessage(@Nullable Object message);

  /**
   * Sends a {@link AdapterCommand} to the communication adapter.
   *
   * @param command The adapter command to be sent.
   */
  void sendCommAdapterCommand(@Nonnull AdapterCommand command);

  /**
   * Returns a list of {@link MovementCommand}s that have been sent to the communication adapter.
   *
   * @return A list of {@link MovementCommand}s that have been sent to the communication adapter.
   */
  @Nonnull
  Queue<MovementCommand> getCommandsSent();

  /**
   * Returns the command for which the execution of peripheral operations must be completed before
   * it can be sent to the communication adapter.
   * For this command, allocated resources have already been accepted.
   *
   * @return The command for which the execution of peripheral operations is pending or
   * {@link Optional#empty()} if there's no such command.
   */
  @Nonnull
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default Optional<MovementCommand> getInteractionsPendingCommand() {
    return Optional.empty();
  }
}
