/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.peripherals;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.data.peripherals.PeripheralJobHistoryCodes.JOB_CREATED;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * Represents a job that is to be processed by a peripheral device.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralJob
    extends TCSObject<PeripheralJob>
    implements Serializable {

  /**
   * A token that may be used to reserve a peripheral device.
   * A peripheral device that is reserved for a specific token can only process jobs which match
   * that reservation token.
   * This string may not be empty.
   */
  @Nonnull
  private final String reservationToken;
  /**
   * The vehicle for which this peripheral job was created.
   * May be {@code null}, if this job wasn't created in the context of a transport order being
   * processed by a vehicle.
   */
  @Nullable
  private final TCSObjectReference<Vehicle> relatedVehicle;
  /**
   * The transport order for which this peripheral job was created.
   * May be {@code null}, if this job wasn't created in the context of a transport order being
   * processed by a vehicle.
   */
  @Nullable
  private final TCSObjectReference<TransportOrder> relatedTransportOrder;
  /**
   * The operation that is to be performed by the pripheral device.
   */
  @Nonnull
  private final PeripheralOperation peripheralOperation;
  /**
   * This peripheral job's current state.
   */
  @Nonnull
  private final State state;
  /**
   * The point of time at which this peripheral job was created.
   */
  @Nonnull
  private final Instant creationTime;
  /**
   * The point of time at which processing of this peripheral job was finished.
   */
  @Nonnull
  private final Instant finishedTime;

  /**
   * Creates a new instance.
   *
   * @param name The peripheral job's name.
   * @param reservationToken The reservation token to be used.
   * @param peripheralOperation The operation to be performed.
   */
  public PeripheralJob(@Nonnull String name,
                       @Nonnull String reservationToken,
                       @Nonnull PeripheralOperation peripheralOperation) {
    this(name,
         new HashMap<>(),
         new ObjectHistory().withEntryAppended(new ObjectHistory.Entry(JOB_CREATED)),
         reservationToken,
         null,
         null,
         peripheralOperation,
         State.TO_BE_PROCESSED,
         Instant.now(),
         Instant.MAX);
  }

  private PeripheralJob(String objectName,
                        Map<String, String> properties,
                        ObjectHistory history,
                        String reservationToken,
                        TCSObjectReference<Vehicle> relatedVehicle,
                        TCSObjectReference<TransportOrder> transportOrder,
                        PeripheralOperation peripheralOperation,
                        State state,
                        Instant creationTime,
                        Instant finishedTime) {
    super(objectName, properties, history);
    this.reservationToken = requireNonNull(reservationToken, "reservationToken");
    checkArgument(!reservationToken.isEmpty(), "reservationToken may not be empty.");
    this.relatedVehicle = relatedVehicle;
    this.relatedTransportOrder = transportOrder;
    this.peripheralOperation = requireNonNull(peripheralOperation, "peripheralOperation");
    this.state = requireNonNull(state, "state");
    this.creationTime = requireNonNull(creationTime, "creationTime");
    this.finishedTime = requireNonNull(finishedTime, "finishedTime");
  }

  @Override
  public PeripheralJob withProperty(String key, String value) {
    return new PeripheralJob(getName(),
                             propertiesWith(key, value),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  @Override
  public PeripheralJob withProperties(Map<String, String> properties) {
    return new PeripheralJob(getName(),
                             properties,
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  @Override
  public PeripheralJob withHistoryEntry(ObjectHistory.Entry entry) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory().withEntryAppended(entry),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  @Override
  public PeripheralJob withHistory(ObjectHistory history) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             history,
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  /**
   * Returns the token that may be used to reserve a peripheral device.
   *
   * @return The token that may be used to reserve a peripheral device.
   */
  public String getReservationToken() {
    return reservationToken;
  }

  /**
   * Creates a copy of this object, with the given reservation token.
   *
   * @param reservationToken The reservation token to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJob withReservationToken(String reservationToken) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  /**
   * Returns the vehicle for which this peripheral job was created.
   *
   * @return The vehicle for which this peripheral job was created.
   */
  public TCSObjectReference<Vehicle> getRelatedVehicle() {
    return relatedVehicle;
  }

  /**
   * Creates a copy of this object, with the given related vehicle.
   *
   * @param relatedVehicle The related vehicle to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJob withRelatedVehicle(TCSObjectReference<Vehicle> relatedVehicle) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  /**
   * Returns the transport order for which this peripheral job was created.
   *
   * @return The transport order for which this peripheral job was created.
   */
  public TCSObjectReference<TransportOrder> getRelatedTransportOrder() {
    return relatedTransportOrder;
  }

  /**
   * Creates a copy of this object, with the given related transport order.
   *
   * @param relatedTransportOrder The related transport order to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJob withRelatedTransportOrder(
      TCSObjectReference<TransportOrder> relatedTransportOrder) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  /**
   * Returns the operation that is to be performed by the peripheral device.
   *
   * @return The operation that is to be performed by the peripheral device.
   */
  public PeripheralOperation getPeripheralOperation() {
    return peripheralOperation;
  }

  /**
   * Creates a copy of this object, with the given peripheral operation.
   *
   * @param peripheralOperation The peripheral operation to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJob withPeripheralOperation(PeripheralOperation peripheralOperation) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  /**
   * Returns this peripheral job's current state.
   *
   * @return this peripheral job's current state.
   */
  public State getState() {
    return state;
  }

  /**
   * Creates a copy of this object, with the given state.
   *
   * @param state The state to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJob withState(State state) {
    // XXX Finished time should probably not be set implicitly.
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             state == State.FINISHED ? Instant.now() : finishedTime);
  }

  /**
   * Returns the point of time at which this peripheral job was created.
   *
   * @return The point of time at which this peripheral job was created.
   */
  public Instant getCreationTime() {
    return creationTime;
  }

  /**
   * Creates a copy of this object, with the given creation time.
   *
   * @param creationTime The creation time to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJob withCreationTime(Instant creationTime) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  /**
   * Returns the point of time at which processing of this peripheral job was finished.
   *
   * @return The point of time at which processing of this peripheral job was finished.
   */
  public Instant getFinishedTime() {
    return finishedTime;
  }

  /**
   * Creates a copy of this object, with the given finished time.
   *
   * @param finishedTime The finished time to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJob withFinishedTime(Instant finishedTime) {
    return new PeripheralJob(getName(),
                             getProperties(),
                             getHistory(),
                             reservationToken,
                             relatedVehicle,
                             relatedTransportOrder,
                             peripheralOperation,
                             state,
                             creationTime,
                             finishedTime);
  }

  /**
   * Defines the various states a peripheral job may be in.
   */
  public static enum State {
    TO_BE_PROCESSED,
    BEING_PROCESSED,
    FINISHED,
    FAILED;
  }
}
