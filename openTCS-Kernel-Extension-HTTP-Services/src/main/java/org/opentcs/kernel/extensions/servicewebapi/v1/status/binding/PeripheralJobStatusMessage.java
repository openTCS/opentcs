/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralJob.State;

/**
 * A status message containing information about a peripheral job.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReservationToken() {
    return reservationToken;
  }

  public void setReservationToken(String reservationToken) {
    this.reservationToken = reservationToken;
  }

  public String getRelatedVehicle() {
    return relatedVehicle;
  }

  public void setRelatedVehicle(String relatedVehicle) {
    this.relatedVehicle = relatedVehicle;
  }

  public String getRelatedTransportOrder() {
    return relatedTransportOrder;
  }

  public void setRelatedTransportOrder(String relatedTransportOrder) {
    this.relatedTransportOrder = relatedTransportOrder;
  }

  public PeripheralOperationDescription getPeripheralOperation() {
    return peripheralOperation;
  }

  public void setPeripheralOperation(PeripheralOperationDescription peripheralOperation) {
    this.peripheralOperation = peripheralOperation;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }

  public Instant getFinishedTime() {
    return finishedTime;
  }

  public void setFinishedTime(Instant finishedTime) {
    this.finishedTime = finishedTime;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
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
