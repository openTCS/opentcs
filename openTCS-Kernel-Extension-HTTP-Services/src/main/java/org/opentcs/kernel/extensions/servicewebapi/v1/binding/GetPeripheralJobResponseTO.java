/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralJob.State;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * The current state of a peripheral job.
 */
public class GetPeripheralJobResponseTO {

  private String name;

  private String reservationToken;

  private String relatedVehicle;

  private String relatedTransportOrder;

  private PeripheralOperationDescription peripheralOperation;

  private State state;

  private Instant creationTime;

  private Instant finishedTime;

  private List<Property> properties;

  public GetPeripheralJobResponseTO() {
  }

  public String getName() {
    return name;
  }

  public GetPeripheralJobResponseTO setName(String name) {
    this.name = name;
    return this;
  }

  public String getReservationToken() {
    return reservationToken;
  }

  public GetPeripheralJobResponseTO setReservationToken(String reservationToken) {
    this.reservationToken = reservationToken;
    return this;
  }

  public String getRelatedVehicle() {
    return relatedVehicle;
  }

  public GetPeripheralJobResponseTO setRelatedVehicle(String relatedVehicle) {
    this.relatedVehicle = relatedVehicle;
    return this;
  }

  public String getRelatedTransportOrder() {
    return relatedTransportOrder;
  }

  public GetPeripheralJobResponseTO setRelatedTransportOrder(String relatedTransportOrder) {
    this.relatedTransportOrder = relatedTransportOrder;
    return this;
  }

  public PeripheralOperationDescription getPeripheralOperation() {
    return peripheralOperation;
  }

  public GetPeripheralJobResponseTO setPeripheralOperation(
      PeripheralOperationDescription peripheralOperation) {
    this.peripheralOperation = peripheralOperation;
    return this;
  }

  public State getState() {
    return state;
  }

  public GetPeripheralJobResponseTO setState(State state) {
    this.state = state;
    return this;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public GetPeripheralJobResponseTO setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  public Instant getFinishedTime() {
    return finishedTime;
  }

  public GetPeripheralJobResponseTO setFinishedTime(Instant finishedTime) {
    this.finishedTime = finishedTime;
    return this;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public GetPeripheralJobResponseTO setProperties(List<Property> properties) {
    this.properties = properties;
    return this;
  }

  public static GetPeripheralJobResponseTO fromPeripheralJob(PeripheralJob job) {
    GetPeripheralJobResponseTO state = new GetPeripheralJobResponseTO();
    state.name = job.getName();
    state.reservationToken = job.getReservationToken();
    if (job.getRelatedVehicle() != null) {
      state.relatedVehicle = job.getRelatedVehicle().getName();
    }
    if (job.getRelatedTransportOrder() != null) {
      state.relatedTransportOrder = job.getRelatedTransportOrder().getName();
    }
    state.peripheralOperation
        = PeripheralOperationDescription.fromPeripheralOperation(job.getPeripheralOperation());
    state.state = job.getState();
    state.creationTime = job.getCreationTime();
    state.finishedTime = job.getFinishedTime();
    state.properties = job.getProperties().entrySet().stream()
        .map(entry -> new Property(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    return state;
  }
}
