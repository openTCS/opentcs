// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Queue;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This interface declares the methods that a driver communicating with and
 * controlling a physical vehicle must implement.
 * <p>
 * A communication adapter is basically a driver that converts high-level
 * commands sent by openTCS to a form that the controlled vehicles understand.
 * </p>
 */
public interface VehicleCommAdapter
    extends
      Lifecycle {

  /**
   * Enables this comm adapter, i.e. turns it on.
   */
  void enable();

  /**
   * Disables this comm adapter, i.e. turns it off.
   */
  void disable();

  /**
   * Checks whether this communication adapter is enabled.
   *
   * @return <code>true</code> if, and only if, this communication adapter is
   * enabled.
   */
  boolean isEnabled();

  /**
   * Returns an observable model of the vehicle's and its comm adapter's attributes.
   *
   * @return An observable model of the vehicle's and its comm adapter's attributes.
   */
  @Nonnull
  VehicleProcessModel getProcessModel();

  /**
   * Returns a transferable/serializable model of the vehicle's and its comm adapter's attributes.
   *
   * @return A transferable/serializable model of the vehicle's and its comm adapter's attributes.
   */
  @Nonnull
  VehicleProcessModelTO createTransferableProcessModel();

  /**
   * Returns this adapter's queue of unsent commands.
   * <p>
   * Unsent {@link MovementCommand}s are commands that the comm adapter received from the
   * {@link VehicleController} it's associated with. When a command is sent to the vehicle, the
   * command is removed from this queue and added to the {@link #getSentCommands() queue of sent
   * commands}.
   * </p>
   *
   * @return This adapter's queue of unsent commands.
   * @see #getCommandsCapacity()
   */
  Queue<MovementCommand> getUnsentCommands();

  /**
   * Returns this adapter's queue of sent commands.
   * <p>
   * Sent {@link MovementCommand}s are commands that the comm adapter has sent to the vehicle
   * already but which have not yet been processed by it.
   * </p>
   *
   * @return This adapter's queue of sent commands.
   * @see #getCommandsCapacity()
   */
  Queue<MovementCommand> getSentCommands();

  /**
   * Indicates how many commands this comm adapter accepts.
   * <p>
   * This capacity considers both the {@link #getUnsentCommands() queue of unsent commands} and the
   * {@link #getSentCommands() queue of sent commands}. This means that:
   * </p>
   * <ul>
   * <li>The number of elements in both queues combined must not exceed this number.</li>
   * <li>The vehicle will have at most this number of (not yet completed) commands at any given
   * point of time.</li>
   * </ul>
   *
   * @return The number of commands this comm adapter accepts.
   */
  int getCommandsCapacity();

  /**
   * Checks whether this comm adapter can accept the next (i.e. one more)
   * {@link MovementCommand command}.
   *
   * @return {@code true}, if this adapter can accept another command, otherwise {@code false}.
   */
  boolean canAcceptNextCommand();

  /**
   * Returns the string the comm adapter recognizes as a recharge operation.
   *
   * @return The string the comm adapter recognizes as a recharge operation.
   */
  String getRechargeOperation();

  /**
   * Appends a command to this communication adapter's queue of
   * {@link #getUnsentCommands() unsent commands}.
   * <p>
   * The return value of this method indicates whether the command was really added to the queue.
   * The primary reason for a commmand not being added to the queue is that it would exceed the
   * adapter's {@link #getCommandsCapacity() commands capacity}.
   * </p>
   *
   * @param newCommand The command to be added to this adapter's queue of
   * {@link #getUnsentCommands() unsent commands}.
   * @return <code>true</code> if, and only if, the new command was added to the queue.
   */
  boolean enqueueCommand(
      @Nonnull
      MovementCommand newCommand
  );

  /**
   * Clears this communication adapter's command queues (i.e. the queues of
   * {@link #getUnsentCommands() unsent} and {@link #getSentCommands() sent} commands).
   * <p>
   * All commands in the queue that have not been sent to this adapter's vehicle, yet, will be
   * removed. Whether commands the vehicle has already received are still executed is up to the
   * implementation and/or the vehicle.
   * </p>
   */
  void clearCommandQueue();

  /**
   * Checks if the vehicle would be able to process the given transport order, taking into account
   * its current state.
   *
   * @param order The transport order to be checked.
   * @return An <code>ExplainedBoolean</code> indicating whether the vehicle would be able to
   * process the given order.
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
   * Processes a generic message to the communication adapter.
   * This method provides a generic one-way communication channel to the comm adapter. The message
   * can be anything, including <code>null</code>, and since
   * {@link VehicleService#sendCommAdapterMessage(org.opentcs.data.TCSObjectReference, Object)}
   * provides a way to send a message from outside the kernel, it can basically originate from any
   * source. The message thus does not necessarily have to be meaningful to the concrete comm
   * adapter implementation at all.
   * <p>
   * <em>
   * Implementation notes:
   * Meaningless messages should simply be ignored and not result in exceptions being thrown.
   * If a comm adapter implementation does not support processing messages, it should simply provide
   * an empty implementation.
   * A call to this method should return quickly, i.e. this method should not execute long
   * computations directly but start them in a separate thread.
   * </em></p>
   *
   * @param message The message to be processed.
   * @deprecated Use {@link #processMessage(VehicleCommAdapterMessage)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void processMessage(
      @Nullable
      Object message
  );

  /**
   * Executes the given {@link AdapterCommand}.
   *
   * @param command The command to execute.
   * @deprecated Use {@link #processMessage(VehicleCommAdapterMessage)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  void execute(
      @Nonnull
      AdapterCommand command
  );

  /**
   * Processes the given {@link VehicleCommAdapterMessage}
   * <p>
   * This method provides a generic one-way communication channel to the comm adapter. Since
   * {@link VehicleService#sendCommAdapterMessage(org.opentcs.data.TCSObjectReference,
   * VehicleCommAdapterMessage)} provides a way to send a message from outside the kernel, it can
   * basically originate from any source. The message thus does not necessarily have to be
   * meaningful to the concrete comm adapter implementation at all.
   * </p>
   * <p>
   * <em>
   * Implementation notes:
   * Meaningless messages should simply be ignored and not result in exceptions being thrown.
   * If a comm adapter implementation does not support processing messages, it should simply provide
   * an empty implementation.
   * A call to this method should return quickly, i.e. this method should not execute long
   * computations directly but start them in a separate thread.
   * </em>
   * </p>
   *
   * @param message The message to process.
   */
  @ScheduledApiChange(when = "7.0", details = "Default implementation will be removed.")
  default void processMessage(
      @Nonnull
      VehicleCommAdapterMessage message
  ) {
  }
}
