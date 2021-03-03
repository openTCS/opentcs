/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.util.List;
import org.opentcs.access.Kernel;
import org.opentcs.data.model.Vehicle;

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
public interface CommunicationAdapter {

  /**
   * Appends a command to this communication adapter's command queue.
   * The return value of this method indicates whether the command was really
   * added to the queue; the primary reason for a commmand not being added to
   * the queue is that it would exceed its capacity.
   *
   * @param newCommand The command to be added to this adapter's command queue.
   * @return <code>true</code> if, and only if, the new command was added to
   * this adapter's command queue.
   */
  boolean addCommand(AdapterCommand newCommand);

  /**
   * Clears this communication adapter's command queue.
   * All commands in the queue that have not been sent to this adapter's
   * vehicle, yet, will be removed from the command queue. Any operation the
   * vehicle might currently be executing will still be completed, though.
   */
  void clearCommandQueue();

  /**
   * Returns the vehicle's current energy level (in percent of the maximum).
   *
   * @return The vehicle's current energy level (in percent of the maximum).
   */
  int getVehicleEnergyLevel();

  /**
   * Returns the current (state of the) load handling devices of the vehicle.
   *
   * @return The current (state of the) load handling devices of the vehicle.
   */
  List<LoadHandlingDevice> getVehicleLoadHandlingDevices();

  /**
   * Returns the state of vehicle controlled by this communication adapter.
   *
   * @return The state of vehicle controlled by this communication adapter.
   */
  Vehicle.State getVehicleState();

  /**
   * Checks if the vehicle would be able to process the given sequence of
   * operations, taking into account its current state.
   *
   * @param operations A sequence of operations that might appear in future
   * commands.
   * @return A <code>Processability</code> telling if the vehicle would be able
   * to process every single operation in the list (in the given order).
   */
  Processability canProcess(List<String> operations);
  
  /**
   * Processes a generic message to the communication adapter.
   * This method provides a generic one-way communication channel to the
   * communication adapter. The message can be anything, including
   * <code>null</code>, and since
   * {@link Kernel#sendCommAdapterMessage(org.opentcs.data.TCSObjectReference, java.lang.Object)}
   * provides a way to send a message from outside the kernel, it can basically
   * originate from any source. The message thus does not necessarily have to be
   * meaningful to the communication adapter at all; meaningless messages should
   * simply be ignored and not result in exceptions being thrown.
   * <p>
   * A call to this method should return quickly, i.e. this method should not
   * execute long computations directly but start them in a separate thread.
   * </p>
   *
   * @param message The message to be processed.
   */
  void processMessage(Object message);

  /**
   * Defines the various possible states of a communication adapter.
   */
  public static enum State {

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
