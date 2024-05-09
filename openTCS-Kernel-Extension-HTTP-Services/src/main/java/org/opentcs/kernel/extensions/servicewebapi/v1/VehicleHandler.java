/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetVehicleResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostVehicleRoutesRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PutVehicleAllowedOrderTypesTO;

/**
 * Handles requests related to vehicles.
 */
public class VehicleHandler {

  private final VehicleService vehicleService;
  private final RouterService routerService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param vehicleService Used to update vehicle instances.
   * @param routerService Used to get information about potential routes.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public VehicleHandler(VehicleService vehicleService,
                        RouterService routerService,
                        KernelExecutorWrapper executorWrapper) {
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.routerService = requireNonNull(routerService, "routerService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  /**
   * Find all vehicles orders and filters depending on the given parameters.
   *
   * @param procStateName The filter parameter for the processing state of the vehicle.
   * The filtering is disabled for this parameter if the value is null.
   * @return A list of vehicles, that match the filter.
   * @throws IllegalArgumentException If procStateName could not be parsed.
   */
  public List<GetVehicleResponseTO> getVehiclesState(@Nullable String procStateName)
      throws IllegalArgumentException {
    return executorWrapper.callAndWait(() -> {
      Vehicle.ProcState pState = procStateName == null
          ? null
          : Vehicle.ProcState.valueOf(procStateName);

      return vehicleService.fetchObjects(Vehicle.class, Filters.vehicleWithProcState(pState))
          .stream()
          .map(GetVehicleResponseTO::fromVehicle)
          .sorted(Comparator.comparing(GetVehicleResponseTO::getName))
          .collect(Collectors.toList());
    });
  }

  /**
   * Finds the vehicle with the given name.
   *
   * @param name The name of the requested vehicle.
   * @return A single vehicle that has the given name.
   * @throws ObjectUnknownException If a vehicle with the given name does not exist.
   */
  public GetVehicleResponseTO getVehicleStateByName(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      return Optional.ofNullable(vehicleService.fetchObject(Vehicle.class, name))
          .map(GetVehicleResponseTO::fromVehicle)
          .orElseThrow(() -> new ObjectUnknownException("Unknown vehicle: " + name));
    });
  }

  public void putVehicleIntegrationLevel(String name, String value)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(),
                                                   Vehicle.IntegrationLevel.valueOf(value));
    });
  }

  public void putVehiclePaused(String name, String value)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      vehicleService.updateVehiclePaused(vehicle.getReference(), Boolean.parseBoolean(value));
    });
  }

  public void putVehicleEnvelopeKey(String name, String value)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      vehicleService.updateVehicleEnvelopeKey(vehicle.getReference(), value);
    });
  }

  public void putVehicleCommAdapterEnabled(String name, String value)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      if (Boolean.parseBoolean(value)) {
        vehicleService.enableCommAdapter(vehicle.getReference());
      }
      else {
        vehicleService.disableCommAdapter(vehicle.getReference());
      }
    });
  }

  public VehicleAttachmentInformation getVehicleCommAdapterAttachmentInformation(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      return vehicleService.fetchAttachmentInformation(vehicle.getReference());
    });
  }

  public void putVehicleCommAdapter(String name, String value)
      throws ObjectUnknownException {
    requireNonNull(name, "name");
    requireNonNull(value, "value");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      VehicleCommAdapterDescription newAdapter
          = vehicleService.fetchAttachmentInformation(vehicle.getReference())
              .getAvailableCommAdapters()
              .stream()
              .filter(description -> description.getClass().getName().equals(value))
              .findAny()
              .orElseThrow(
                  () -> new IllegalArgumentException("Unknown vehicle driver class name: " + value)
              );
      vehicleService.attachCommAdapter(vehicle.getReference(), newAdapter);
    });
  }

  public void putVehicleAllowedOrderTypes(String name,
                                          PutVehicleAllowedOrderTypesTO allowedOrderTypes)
      throws ObjectUnknownException {
    requireNonNull(name, "name");
    requireNonNull(allowedOrderTypes, "allowedOrderTypes");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }
      vehicleService.updateVehicleAllowedOrderTypes(
          vehicle.getReference(), new HashSet<>(allowedOrderTypes.getOrderTypes())
      );
    });
  }

  public Map<TCSObjectReference<Point>, Route> getVehicleRoutes(String name,
                                                                PostVehicleRoutesRequestTO request)
      throws ObjectUnknownException {
    requireNonNull(name, "name");
    requireNonNull(request, "request");

    return executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      TCSObjectReference<Point> sourcePointRef;
      if (request.getSourcePoint() == null) {
        if (vehicle.getCurrentPosition() == null) {
          throw new IllegalArgumentException("Unknown vehicle position: " + vehicle.getName());
        }
        sourcePointRef = vehicle.getCurrentPosition();
      }
      else {
        Point sourcePoint = vehicleService.fetchObject(Point.class, request.getSourcePoint());
        if (sourcePoint == null) {
          throw new ObjectUnknownException("Unknown source point: " + request.getSourcePoint());
        }
        sourcePointRef = sourcePoint.getReference();
      }

      Set<TCSObjectReference<Point>> destinationPointRefs = request.getDestinationPoints()
          .stream()
          .map(destPointName -> {
            Point destPoint = vehicleService.fetchObject(Point.class, destPointName);
            if (destPoint == null) {
              throw new ObjectUnknownException("Unknown destination point: " + destPointName);
            }
            return destPoint.getReference();
          })
          .collect(Collectors.toSet());

      Set<TCSResourceReference<?>> resourcesToAvoid = new HashSet<>();

      if (request.getResourcesToAvoid() != null) {
        for (String resourceName : request.getResourcesToAvoid()) {
          Point point = vehicleService.fetchObject(Point.class, resourceName);
          if (point != null) {
            resourcesToAvoid.add(point.getReference());
            continue;
          }

          Path path = vehicleService.fetchObject(Path.class, resourceName);
          if (path != null) {
            resourcesToAvoid.add(path.getReference());
            continue;
          }

          Location location = vehicleService.fetchObject(Location.class, resourceName);
          if (location != null) {
            resourcesToAvoid.add(location.getReference());
            continue;
          }

          throw new ObjectUnknownException("Unknown resource: " + resourceName);
        }
      }

      return routerService.computeRoutes(vehicle.getReference(),
                                         sourcePointRef,
                                         destinationPointRefs,
                                         resourcesToAvoid);
    });
  }
}
