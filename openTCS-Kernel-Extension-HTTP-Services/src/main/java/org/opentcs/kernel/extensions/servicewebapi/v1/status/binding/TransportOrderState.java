/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class TransportOrderState {

  @JsonPropertyDescription("The name of the transport order.")
  private String name = "";
  
  @JsonPropertyDescription("The type of the transport order.")
  private String type = "";

  @JsonPropertyDescription("The transport order's current state.")
  private TransportOrder.State state = TransportOrder.State.RAW;

  @JsonPropertyDescription(
      "The name of the vehicle that is intended to process the transport order.")
  private String intendedVehicle;

  @JsonPropertyDescription(
      "The name of the vehicle currently processing the transport order.")
  private String processingVehicle;

  @JsonPropertyDescription("The sequence of destinations of the transport order.")
  private List<Destination> destinations = new ArrayList<>();

  private TransportOrderState() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = requireNonNull(name, "name");
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed in favor of order types.")
  public String getCategory() {
    return type;
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed in favor of order types.")
  public void setCategory(String category) {
    this.type = requireNonNull(category, "category");
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public TransportOrder.State getState() {
    return state;
  }

  public void setState(TransportOrder.State state) {
    this.state = requireNonNull(state, "state");
  }

  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  public void setIntendedVehicle(String intendedVehicle) {
    this.intendedVehicle = intendedVehicle;
  }

  public String getProcessingVehicle() {
    return processingVehicle;
  }

  public void setProcessingVehicle(String processingVehicle) {
    this.processingVehicle = processingVehicle;
  }

  public List<Destination> getDestinations() {
    return destinations;
  }

  public void setDestinations(List<Destination> destinations) {
    this.destinations = requireNonNull(destinations, "destinations");
  }

  /**
   * Creates a <Code>TransportOrderState</Code> instance from a <Code>TransportOrder</Code> instance.
   *
   * @param transportOrder The transport order whose properties will be used to create a
   * <Code>TransportOrderState</Code> instance.
   * @return A new <Code>TransportOrderState</Code> instance filled with data from
   * the given transport order.
   */
  public static TransportOrderState fromTransportOrder(TransportOrder transportOrder) {
    if (transportOrder == null) {
      return null;
    }
    TransportOrderState transportOrderState = new TransportOrderState();
    transportOrderState.setName(transportOrder.getName());
    transportOrderState.setType(transportOrder.getType());
    transportOrderState.setDestinations(
        transportOrder.getAllDriveOrders()
            .stream()
            .map(driveOrder -> Destination.fromDriveOrder(driveOrder))
            .collect(Collectors.toList()));
    transportOrderState.setIntendedVehicle(
        nameOfNullableReference(transportOrder.getIntendedVehicle()));
    transportOrderState.setProcessingVehicle(
        nameOfNullableReference(transportOrder.getProcessingVehicle()));
    transportOrderState.setState(transportOrder.getState());
    return transportOrderState;
  }

  private static String nameOfNullableReference(@Nullable TCSObjectReference<?> reference) {
    return reference == null ? null : reference.getName();
  }
}
