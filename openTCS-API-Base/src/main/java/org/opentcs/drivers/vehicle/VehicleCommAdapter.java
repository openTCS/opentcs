/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleCommAdapter
    extends Lifecycle {

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
   * Indicates how many commands this comm adapter accepts.
   * <p>
   * This capacity considers both the {@link #getCommandQueue() command queue} and the
   * {@link #getSentQueue() sent queue}. This means that the number of elements in both queues
   * combined must not exceed this number.
   * </p>
   *
   * @return The number of commands this comm adapter accepts.
   */
  int getCommandQueueCapacity();

  /**
   * Returns this adapter's command queue.
   * <p>
   * This queue contains {@link MovementCommand}s that the comm adapter received from the
   * {@link VehicleController} it's associated with. When a command is sent to the vehicle, the
   * command is removed from this queue and added to the {@link #getSentQueue() sent queue}.
   * </p>
   *
   * @return This adapter's command queue.
   * @see #getCommandQueueCapacity()
   */
  @Nonnull
  Queue<MovementCommand> getCommandQueue();

  /**
   * Checks whether this comm adapter can accept the next (i.e. one more)
   * {@link MovementCommand command}.
   *
   * @return {@code true}, if this adapter can accept another command, otherwise {@code false}.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default boolean canAcceptNextCommand() {
    return (getCommandQueue().size() + getSentQueue().size()) < getCommandQueueCapacity();
  }

  /**
   * Returns the capacity of this adapter's {@link #getSentQueue() <em>sent queue</em>}.
   *
   * @return The capacity of this adapter's <em>sent queue</em>.
   */
  int getSentQueueCapacity();

  /**
   * Returns a queue containing the commands that this adapter has sent to the vehicle already but
   * which have not yet been processed by it.
   *
   * @return A queue containing the commands that this adapter has sent to the vehicle already but
   * which have not yet been processed by it.
   * @see #getSentQueueCapacity()
   * @see #getCommandQueueCapacity()
   */
  @Nonnull
  Queue<MovementCommand> getSentQueue();

  /**
   * Returns the string the comm adapter recognizes as a recharge operation.
   *
   * @return The string the comm adapter recognizes as a recharge operation.
   */
  String getRechargeOperation();

  /**
   * Appends a command to this communication adapter's command queue.
   * The return value of this method indicates whether the command was really
   * added to the queue. The primary reason for a commmand not being added to
   * the queue is that it would exceed its capacity.
   *
   * @param newCommand The command to be added to this adapter's command queue.
   * @return <code>true</code> if, and only if, the new command was added to
   * this adapter's command queue.
   */
  boolean enqueueCommand(@Nonnull MovementCommand newCommand);

  /**
   * Clears this communication adapter's command queue.
   * All commands in the queue that have not been sent to this adapter's
   * vehicle, yet, will be removed from the command queue. Any operation the
   * vehicle might currently be executing will still be completed, though.
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
   * @param operations A sequence of operations that might have to be processed as part of a
   * transport order.
   * @return An <code>ExplainedBoolean</code> indicating whether the vehicle would be able to
   * process every single operation in the list (in the given order).
   * @deprecated Use {@link #canProcess(org.opentcs.data.order.TransportOrder)} instead.
   */
  @Nonnull
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default ExplainedBoolean canProcess(@Nonnull List<String> operations) {
    return new ExplainedBoolean(false, "VehicleCommAdapter default implementation");
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
   * Processes a generic message to the communication adapter.
   * This method provides a generic one-way communication channel to the comm adapter. The message
   * can be anything, including <code>null</code>, and since
   * {@link VehicleService#sendCommAdapterMessage(org.opentcs.data.TCSObjectReference, java.lang.Object)}
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
   */
  void processMessage(@Nullable Object message);

  /**
   * Executes the given {@link AdapterCommand}.
   *
   * @param command The command to execute.
   */
  void execute(@Nonnull AdapterCommand command);
}
