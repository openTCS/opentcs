/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralJob.State;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A status message containing information about a peripheral job.
 */
public class PeripheralJobStatusMessage
    extends StatusMessage {

  private String name;

  private String reservationToken;

  private String relatedVehicle;

  private String relatedTransportOrder;

  private PeripheralOperationDescription peripheralOperation;

  private State state;

  private Instant creationTime;

  private Instant finishedTime;

  private List<Property> properties;

  /**
   * Creates a new instance.
   */
  public PeripheralJobStatusMessage() {
  }

  @Override
  public PeripheralJobStatusMessage setSequenceNumber(long sequenceNumber) {
    return (PeripheralJobStatusMessage) super.setSequenceNumber(sequenceNumber);
  }

  @Override
  public PeripheralJobStatusMessage setCreationTimeStamp(Instant creationTimeStamp) {
    return (PeripheralJobStatusMessage) super.setCreationTimeStamp(creationTimeStamp);
  }

  public String getName() {
    return name;
  }

  public PeripheralJobStatusMessage setName(String name) {
    this.name = name;
    return this;
  }

  public String getReservationToken() {
    return reservationToken;
  }

  public PeripheralJobStatusMessage setReservationToken(String reservationToken) {
    this.reservationToken = reservationToken;
    return this;
  }

  public String getRelatedVehicle() {
    return relatedVehicle;
  }

  public PeripheralJobStatusMessage setRelatedVehicle(String relatedVehicle) {
    this.relatedVehicle = relatedVehicle;
    return this;
  }

  public String getRelatedTransportOrder() {
    return relatedTransportOrder;
  }

  public PeripheralJobStatusMessage setRelatedTransportOrder(String relatedTransportOrder) {
    this.relatedTransportOrder = relatedTransportOrder;
    return this;
  }

  public PeripheralOperationDescription getPeripheralOperation() {
    return peripheralOperation;
  }

  public PeripheralJobStatusMessage setPeripheralOperation(
      PeripheralOperationDescription peripheralOperation) {
    this.peripheralOperation = peripheralOperation;
    return this;
  }

  public State getState() {
    return state;
  }

  public PeripheralJobStatusMessage setState(State state) {
    this.state = state;
    return this;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public PeripheralJobStatusMessage setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  public Instant getFinishedTime() {
    return finishedTime;
  }

  public PeripheralJobStatusMessage setFinishedTime(Instant finishedTime) {
    this.finishedTime = finishedTime;
    return this;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public PeripheralJobStatusMessage setProperties(List<Property> properties) {
    this.properties = properties;
    return this;
  }

  public static PeripheralJobStatusMessage fromPeripheralJob(PeripheralJob job,
                                                             long sequenceNumber) {
    return fromPeripheralJob(job, sequenceNumber, Instant.now());
  }

  public static PeripheralJobStatusMessage fromPeripheralJob(PeripheralJob job,
                                                             long sequenceNumber,
                                                             Instant creationTimestamp) {
    PeripheralJobStatusMessage message = new PeripheralJobStatusMessage();
    message.setSequenceNumber(sequenceNumber);
    message.setCreationTimeStamp(creationTimestamp);

    message.setName(job.getName());
    message.setReservationToken(job.getReservationToken());
    if (job.getRelatedVehicle() != null) {
      message.setRelatedVehicle(job.getRelatedVehicle().getName());
    }
    if (job.getRelatedTransportOrder() != null) {
      message.setRelatedTransportOrder(job.getRelatedTransportOrder().getName());
    }
    message.setPeripheralOperation(
        PeripheralOperationDescription.fromPeripheralOperation(job.getPeripheralOperation())
    );
    message.setState(job.getState());
    message.setCreationTime(job.getCreationTime());
    message.setFinishedTime(job.getFinishedTime());
    message.setProperties(job.getProperties().entrySet().stream()
        .map(entry -> new Property(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList()));

    return message;
  }
}
