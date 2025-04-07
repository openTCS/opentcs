// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 */
public class GetTransportOrderResponseTO {

  private boolean dispensable;

  private String name = "";

  private String peripheralReservationToken;

  private String wrappingSequence;

  private String type = "";

  private TransportOrder.State state = TransportOrder.State.RAW;

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

  public TransportOrder.State getState() {
    return state;
  }

  public GetTransportOrderResponseTO setState(TransportOrder.State state) {
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

  public List<DestinationState> getDestinations() {
    return destinations;
  }

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
}
