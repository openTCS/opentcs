/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.TransportOrderService;
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
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Handles requests related to transport orders and order sequences.
 */
public class TransportOrderHandler {

  private final TransportOrderService orderService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param orderService The service we use to get the transport orders.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public TransportOrderHandler(TransportOrderService orderService,
                               KernelExecutorWrapper executorWrapper) {
    this.orderService = requireNonNull(orderService, "orderService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
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

  public void updateTransportOrderIntendedVehicle(String orderName, @Nullable String vehicleName)
      throws ObjectUnknownException {
    requireNonNull(orderName, "orderName");

    executorWrapper.callAndWait(() -> {
      TransportOrder order = orderService.fetchObject(TransportOrder.class, orderName);
      if (order == null) {
        throw new ObjectUnknownException("Unknown transport order: " + orderName);
      }
      Vehicle vehicle = null;
      if (vehicleName != null) {
        vehicle = orderService.fetchObject(Vehicle.class, vehicleName);
        if (vehicle == null) {
          throw new ObjectUnknownException("Unknown vehicle: " + vehicleName);
        }
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
      @Nullable String intendedVehicle
  ) {
    return executorWrapper.callAndWait(() -> {
      TCSObjectReference<Vehicle> intendedVehicleRef
          = Optional.ofNullable(intendedVehicle)
              .map(name -> orderService.fetchObject(Vehicle.class, name))
              .map(Vehicle::getReference)
              .orElse(null);

      if (intendedVehicle != null && intendedVehicleRef == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + intendedVehicle);
      }

      return orderService.fetchObjects(
          TransportOrder.class,
          Filters.transportOrderWithIntendedVehicle(intendedVehicleRef)
      )
          .stream()
          .map(GetTransportOrderResponseTO::fromTransportOrder)
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
      return Optional.ofNullable(orderService.fetchObject(TransportOrder.class, name))
          .map(GetTransportOrderResponseTO::fromTransportOrder)
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
        .withProperties(properties(sequence.getProperties()))
        .withType(sequence.getType());

    return executorWrapper.callAndWait(() -> {
      return orderService.createOrderSequence(to);
    });
  }

  public void putOrderSequenceComplete(String name)
      throws ObjectUnknownException,
             IllegalStateException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      OrderSequence orderSequence = orderService.fetchObject(OrderSequence.class, name);
      if (orderSequence == null) {
        throw new ObjectUnknownException("Unknown order sequence: " + name);
      }
      orderService.markOrderSequenceComplete(orderSequence.getReference());
    });
  }

  public List<GetOrderSequenceResponseTO> getOrderSequences(@Nullable String intendedVehicle) {
    return executorWrapper.callAndWait(() -> {
      TCSObjectReference<Vehicle> intendedVehicleRef
          = Optional.ofNullable(intendedVehicle)
              .map(name -> orderService.fetchObject(Vehicle.class, name))
              .map(Vehicle::getReference)
              .orElse(null);

      if (intendedVehicle != null && intendedVehicleRef == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + intendedVehicle);
      }

      return orderService.fetchObjects(
          OrderSequence.class,
          Filters.orderSequenceWithIntendedVehicle(intendedVehicleRef)
      )
          .stream()
          .map(GetOrderSequenceResponseTO::fromOrderSequence)
          .sorted(Comparator.comparing(GetOrderSequenceResponseTO::getName))
          .collect(Collectors.toList());
    });
  }

  public GetOrderSequenceResponseTO getOrderSequenceByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return Optional.ofNullable(orderService.fetchObject(OrderSequence.class, name))
          .map(GetOrderSequenceResponseTO::fromOrderSequence)
          .orElseThrow(() -> new ObjectUnknownException("Unknown transport order: " + name));
    });
  }

  private List<DestinationCreationTO> destinations(PostTransportOrderRequestTO order) {
    List<DestinationCreationTO> result = new ArrayList<>(order.getDestinations().size());

    for (Destination dest : order.getDestinations()) {
      DestinationCreationTO to = new DestinationCreationTO(dest.getLocationName(),
                                                           dest.getOperation());

      if (dest.getProperties() != null) {
        for (Property prop : dest.getProperties()) {
          to = to.withProperty(prop.getKey(), prop.getValue());
        }
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
