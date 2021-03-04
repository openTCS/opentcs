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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.Kernel;
import org.opentcs.components.Lifecycle;
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
  @ScheduledApiChange(details = "Default implementation will be removed", when = "5.0")
  default VehicleProcessModelTO createTransferableProcessModel() {
    return new VehicleProcessModelTO();
  }

  /**
   * Indicates how many commands this comm adapter's command queue accepts.
   *
   * @return The number of commands this comm adapter's command queue accepts.
   */
  int getCommandQueueCapacity();

  /**
   * Returns this adapter's command queue.
   *
   * @return This adapter's command queue.
   */
  @Nonnull
  Queue<MovementCommand> getCommandQueue();

  /**
   * Returns the capacity of this adapter's <em>sent queue</em>.
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
   * Returns a list of panels that this communication adapter offers for displaying or manipulating
   * its custom properties.
   *
   * @return A list of panels.
   * @deprecated {@code VehicleCommAdapterPanel} is deprecated.
   */
  @Nonnull
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  List<VehicleCommAdapterPanel> getAdapterPanels();

  /**
   * Checks if the vehicle would be able to process the given sequence of operations, taking into
   * account its current state.
   *
   * @param operations A sequence of operations that might have to be processed as part of a
   * transport order.
   * @return A <code>Processability</code> telling if the vehicle would be able to process every
   * single operation in the list (in the given order).
   */
  @Nonnull
  ExplainedBoolean canProcess(@Nonnull List<String> operations);

  /**
   * Processes a generic message to the communication adapter.
   * This method provides a generic one-way communication channel to the comm adapter. The message
   * can be anything, including <code>null</code>, and since
   * {@link Kernel#sendCommAdapterMessage(org.opentcs.data.TCSObjectReference, java.lang.Object)}
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
  @ScheduledApiChange(details = "Default implementation will be removed.", when = "5.0")
  default void execute(@Nonnull AdapterCommand command) {
    command.execute(this);
  }

  /**
   * Defines the possible states of a communication adapter.
   *
   * @deprecated Does not serve any useful purpose.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public enum State {

    /**
     * Indicates the state of this communication adapter is currently not known.
     */
    UNKNOWN,
    /**
     * Indicates this communication adapter is not currently connected to the
     * vehicle it controls.
     */
    CONNECTING,
    /**
     * Indicates this communication adapter is connected to the vehicle it
     * controls.
     */
    CONNECTED
  }
}
