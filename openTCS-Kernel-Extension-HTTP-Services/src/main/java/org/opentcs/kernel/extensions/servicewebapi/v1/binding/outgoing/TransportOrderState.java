/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.outgoing;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.TransportOrder;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class TransportOrderState {

  private String name = "";
  
  private String type = "";

  private TransportOrder.State state = TransportOrder.State.RAW;

  private String intendedVehicle;

  private String processingVehicle;

  private List<Destination> destinations = new ArrayList<>();

  private TransportOrderState() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = requireNonNull(name, "name");
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
   * Creates a new instance from a <code>TransportOrder</code>.
   *
   * @param transportOrder The transport order to create an instance from.
   * @return A new instance containing the data from the given transport order.
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
