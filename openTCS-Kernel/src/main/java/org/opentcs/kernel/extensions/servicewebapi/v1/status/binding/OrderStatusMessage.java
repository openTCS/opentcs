/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Size;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * A status message containing details about a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderStatusMessage
    extends StatusMessage {

  @JsonPropertyDescription("The (optional) transport order name")
  private String orderName;

  @JsonPropertyDescription("The processing vehicle's name")
  private String processingVehicleName;

  @JsonPropertyDescription("The transport order's current state")
  private OrderState orderState;

  @JsonPropertyDescription("The transport order's destinations")
  @Size(min = 1)
  private List<Destination> destinations = new LinkedList<>();

  @JsonPropertyDescription("The transport order's properties")
  private List<Property> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public OrderStatusMessage() {
  }

  public String getOrderName() {
    return orderName;
  }

  public void setOrderName(String orderName) {
    this.orderName = orderName;
  }

  public String getProcessingVehicleName() {
    return processingVehicleName;
  }

  public void setProcessingVehicleName(String processingVehicleName) {
    this.processingVehicleName = processingVehicleName;
  }

  public OrderState getOrderState() {
    return orderState;
  }

  public void setOrderState(OrderState orderState) {
    this.orderState = orderState;
  }

  public List<Destination> getDestinations() {
    return destinations;
  }

  public void setDestinations(List<Destination> destinations) {
    this.destinations = destinations;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  public static OrderStatusMessage fromTransportOrder(TransportOrder order,
                                                      long sequenceNumber) {
    return fromTransportOrder(order, sequenceNumber, Instant.now());
  }

  public static OrderStatusMessage fromTransportOrder(TransportOrder order,
                                                      long sequenceNumber,
                                                      Instant creationTimeStamp) {
    OrderStatusMessage orderMessage = new OrderStatusMessage();
    orderMessage.setSequenceNumber(sequenceNumber);
    orderMessage.setCreationTimeStamp(creationTimeStamp);
    orderMessage.setOrderName(order.getName());
    orderMessage.setProcessingVehicleName(
        order.getProcessingVehicle() == null ? null : order.getProcessingVehicle().getName());
    orderMessage.setOrderState(OrderState.fromTransportOrderState(order.getState()));
    for (DriveOrder curDriveOrder : order.getAllDriveOrders()) {
      orderMessage.getDestinations().add(Destination.fromDriveOrder(curDriveOrder));
    }
    for (Map.Entry<String, String> mapEntry : order.getProperties().entrySet()) {
      Property prop = new Property();
      prop.setKey(mapEntry.getKey());
      prop.setValue(mapEntry.getValue());
      orderMessage.getProperties().add(prop);
    }
    return orderMessage;
  }

  /**
   * The various states a transport order may be in.
   */
  public enum OrderState {
    /**
     * A transport order's initial state.
     */
    RAW,
    /**
     * Indicates a transport order's parameters have been set up completely and the kernel should
     * dispatch it when possible.
     */
    ACTIVE,
    /**
     * Marks a transport order as ready to be dispatched to a vehicle.
     */
    DISPATCHABLE,
    /**
     * Marks a transport order as being processed by a vehicle.
     */
    BEING_PROCESSED,
    /**
     * Indicates the transport order is withdrawn from a processing vehicle but not yet in its
     * final state, as the vehicle has not yet finished/cleaned up.
     */
    WITHDRAWN,
    /**
     * Marks a transport order as successfully completed.
     */
    FINISHED,
    /**
     * General failure state that marks a transport order as failed.
     */
    FAILED,
    /**
     * Failure state that marks a transport order as unroutable.
     */
    UNROUTABLE;

    /**
     * Maps a transpor order's {@link TransportOrder#state state} to the corresponding
     * {@link OrderState}.
     *
     * @param state The transport order's state.
     * @return The corresponding OrderState.
     */
    public static OrderState fromTransportOrderState(TransportOrder.State state) {
      switch (state) {
        case RAW:
          return RAW;
        case ACTIVE:
          return ACTIVE;
        case DISPATCHABLE:
          return DISPATCHABLE;
        case BEING_PROCESSED:
          return BEING_PROCESSED;
        case WITHDRAWN:
          return WITHDRAWN;
        case FINISHED:
          return FINISHED;
        case FAILED:
          return FAILED;
        case UNROUTABLE:
          return UNROUTABLE;
        default:
          throw new IllegalArgumentException("Unknown transport order state.");
      }
    }
  }
}
