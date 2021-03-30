/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * Contains details about a peripheral device a location may represent.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralInformation
    implements Serializable {

  /**
   * A token for which a location/peripheral device is currently reserved.
   */
  @Nullable
  private final String reservationToken;
  /**
   * This peripheral device's current state.
   */
  @Nonnull
  private final State state;
  /**
   * This peripheral device's current processing state.
   */
  @Nonnull
  private final ProcState procState;
  /**
   * A reference to the peripheral job this peripheral device is currently processing.
   */
  @Nullable
  private final TCSObjectReference<PeripheralJob> peripheralJob;

  public PeripheralInformation() {
    this(null, State.NO_PERIPHERAL, ProcState.IDLE, null);
  }

  private PeripheralInformation(
      @Nullable String reservationToken,
      @Nonnull State state,
      @Nonnull ProcState procState,
      @Nullable TCSObjectReference<PeripheralJob> peripheralJob) {
    this.reservationToken = reservationToken;
    this.state = Objects.requireNonNull(state, "state");
    this.procState = Objects.requireNonNull(procState, "procState");
    this.peripheralJob = peripheralJob;
  }

  /**
   * Returns a token for which a location/peripheral device is currently reserved.
   *
   * @return A token for which a location/peripheral device is currently reserved.
   */
  @Nullable
  public String getReservationToken() {
    return reservationToken;
  }

  /**
   * Creates a copy of this object, with the given reservation token.
   *
   * @param reservationToken The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralInformation withReservationToken(@Nullable String reservationToken) {
    return new PeripheralInformation(reservationToken, state, procState, peripheralJob);
  }

  /**
   * Returns the peripheral device's current state.
   *
   * @return The peripheral device's current state.
   */
  @Nonnull
  public State getState() {
    return state;
  }

  /**
   * Creates a copy of this object, with the given state.
   *
   * @param state The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralInformation withState(@Nonnull State state) {
    return new PeripheralInformation(reservationToken, state, procState, peripheralJob);
  }

  /**
   * Returns the peripheral device's current processing state.
   *
   * @return The peripheral device's current processing state.
   */
  @Nonnull
  public ProcState getProcState() {
    return procState;
  }

  /**
   * Creates a copy of this object, with the given processing state.
   *
   * @param procState The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralInformation withProcState(@Nonnull ProcState procState) {
    return new PeripheralInformation(reservationToken, state, procState, peripheralJob);
  }

  /**
   * Returns a reference to the peripheral job this peripheral device is currently processing.
   *
   * @return A reference to the peripheral job this peripheral device is currently processing,
   * or {@code null}, it is not processing any peripheral job at the moment.
   */
  @Nullable
  public TCSObjectReference<PeripheralJob> getPeripheralJob() {
    return peripheralJob;
  }

  /**
   * Creates a copy of this object, with the given peripheral job.
   *
   * @param peripheralJob The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralInformation withPeripheralJob(
      @Nullable TCSObjectReference<PeripheralJob> peripheralJob) {
    return new PeripheralInformation(reservationToken, state, procState, peripheralJob);
  }

  /**
   * The elements of this enumeration describe the various possible states of a peripheral device.
   */
  public enum State {
    /**
     * Indicates that the location the {@link PeripheralInformation} belongs to doesn't represent
     * a peripheral device.
     */
    NO_PERIPHERAL,
    /**
     * The peripheral device's current state is unknown, e.g. because communication with
     * it is currently not possible for some reason.
     */
    UNKNOWN,
    /**
     * The peripheral device's state is known and it's not in an error state, but it is
     * not available for receiving jobs.
     */
    UNAVAILABLE,
    /**
     * There is a problem with the peripheral device.
     */
    ERROR,
    /**
     * The peripheral device is currently idle/available for processing jobs.
     */
    IDLE,
    /**
     * The peripheral device is processing a job.
     */
    EXECUTING
  }

  /**
   * A peripheral device's processing state as seen by the peripheral job dispatcher.
   */
  public enum ProcState {
    /**
     * The peripheral device is currently not processing a job.
     */
    IDLE,
    /**
     * The peripheral device is currently processing a job.
     */
    PROCESSING_JOB
  }
}
