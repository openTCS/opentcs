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
 * The current state of a peripheral job.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class PeripheralJobState {

  private String name;

  private String reservationToken;

  private String relatedVehicle;

  private String relatedTransportOrder;

  private PeripheralOperationDescription peripheralOperation;

  private State state;

  private Instant creationTime;

  private Instant finishedTime;

  private List<Property> properties;

  private PeripheralJobState() {
  }

  public String getName() {
    return name;
  }

  public String getReservationToken() {
    return reservationToken;
  }

  public String getRelatedVehicle() {
    return relatedVehicle;
  }

  public String getRelatedTransportOrder() {
    return relatedTransportOrder;
  }

  public PeripheralOperationDescription getPeripheralOperation() {
    return peripheralOperation;
  }

  public State getState() {
    return state;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public Instant getFinishedTime() {
    return finishedTime;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public static PeripheralJobState fromPeripheralJob(PeripheralJob job) {
    PeripheralJobState state = new PeripheralJobState();
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
