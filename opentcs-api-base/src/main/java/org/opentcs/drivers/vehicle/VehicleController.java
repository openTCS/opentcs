// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.ExplainedBoolean;

/**
 * Provides high-level methods for the kernel to control a vehicle.
 */
public interface VehicleController
    extends
      Lifecycle {

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
   * because it has already been partly processed and the route's continuity is not given, the
   * vehicle's current position is unknown or the resources for the vehicle's current position may
   * not be allocated (in case of forced rerouting).
   */
  void setTransportOrder(
      @Nonnull
      TransportOrder newOrder
  )
      throws IllegalArgumentException;

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
  void abortTransportOrder(boolean immediate);

  /**
   * Checks if the vehicle would be able to process the given transport order, taking into account
   * its current state.
   *
   * @param order The transport order to be checked.
   * @return An <code>ExplainedBoolean</code> indicating whether the vehicle would be able to
   * process given order.
   */
  @Nonnull
  ExplainedBoolean canProcess(
      @Nonnull
      TransportOrder order
  );

  /**
   * Notifies the implementation that the vehicle's <em>paused</em> state in the kernel has changed.
   * If pausing between points in the plant model is supported by the vehicle, the communication
   * adapter may want to inform the vehicle about this change of state.
   *
   * @param paused The vehicle's new paused state.
   */
  void onVehiclePaused(boolean paused);

  /**
   * Sends a {@link VehicleCommAdapterMessage} to the communication adapter.
   *
   * @param message The message to be sent.
   */
  void sendCommAdapterMessage(
      @Nonnull
      VehicleCommAdapterMessage message
  );

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
  Optional<MovementCommand> getInteractionsPendingCommand();

  /**
   * Checks if the given set of resources are safe to be allocated <em>immediately</em> by this
   * controller.
   *
   * @param resources The requested resources.
   * @return {@code true} if the given resources are safe to be allocated by this controller,
   * otherwise {@code false}.
   */
  boolean mayAllocateNow(
      @Nonnull
      Set<TCSResource<?>> resources
  );
}
