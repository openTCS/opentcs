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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.incoming.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.incoming.Job;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.incoming.Transport;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

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
   * The service we use to update vehicle states.
   */
  private final VehicleService vehicleService;
  /**
   * The service we use to withdraw transport orders.
   */
  private final DispatcherService dispatcherService;
  /**
   * The service we use to create peripheral jobs.
   */
  private final PeripheralJobService jobService;
  /**
   * The service we use to dispatch peripheral jobs.
   */
  private final PeripheralDispatcherService jobDispatcherService;
  /**
   * Executes tasks modifying kernel data.
   */
  private final ExecutorService kernelExecutor;

  /**
   * Creates a new instance.
   *
   * @param orderService Used to create transport orders.
   * @param vehicleService Used to update vehicle state.
   * @param dispatcherService Used to withdraw transport orders.
   * @param jobService Used to create peripheral jobs.
   * @param jobDispatcherService Used to dispatch peripheral jobs.
   * @param kernelExecutor Executes tasks modifying kernel data.
   */
  @Inject
  public OrderHandler(TransportOrderService orderService,
                      VehicleService vehicleService,
                      DispatcherService dispatcherService,
                      PeripheralJobService jobService,
                      PeripheralDispatcherService jobDispatcherService,
                      @KernelExecutor ExecutorService kernelExecutor) {
    this.orderService = requireNonNull(orderService, "orderService");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.jobService = requireNonNull(jobService, "jobService");
    this.jobDispatcherService = requireNonNull(jobDispatcherService, "jobDispatcherService");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  public TransportOrder createOrder(String name, Transport order)
      throws ObjectUnknownException,
             ObjectExistsException,
             KernelRuntimeException,
             IllegalStateException {
    requireNonNull(name, "name");
    requireNonNull(order, "order");

    TransportOrderCreationTO to
        = new TransportOrderCreationTO(name, destinations(order))
            .withIncompleteName(order.hasIncompleteName())
            .withIntendedVehicleName(order.getIntendedVehicle())
            .withDependencyNames(dependencyNames(order.getDependencies()))
            .withDeadline(deadline(order))
            .withProperties(properties(order.getProperties()));

    try {
      return kernelExecutor.submit(
          () -> {
            TransportOrder result = orderService.createTransportOrder(to);
            return result;
          }
      ).get();
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

  public PeripheralJob createPeripheralJob(String name, Job job) {
    requireNonNull(name, "name");
    requireNonNull(job, "job");

    // Check if the vehicle, location and transport order exist.
    if (job.getRelatedVehicle() != null
        && vehicleService.fetchObject(Vehicle.class, job.getRelatedVehicle()) == null) {
      throw new ObjectUnknownException("Unknown vehicle: " + job.getRelatedVehicle());
    }
    if (job.getRelatedTransportOrder() != null
        && vehicleService.fetchObject(TransportOrder.class,
                                      job.getRelatedTransportOrder()) == null) {
      throw new ObjectUnknownException(
          "Unknown transport order: " + job.getRelatedTransportOrder()
      );
    }
    if (job.getPeripheralOperation().getLocationName() != null
        && vehicleService.fetchObject(Location.class,
                                      job.getPeripheralOperation().getLocationName()) == null) {
      throw new ObjectUnknownException(
          "Unknown location: " + job.getPeripheralOperation().getLocationName()
      );
    }

    PeripheralOperationCreationTO operationCreationTO = new PeripheralOperationCreationTO(
        job.getPeripheralOperation().getOperation(),
        job.getPeripheralOperation().getLocationName())
        .withCompletionRequired(job.getPeripheralOperation().isCompletionRequired());
    if (job.getPeripheralOperation().getExecutionTrigger() != null) {
      operationCreationTO = operationCreationTO
          .withExecutionTrigger(job.getPeripheralOperation().getExecutionTrigger());
    }

    PeripheralJobCreationTO jobCreationTO = new PeripheralJobCreationTO(
        name,
        job.getReservationToken(),
        operationCreationTO)
        .withIncompleteName(job.isIncompleteName());
    if (job.getProperties() != null) {
      jobCreationTO = jobCreationTO.withProperties(job.getProperties().stream()
          .collect(Collectors.toMap(
              property -> property.getKey(),
              property -> property.getValue()
          ))
      );
    }
    if (job.getRelatedTransportOrder() != null) {
      jobCreationTO = jobCreationTO.withRelatedTransportOrderName(job.getRelatedTransportOrder());
    }
    if (job.getRelatedVehicle() != null) {
      jobCreationTO = jobCreationTO.withRelatedVehicleName(job.getRelatedVehicle());
    }

    try {
      final PeripheralJobCreationTO finalJobCreationTO = jobCreationTO;
      return kernelExecutor.submit(
          () -> {
            PeripheralJob result = jobService.createPeripheralJob(finalJobCreationTO);
            return result;
          }
      ).get();
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

  public void triggerDispatcher() {
    kernelExecutor.submit(() -> dispatcherService.dispatch());
  }

  public void triggerJobDispatcher() {
    kernelExecutor.submit(() -> jobDispatcherService.dispatch());
  }

  public void withdrawByTransportOrder(String name, boolean immediate, boolean disableVehicle)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    if (orderService.fetchObject(TransportOrder.class, name) == null) {
      throw new ObjectUnknownException("Unknown transport order: " + name);
    }

    kernelExecutor.submit(() -> {
      TransportOrder order = orderService.fetchObject(TransportOrder.class, name);
      if (disableVehicle && order.getProcessingVehicle() != null) {
        vehicleService.updateVehicleIntegrationLevel(order.getProcessingVehicle(),
                                                     Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      }

      dispatcherService.withdrawByTransportOrder(order.getReference(), immediate);
    });
  }

  public void withdrawByVehicle(String name, boolean immediate, boolean disableVehicle)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    Vehicle vehicle = orderService.fetchObject(Vehicle.class, name);
    if (vehicle == null) {
      throw new ObjectUnknownException("Unknown vehicle: " + name);
    }

    kernelExecutor.submit(() -> {
      if (disableVehicle) {
        vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(),
                                                     Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      }

      dispatcherService.withdrawByVehicle(vehicle.getReference(), immediate);
    });
  }

  private List<DestinationCreationTO> destinations(Transport order) {
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

  private Instant deadline(Transport order) {
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
