// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents;

import java.time.Instant;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralJobStateTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A status message containing information about a peripheral job.
 */
public class PeripheralJobStatusMessage
    extends
      StatusMessage {

  private String name;

  private String reservationToken;

  private String relatedVehicle;

  private String relatedTransportOrder;

  private PeripheralOperationDescription peripheralOperation;

  private PeripheralJobStateTO state;

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
      PeripheralOperationDescription peripheralOperation
  ) {
    this.peripheralOperation = peripheralOperation;
    return this;
  }

  public PeripheralJobStateTO getState() {
    return state;
  }

  public PeripheralJobStatusMessage setState(PeripheralJobStateTO state) {
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
}
