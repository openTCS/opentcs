// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8;

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
import org.opentcs.kernel.extensions.servicewebapi.common.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostOrderSequenceRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.PostTransportOrderRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.OrderSequenceConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter.TransportOrderConverter;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.OrderSequenceTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.TransportOrderTO;

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
            .withProperties(order.getProperties() == null ? Map.of() : order.getProperties());

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
  public List<TransportOrderTO> getTransportOrders(
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
          .map(transportOrderConverter::convert)
          .sorted(Comparator.comparing(TransportOrderTO::getName))
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
  public TransportOrderTO getTransportOrderByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return orderService.fetch(TransportOrder.class, name)
          .map(transportOrderConverter::convert)
          .orElseThrow(() -> new ObjectUnknownException("Unknown transport order: " + name));
    });
  }

  public OrderSequence createOrderSequence(String name, PostOrderSequenceRequestTO sequence)
      throws ObjectUnknownException,
        ObjectExistsException,
        KernelRuntimeException,
        IllegalStateException {
    requireNonNull(name, "name");
    requireNonNull(sequence, "sequence");

    OrderSequenceCreationTO to = new OrderSequenceCreationTO(name)
        .withFailureFatal(sequence.isFailureFatal())
        .withIncompleteName(sequence.isIncompleteName())
        .withIntendedVehicleName(sequence.getIntendedVehicle())
        .withProperties(sequence.getProperties())
        .withOrderTypes(new HashSet<>(sequence.getOrderTypes()));

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

  public List<OrderSequenceTO> getOrderSequences(
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
          .map(orderSequenceConverter::convert)
          .sorted(Comparator.comparing(OrderSequenceTO::getName))
          .collect(Collectors.toList());
    });
  }

  public OrderSequenceTO getOrderSequenceByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return orderService.fetch(OrderSequence.class, name)
          .map(orderSequenceConverter::convert)
          .orElseThrow(() -> new ObjectUnknownException("Unknown order sequence: " + name));
    });
  }

  private List<DestinationCreationTO> destinations(PostTransportOrderRequestTO order) {
    List<DestinationCreationTO> result = new ArrayList<>(order.getDestinations().size());

    for (PostTransportOrderRequestTO.Destination dest : order.getDestinations()) {
      DestinationCreationTO to = new DestinationCreationTO(
          dest.getLocationName(),
          dest.getOperation()
      );

      if (dest.getProperties() != null) {
        to = to.withProperties(dest.getProperties());
      }

      result.add(to);
    }

    return result;
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
