// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetTransportOrderResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.OrderStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Includes the conversion methods for all TransportOrder classes.
 */
public class TransportOrderConverter {

  public TransportOrderConverter() {
  }

  public OrderStatusMessage toOrderStatusMessage(
      TransportOrder order,
      long sequenceNumber,
      Instant creationTimeStamp
  ) {
    OrderStatusMessage orderMessage = new OrderStatusMessage();
    orderMessage.setSequenceNumber(sequenceNumber);
    orderMessage.setCreationTimeStamp(creationTimeStamp);
    orderMessage.setOrderName(order.getName());
    orderMessage.setDispensable(order.isDispensable());
    orderMessage.setPeripheralReservationToken(order.getPeripheralReservationToken());
    orderMessage.setWrappingSequence(nameOfNullableReference(order.getWrappingSequence()));
    orderMessage.setOrderType(order.getType());
    orderMessage.setIntendedVehicle(nameOfNullableReference(order.getIntendedVehicle()));
    orderMessage.setProcessingVehicleName(
        order.getProcessingVehicle() == null ? null : order.getProcessingVehicle().getName()
    );
    orderMessage.setOrderState(
        OrderStatusMessage.OrderState.fromTransportOrderState(order.getState())
    );
    for (DriveOrder curDriveOrder : order.getAllDriveOrders()) {
      orderMessage.getDestinations().add(DestinationState.fromDriveOrder(curDriveOrder));
    }
    for (Map.Entry<String, String> mapEntry : order.getProperties().entrySet()) {
      orderMessage.getProperties().add(new Property(mapEntry.getKey(), mapEntry.getValue()));
    }
    return orderMessage;
  }

  /**
   * Creates a new instance from a <code>TransportOrder</code>.
   *
   * @param transportOrder The transport order to create an instance from.
   * @return A new instance containing the data from the given transport order.
   */
  public GetTransportOrderResponseTO toGetTransportOrderResponse(TransportOrder transportOrder) {
    if (transportOrder == null) {
      return null;
    }
    GetTransportOrderResponseTO transportOrderState = new GetTransportOrderResponseTO();
    transportOrderState.setDispensable(transportOrder.isDispensable());
    transportOrderState.setName(transportOrder.getName());
    transportOrderState.setPeripheralReservationToken(
        transportOrder.getPeripheralReservationToken()
    );
    transportOrderState.setWrappingSequence(
        nameOfNullableReference(transportOrder.getWrappingSequence())
    );
    transportOrderState.setType(transportOrder.getType());
    transportOrderState.setDestinations(
        transportOrder.getAllDriveOrders()
            .stream()
            .map(driveOrder -> DestinationState.fromDriveOrder(driveOrder))
            .collect(Collectors.toList())
    );
    transportOrderState.setIntendedVehicle(
        nameOfNullableReference(transportOrder.getIntendedVehicle())
    );
    transportOrderState.setProcessingVehicle(
        nameOfNullableReference(transportOrder.getProcessingVehicle())
    );
    transportOrderState.setState(convertState(transportOrder.getState()));
    transportOrderState.setProperties(convertProperties(transportOrder.getProperties()));
    return transportOrderState;
  }

  private GetTransportOrderResponseTO.State convertState(TransportOrder.State state) {
    return switch (state) {
      case UNROUTABLE -> GetTransportOrderResponseTO.State.UNROUTABLE;
      case ACTIVE -> GetTransportOrderResponseTO.State.ACTIVE;
      case BEING_PROCESSED -> GetTransportOrderResponseTO.State.BEING_PROCESSED;
      case DISPATCHABLE -> GetTransportOrderResponseTO.State.DISPATCHABLE;
      case RAW -> GetTransportOrderResponseTO.State.RAW;
      case WITHDRAWN -> GetTransportOrderResponseTO.State.WITHDRAWN;
      case FINISHED -> GetTransportOrderResponseTO.State.FINISHED;
      case FAILED -> GetTransportOrderResponseTO.State.FAILED;
    };
  }

  private static String nameOfNullableReference(
      @Nullable
      TCSObjectReference<?> reference
  ) {
    return reference == null ? null : reference.getName();
  }

  private static List<Property> convertProperties(Map<String, String> properties) {
    return properties.entrySet().stream()
        .map(property -> new Property(property.getKey(), property.getValue()))
        .collect(Collectors.toList());
  }
}
