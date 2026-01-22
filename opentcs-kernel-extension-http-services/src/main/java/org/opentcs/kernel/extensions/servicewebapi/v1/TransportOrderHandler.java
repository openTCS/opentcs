// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetOrderSequenceResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetTransportOrderResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostOrderSequenceRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostTransportOrderRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.posttransportorder.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.OrderConstantsTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.OrderSequenceConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.TransportOrderConverter;

/**
 * Handles requests related to transport orders and order sequences.
 */
public class TransportOrderHandler {

  private final InternalTransportOrderService orderService;
  private final KernelExecutorWrapper executorWrapper;
  private final OrderSequenceConverter orderSequenceConverter;
  private final TransportOrderConverter transportOrderConverter;

  /**
   * Creates a new instance.
   *
   * @param orderService The service we use to get the transport orders.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public TransportOrderHandler(
      InternalTransportOrderService orderService,
      KernelExecutorWrapper executorWrapper,
      OrderSequenceConverter orderSequenceConverter,
      TransportOrderConverter transportOrderConverter
  ) {
    this.orderService = requireNonNull(orderService, "orderService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
    this.orderSequenceConverter
        = requireNonNull(orderSequenceConverter, "orderSequenceConverter");
    this.transportOrderConverter
        = requireNonNull(transportOrderConverter, "transportOrderConverter");
  }

  public TransportOrder createOrder(String name, PostTransportOrderRequestTO order)
      throws ObjectUnknownException,
        ObjectExistsException,
        KernelRuntimeException,
        IllegalStateException {
    requireNonNull(name, "name");
    requireNonNull(order, "order");

    TransportOrderCreationTO to
        = new TransportOrderCreationTO(name, destinations(order))
            .withIncompleteName(order.isIncompleteName())
            .withDispensable(order.isDispensable())
            .withIntendedVehicleName(order.getIntendedVehicle())
            .withDependencyNames(dependencyNames(order.getDependencies()))
            .withDeadline(deadline(order))
            .withPeripheralReservationToken(order.getPeripheralReservationToken())
            .withWrappingSequence(order.getWrappingSequence())
            .withType(order.getType() == null ? OrderConstants.TYPE_NONE : order.getType())
            .withProperties(properties(order.getProperties()));

    return executorWrapper.callAndWait(() -> {
      return orderService.createTransportOrder(to);
    });
  }

  public void updateTransportOrderIntendedVehicle(
      String orderName,
      @Nullable
      String vehicleName
  )
      throws ObjectUnknownException {
    requireNonNull(orderName, "orderName");

    executorWrapper.callAndWait(() -> {
      TransportOrder order = orderService.fetch(TransportOrder.class, orderName)
          .orElseThrow(() -> new ObjectUnknownException("Unknown transport order: " + orderName));
      Vehicle vehicle = null;
      if (vehicleName != null) {
        vehicle = orderService.fetch(Vehicle.class, vehicleName)
            .orElseThrow(() -> new ObjectUnknownException("Unknown vehicle: " + vehicleName));
      }

      orderService.updateTransportOrderIntendedVehicle(
          order.getReference(),
          vehicle != null ? vehicle.getReference() : null
      );
    });
  }

  /**
   * Find all transport orders and filters depending on the given parameters.
   *
   * @param intendedVehicle The filter parameter for the name of the
   * intended vehicle for the transport order. The filtering is disabled for this parameter if the
   * value is null.
   * @return A list of transport orders that match the filter.
   */
  public List<GetTransportOrderResponseTO> getTransportOrders(
      @Nullable
      String intendedVehicle
  ) {
    return executorWrapper.callAndWait(() -> {
      TCSObjectReference<Vehicle> intendedVehicleRef
          = Optional.ofNullable(intendedVehicle)
              .map(name -> orderService.fetch(Vehicle.class, name).orElse(null))
              .map(Vehicle::getReference)
              .orElse(null);

      if (intendedVehicle != null && intendedVehicleRef == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + intendedVehicle);
      }

      return orderService.stream(TransportOrder.class)
          .filter(Filters.transportOrderWithIntendedVehicle(intendedVehicleRef))
          .map(order -> transportOrderConverter.toGetTransportOrderResponse(order))
          .sorted(Comparator.comparing(GetTransportOrderResponseTO::getName))
          .collect(Collectors.toList());
    });
  }

  /**
   * Finds the transport order with the given name.
   *
   * @param name The name of the requested transport order.
   * @return A single transport order with the given name.
   * @throws ObjectUnknownException If a transport order with the given name does not exist.
   */
  public GetTransportOrderResponseTO getTransportOrderByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return orderService.fetch(TransportOrder.class, name)
          .map(order -> transportOrderConverter.toGetTransportOrderResponse(order))
          .orElseThrow(() -> new ObjectUnknownException("Unknown transport order: " + name));
    });
  }

  @SuppressWarnings("deprecation")
  public OrderSequence createOrderSequence(String name, PostOrderSequenceRequestTO sequence)
      throws ObjectUnknownException,
        ObjectExistsException,
        KernelRuntimeException,
        IllegalStateException {
    requireNonNull(name, "name");
    requireNonNull(sequence, "sequence");

    if (!hasValidTypes(sequence)) {
      throw new IllegalArgumentException(
          "Order sequence must only have either 'type' or 'orderTypes' set."
      );
    }

    OrderSequenceCreationTO to = new OrderSequenceCreationTO(name)
        .withFailureFatal(sequence.isFailureFatal())
        .withIncompleteName(sequence.isIncompleteName())
        .withIntendedVehicleName(sequence.getIntendedVehicle())
        .withProperties(properties(sequence.getProperties()))
        .withType(sequence.getType() != null ? sequence.getType() : OrderConstantsTO.TYPE_UNSET)
        .withOrderTypes(
            sequence.getOrderTypes() != null ? new HashSet<>(sequence.getOrderTypes()) : Set.of()
        );

    return executorWrapper.callAndWait(() -> {
      return orderService.createOrderSequence(to);
    });
  }

  public void putOrderSequenceComplete(String name)
      throws ObjectUnknownException,
        IllegalStateException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      OrderSequence orderSequence = orderService.fetch(OrderSequence.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown order sequence: " + name));
      orderService.markOrderSequenceComplete(orderSequence.getReference());
    });
  }

  public List<GetOrderSequenceResponseTO> getOrderSequences(
      @Nullable
      String intendedVehicle
  ) {
    return executorWrapper.callAndWait(() -> {
      TCSObjectReference<Vehicle> intendedVehicleRef
          = Optional.ofNullable(intendedVehicle)
              .map(name -> orderService.fetch(Vehicle.class, name).orElse(null))
              .map(Vehicle::getReference)
              .orElse(null);

      if (intendedVehicle != null && intendedVehicleRef == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + intendedVehicle);
      }

      return orderService.stream(OrderSequence.class)
          .filter(Filters.orderSequenceWithIntendedVehicle(intendedVehicleRef))
          .map(sequence -> orderSequenceConverter.toGetOrderSequenceResponseTO(sequence))
          .sorted(Comparator.comparing(GetOrderSequenceResponseTO::getName))
          .collect(Collectors.toList());
    });
  }

  public GetOrderSequenceResponseTO getOrderSequenceByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return orderService.fetch(OrderSequence.class, name)
          .map(sequence -> orderSequenceConverter.toGetOrderSequenceResponseTO(sequence))
          .orElseThrow(() -> new ObjectUnknownException("Unknown transport order: " + name));
    });
  }

  private List<DestinationCreationTO> destinations(PostTransportOrderRequestTO order) {
    List<DestinationCreationTO> result = new ArrayList<>(order.getDestinations().size());

    for (Destination dest : order.getDestinations()) {
      DestinationCreationTO to = new DestinationCreationTO(
          dest.getLocationName(),
          dest.getOperation()
      );

      if (dest.getProperties() != null) {
        for (Property prop : dest.getProperties()) {
          to = to.withProperty(prop.getKey(), prop.getValue());
        }
      }

      result.add(to);
    }

    return result;
  }

  private boolean hasValidTypes(PostOrderSequenceRequestTO orderSequenceRequestTO) {
    return (orderSequenceRequestTO.getType() != null)
        ^ (orderSequenceRequestTO.getOrderTypes() != null);
  }

  private Set<String> dependencyNames(List<String> dependencies) {
    return dependencies == null ? new HashSet<>() : new HashSet<>(dependencies);
  }

  private Instant deadline(PostTransportOrderRequestTO order) {
    return order.getDeadline() == null ? Instant.MAX : order.getDeadline();
  }

  private Map<String, String> properties(List<Property> properties) {
    Map<String, String> result = new HashMap<>();
    if (properties != null) {
      for (Property prop : properties) {
        result.put(prop.getKey(), prop.getValue());
      }
    }
    return result;
  }
}
