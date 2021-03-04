/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Transport;

/**
 * Handles requests for creating or withdrawing transport orders.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderHandler {

  /**
   * The service we use to create transport orders.
   */
  private final TransportOrderService orderService;
  /**
   * The service we use to withdraw transport orders.
   */
  private final DispatcherService dispatcherService;
  /**
   * Executes tasks modifying kernel data.
   */
  private final ExecutorService kernelExecutor;

  /**
   * Creates a new instance.
   *
   * @param orderService The service we use to create transport orders.
   * @param dispatcherService The service we use to withdraw transport orders.
   * @param kernelExecutor Executes tasks modifying kernel data.
   */
  @Inject
  public OrderHandler(TransportOrderService orderService,
                      DispatcherService dispatcherService,
                      @KernelExecutor ExecutorService kernelExecutor) {
    this.orderService = requireNonNull(orderService, "orderService");
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  public void createOrder(String name, Transport order)
      throws KernelRuntimeException, IllegalStateException {
    requireNonNull(name, "name");
    requireNonNull(order, "order");

    TransportOrderCreationTO to
        = new TransportOrderCreationTO(name, destinations(order))
            .withIntendedVehicleName(order.getIntendedVehicle())
            .withDependencyNames(new HashSet<>(order.getDependencies()))
            .withDeadline(deadline(order))
            .withProperties(properties(order.getProperties()));

    try {
      kernelExecutor.submit(() -> {
        orderService.createTransportOrder(to);
        dispatcherService.dispatch();
      }).get();
    }
    catch (InterruptedException exc) {
      throw new IllegalStateException("Unexpectedly interrupted");
    }
    catch (ExecutionException exc) {
      if (exc.getCause() instanceof RuntimeException) {
        throw (RuntimeException) exc.getCause();
      }
      throw new KernelRuntimeException(exc.getCause());
    }
  }

  public void withdrawByTransportOrder(String name, boolean immediate, boolean disableVehicle)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    TransportOrder order = orderService.fetchObject(TransportOrder.class, name);
    if (order == null) {
      throw new ObjectUnknownException("Unknown transport order: " + name);
    }

    dispatcherService.withdrawByTransportOrder(order.getReference(), immediate, disableVehicle);
  }

  public void withdrawByVehicle(String name, boolean immediate, boolean disableVehicle)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    Vehicle vehicle = orderService.fetchObject(Vehicle.class, name);
    if (vehicle == null) {
      throw new ObjectUnknownException("Unknown vehicle: " + name);
    }

    dispatcherService.withdrawByVehicle(vehicle.getReference(), immediate, disableVehicle);
  }

  private List<DestinationCreationTO> destinations(Transport order) {
    List<DestinationCreationTO> result = new ArrayList<>(order.getDestinations().size());

    for (Destination dest : order.getDestinations()) {
      DestinationCreationTO to = new DestinationCreationTO(dest.getLocationName(),
                                                           dest.getOperation());

      for (Property prop : dest.getProperties()) {
        to = to.withProperty(prop.getKey(), prop.getValue());
      }

      result.add(to);
    }

    return result;
  }

  private Instant deadline(Transport order) {
    return order.getDeadline() == null ? Instant.MAX : order.getDeadline();
  }

  private Map<String, String> properties(List<Property> properties) {
    Map<String, String> result = new HashMap<>();
    for (Property prop : properties) {
      result.put(prop.getKey(), prop.getValue());
    }
    return result;
  }

}
