// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DriveOrderTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 */
public class GetTransportOrderResponseTO {

  private boolean dispensable;

  private String name = "";

  private ObjectHistoryTO history;

  private List<String> dependencies;

  private List<DriveOrderTO> driveOrders;

  private int currentDriveOrderIndex;

  private int currentRouteStepIndex;

  private Instant creationTime;

  private Instant deadline;

  private Instant finishedTime;

  private String peripheralReservationToken;

  private String wrappingSequence;

  private String type = "";

  private State state = State.RAW;

  private String intendedVehicle;

  private String processingVehicle;

  private List<DestinationState> destinations = new ArrayList<>();

  @Nonnull
  private List<Property> properties = List.of();

  public GetTransportOrderResponseTO() {
  }

  public boolean isDispensable() {
    return dispensable;
  }

  public GetTransportOrderResponseTO setDispensable(boolean dispensable) {
    this.dispensable = dispensable;
    return this;
  }

  public String getName() {
    return name;
  }

  public GetTransportOrderResponseTO setName(String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  public ObjectHistoryTO getHistory() {
    return history;
  }

  public GetTransportOrderResponseTO setHistory(ObjectHistoryTO history) {
    this.history = requireNonNull(history, "history");
    return this;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public GetTransportOrderResponseTO setDependencies(List<String> dependencies) {
    this.dependencies = requireNonNull(dependencies, "dependencies");
    return this;
  }

  public List<DriveOrderTO> getDriveOrders() {
    return driveOrders;
  }

  public GetTransportOrderResponseTO setDriveOrders(List<DriveOrderTO> driveOrders) {
    this.driveOrders = requireNonNull(driveOrders, "driveOrders");
    return this;
  }

  public int getCurrentDriveOrderIndex() {
    return currentDriveOrderIndex;
  }

  public GetTransportOrderResponseTO setCurrentDriveOrderIndex(int currentDriveOrderIndex) {
    this.currentDriveOrderIndex = currentDriveOrderIndex;
    return this;
  }

  public int getCurrentRouteStepIndex() {
    return currentRouteStepIndex;
  }

  public GetTransportOrderResponseTO setCurrentRouteStepIndex(int currentRouteStepIndex) {
    this.currentRouteStepIndex = currentRouteStepIndex;
    return this;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public GetTransportOrderResponseTO setCreationTime(Instant creationTime) {
    this.creationTime = requireNonNull(creationTime, "creationTime");
    return this;
  }

  public Instant getDeadline() {
    return deadline;
  }

  public GetTransportOrderResponseTO setDeadline(Instant deadline) {
    this.deadline = requireNonNull(deadline, "deadline");
    return this;
  }

  public Instant getFinishedTime() {
    return finishedTime;
  }

  public GetTransportOrderResponseTO setFinishedTime(Instant finishedTime) {
    this.finishedTime = requireNonNull(finishedTime, "finishedTime");
    return this;
  }

  public String getPeripheralReservationToken() {
    return peripheralReservationToken;
  }

  public GetTransportOrderResponseTO setPeripheralReservationToken(
      String peripheralReservationToken
  ) {
    this.peripheralReservationToken = peripheralReservationToken;
    return this;
  }

  public String getWrappingSequence() {
    return wrappingSequence;
  }

  public GetTransportOrderResponseTO setWrappingSequence(String wrappingSequence) {
    this.wrappingSequence = wrappingSequence;
    return this;
  }

  public String getType() {
    return type;
  }

  public GetTransportOrderResponseTO setType(String type) {
    this.type = type;
    return this;
  }

  public State getState() {
    return state;
  }

  public GetTransportOrderResponseTO setState(State state) {
    this.state = requireNonNull(state, "state");
    return this;
  }

  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public GetTransportOrderResponseTO setIntendedVehicle(String intendedVehicle) {
    this.intendedVehicle = intendedVehicle;
    return this;
  }

  public String getProcessingVehicle() {
    return processingVehicle;
  }

  public GetTransportOrderResponseTO setProcessingVehicle(String processingVehicle) {
    this.processingVehicle = processingVehicle;
    return this;
  }

  @ScheduledApiChange(
      when = "Web API v2",
      details = "Redundant, as the whole drive orders is now reflected."
  )
  public List<DestinationState> getDestinations() {
    return destinations;
  }

  @ScheduledApiChange(
      when = "Web API v2",
      details = "Redundant, as the whole drive orders is now reflected."
  )
  public GetTransportOrderResponseTO setDestinations(List<DestinationState> destinations) {
    this.destinations = requireNonNull(destinations, "destinations");
    return this;
  }

  @Nonnull
  public List<Property> getProperties() {
    return properties;
  }

  public GetTransportOrderResponseTO setProperties(
      @Nonnull
      List<Property> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  // CHECKSTYLE:OFF
  public enum State {

    RAW,
    ACTIVE,
    DISPATCHABLE,
    BEING_PROCESSED,
    WITHDRAWN,
    FINISHED,
    FAILED,
    UNROUTABLE
  }
  // CHECKSTYLE:ON
}
