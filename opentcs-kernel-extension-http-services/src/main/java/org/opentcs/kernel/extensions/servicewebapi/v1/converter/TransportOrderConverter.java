// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetTransportOrderResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.OrderStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DriveOrderTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.RouteTO;

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

  @SuppressWarnings("deprecation")
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
    transportOrderState.setHistory(convertObjectHistory(transportOrder.getHistory()));
    transportOrderState.setDependencies(convertDependencies(transportOrder.getDependencies()));
    transportOrderState.setDriveOrders(convertDriveOrders(transportOrder.getAllDriveOrders()));
    transportOrderState.setCurrentDriveOrderIndex(transportOrder.getCurrentDriveOrderIndex());
    transportOrderState.setCurrentRouteStepIndex(transportOrder.getCurrentRouteStepIndex());
    transportOrderState.setCreationTime(transportOrder.getCreationTime());
    transportOrderState.setDeadline(transportOrder.getDeadline());
    transportOrderState.setFinishedTime(transportOrder.getFinishedTime());
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

  private List<String> convertDependencies(Set<TCSObjectReference<TransportOrder>> dependencies) {
    return dependencies
        .stream()
        .map(ref -> nameOfNullableReference(ref))
        .toList();
  }

  private List<DriveOrderTO> convertDriveOrders(List<DriveOrder> driveOrders) {
    return driveOrders
        .stream()
        .map(
            driveOrder -> new DriveOrderTO(
                driveOrder.getName(),
                convertDestination(driveOrder.getDestination()),
                driveOrder.getTransportOrder().getName(),
                toRouteTO(driveOrder.getRoute()),
                convertState(driveOrder.getState())
            )
        )
        .collect(Collectors.toList());
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

  private DriveOrderTO.StateTO convertState(DriveOrder.State state) {
    return switch (state) {
      case FAILED -> DriveOrderTO.StateTO.FAILED;
      case FINISHED -> DriveOrderTO.StateTO.FINISHED;
      case PRISTINE -> DriveOrderTO.StateTO.PRISTINE;
      case OPERATING -> DriveOrderTO.StateTO.OPERATING;
      case TRAVELLING -> DriveOrderTO.StateTO.TRAVELLING;
    };
  }

  private DriveOrderTO.DestinationTO convertDestination(DriveOrder.Destination destination) {
    if (destination == null) {
      return null;
    }
    return new DriveOrderTO.DestinationTO(
        destination.getDestination().getName(),
        destination.getOperation(),
        convertProperties(destination.getProperties())
    );
  }

  private ObjectHistoryTO convertObjectHistory(ObjectHistory history) {
    return new ObjectHistoryTO(
        history.getEntries()
            .stream()
            .map(
                entry -> new ObjectHistoryTO.ObjectHistoryEntryTO(
                    entry.getTimestamp(),
                    entry.getEventCode(),
                    entry.getSupplements()
                )
            ).toList()
    );
  }

  private RouteTO toRouteTO(Route route) {
    if (route == null) {
      return null;
    }
    return new RouteTO(route.getCosts(), toSteps(route.getSteps()));
  }

  private List<RouteTO.Step> toSteps(List<Route.Step> steps) {
    return steps.stream()
        .map(
            step -> new RouteTO.Step(
                (step.getPath() != null) ? step.getPath().getName() : null,
                (step.getSourcePoint() != null) ? step.getSourcePoint().getName() : null,
                step.getDestinationPoint().getName(),
                convertVehicleOrientation(step.getVehicleOrientation()),
                step.getRouteIndex(),
                step.getCosts(),
                step.isExecutionAllowed(),
                (step.getReroutingType() != null) ? convertReroutingType(step.getReroutingType())
                    : null
            )
        )
        .collect(Collectors.toList());
  }

  private RouteTO.Step.VehicleOrientationTO convertVehicleOrientation(
      Vehicle.Orientation vehicleOrientation
  ) {
    return switch (vehicleOrientation) {
      case FORWARD -> RouteTO.Step.VehicleOrientationTO.FORWARD;
      case BACKWARD -> RouteTO.Step.VehicleOrientationTO.BACKWARD;
      case UNDEFINED -> RouteTO.Step.VehicleOrientationTO.UNDEFINED;
    };
  }

  private RouteTO.Step.ReroutingTypeTO convertReroutingType(ReroutingType reroutingType) {
    return switch (reroutingType) {
      case REGULAR -> RouteTO.Step.ReroutingTypeTO.REGULAR;
      case FORCED -> RouteTO.Step.ReroutingTypeTO.FORCED;
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
