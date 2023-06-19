/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A status message containing details about a transport order.
 */
public class OrderStatusMessage
    extends StatusMessage {

  private String orderName;

  private String processingVehicleName;

  private OrderState orderState;

  private List<DestinationState> destinations = new ArrayList<>();

  private List<Property> properties = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public OrderStatusMessage() {
  }

  @Override
  public OrderStatusMessage setSequenceNumber(long sequenceNumber) {
    return (OrderStatusMessage) super.setSequenceNumber(sequenceNumber);
  }

  @Override
  public OrderStatusMessage setCreationTimeStamp(Instant creationTimeStamp) {
    return (OrderStatusMessage) super.setCreationTimeStamp(creationTimeStamp);
  }

  public String getOrderName() {
    return orderName;
  }

  public OrderStatusMessage setOrderName(String orderName) {
    this.orderName = orderName;
    return this;
  }

  public String getProcessingVehicleName() {
    return processingVehicleName;
  }

  public OrderStatusMessage setProcessingVehicleName(String processingVehicleName) {
    this.processingVehicleName = processingVehicleName;
    return this;
  }

  public OrderState getOrderState() {
    return orderState;
  }

  public OrderStatusMessage setOrderState(OrderState orderState) {
    this.orderState = orderState;
    return this;
  }

  public List<DestinationState> getDestinations() {
    return destinations;
  }

  public OrderStatusMessage setDestinations(List<DestinationState> destinations) {
    this.destinations = destinations;
    return this;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public OrderStatusMessage setProperties(List<Property> properties) {
    this.properties = properties;
    return this;
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
      orderMessage.getDestinations().add(DestinationState.fromDriveOrder(curDriveOrder));
    }
    for (Map.Entry<String, String> mapEntry : order.getProperties().entrySet()) {
      orderMessage.getProperties().add(new Property(mapEntry.getKey(), mapEntry.getValue()));
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
