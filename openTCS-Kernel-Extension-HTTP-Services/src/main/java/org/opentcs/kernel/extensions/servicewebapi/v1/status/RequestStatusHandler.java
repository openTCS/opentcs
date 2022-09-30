/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.PeripheralJobState;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.TransportOrderState;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.VehicleState;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.filter.PeripheralJobFilter;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.filter.TransportOrderFilter;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.filter.VehicleFilter;

/**
 * Handles requests for getting the current state of model elements.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class RequestStatusHandler {

  /**
   * The service we use to fetch objects.
   */
  private final TransportOrderService orderService;
  /**
   * Used to update vehicle instances.
   */
  private final VehicleService vehicleService;
  /**
   * The kernel's executor service.
   */
  private final ExecutorService kernelExecutor;

  /**
   * Creates a new instance.
   *
   * @param orderService The service we use to get the transport orders.
   * @param vehicleService Used to update vehicle instances.
   * @param kernelExecutor The kernel's executor service.
   */
  @Inject
  public RequestStatusHandler(TransportOrderService orderService,
                              VehicleService vehicleService,
                              @KernelExecutor ExecutorService kernelExecutor) {
    this.orderService = requireNonNull(orderService, "orderService");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  /**
   * Find all transport orders and filters depending on the given parameters.
   *
   * @param intendedVehicle The filter parameter for the name of the
   * intended vehicle for the transport order. The filtering is disabled for this parameter if the
   * value is null.
   * @return A list of transport orders that match the filter.
   */
  public List<TransportOrderState> getTransportOrdersState(@Nullable String intendedVehicle) {
    if (intendedVehicle != null) {
      Vehicle vehicle = orderService.fetchObject(Vehicle.class, intendedVehicle);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + intendedVehicle);
      }
    }

    return orderService.fetchObjects(TransportOrder.class,
                                     new TransportOrderFilter(intendedVehicle))
        .stream()
        .map(order -> TransportOrderState.fromTransportOrder(order))
        .collect(Collectors.toList());
  }

  /**
   * Finds the transport order with the given name.
   *
   * @param name The name of the requested transport order.
   * @return A single transport order with the given name.
   * @throws ObjectUnknownException If a transport order with the given name does not exist.
   */
  public TransportOrderState getTransportOrderByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return orderService.fetchObjects(TransportOrder.class,
                                     t -> t.getName().equals(name))
        .stream()
        .map(t -> TransportOrderState.fromTransportOrder(t))
        .findAny()
        .orElseThrow(() -> new ObjectUnknownException("Unknown transport order: " + name));
  }

  /**
   * Returns all peripheral jobs, optionally filtered using the given parameters.
   *
   * @param relatedVehicle Which vehicle to filter peripheral jobs for. Not filtered if the value is
   * null.
   * @param relatedTransportOrder Which transport order to filter peripheral jobs for. Not filtered
   * if the value is null.
   * @return List of peripheral job states.
   */
  public List<PeripheralJobState> getPeripheralJobs(@Nullable String relatedVehicle,
                                                    @Nullable String relatedTransportOrder) {
    // If a related vehicle or transport order is set, make sure they exist.
    if (relatedVehicle != null && orderService.fetchObject(Vehicle.class, relatedVehicle) == null) {
      throw new ObjectUnknownException("Unknown vehicle: " + relatedVehicle);
    }
    if (relatedTransportOrder != null
        && orderService.fetchObject(TransportOrder.class, relatedTransportOrder) == null) {
      throw new ObjectUnknownException("Unknown transport order: " + relatedTransportOrder);
    }

    return orderService.fetchObjects(PeripheralJob.class,
                                     new PeripheralJobFilter(relatedVehicle, relatedTransportOrder))
        .stream()
        .map(peripheralJob -> PeripheralJobState.fromPeripheralJob(peripheralJob))
        .collect(Collectors.toList());
  }

  /**
   * Find a peripheral job by name.
   *
   * @param name The name of the peripheral job.
   * @return The peripheral job state.
   */
  public PeripheralJobState getPeripheralJobByName(@Nonnull String name) {
    requireNonNull(name, "name");

    PeripheralJob job = orderService.fetchObject(PeripheralJob.class, name);
    if (job == null) {
      throw new ObjectUnknownException("Unknown peripheral job: " + name);
    }

    return PeripheralJobState.fromPeripheralJob(job);
  }

  /**
   * Find all vehicles orders and filters depending on the given parameters.
   *
   * @param procState The filter parameter for the processing state of the vehicle.
   * The filtering is disabled for this parameter if the value is null.
   * @return A list of vehicles, that match the filter.
   */
  public List<VehicleState> getVehiclesState(@Nullable String procState) {
    List<VehicleState> vehicles = orderService.fetchObjects(Vehicle.class,
                                                            new VehicleFilter(procState))
        .stream()
        .map(vehicle -> VehicleState.fromVehicle(vehicle))
        .collect(Collectors.toList());
    return vehicles;
  }

  /**
   * Finds the vehicle with the given name.
   *
   * @param name The name of the requested vehicle.
   * @return A single vehicle that has the given name.
   * @throws ObjectUnknownException If a vehicle with the given name does not exist.
   */
  public VehicleState getVehicleStateByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return orderService.fetchObjects(Vehicle.class, v -> v.getName().equals(name))
        .stream()
        .map(v -> VehicleState.fromVehicle(v))
        .findAny()
        .orElseThrow(() -> new ObjectUnknownException("Unknown vehicle: " + name));
  }

  public void putVehicleIntegrationLevel(String name, String value)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    Vehicle vehicle = orderService.fetchObject(Vehicle.class, name);
    if (vehicle == null) {
      throw new ObjectUnknownException("Unknown vehicle: " + name);
    }

    Vehicle.IntegrationLevel level = Vehicle.IntegrationLevel.valueOf(value);

    kernelExecutor.submit(
        () -> vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(), level)
    );
  }

  public void putVehiclePaused(String name, String value)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    Vehicle vehicle = orderService.fetchObject(Vehicle.class, name);
    if (vehicle == null) {
      throw new ObjectUnknownException("Unknown vehicle: " + name);
    }

    boolean paused = Boolean.parseBoolean(value);

    kernelExecutor.submit(
        () -> vehicleService.updateVehiclePaused(vehicle.getReference(), paused)
    );
  }
}
