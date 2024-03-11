/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.Comparator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralJobResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostPeripheralJobRequestTO;

/**
 * Handles requests related to peripheral jobs.
 */
public class PeripheralJobHandler {

  private final PeripheralJobService jobService;
  private final PeripheralDispatcherService jobDispatcherService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param jobService Used to create peripheral jobs.
   * @param jobDispatcherService Used to dispatch peripheral jobs.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public PeripheralJobHandler(PeripheralJobService jobService,
                              PeripheralDispatcherService jobDispatcherService,
                              KernelExecutorWrapper executorWrapper) {
    this.jobService = requireNonNull(jobService, "jobService");
    this.jobDispatcherService = requireNonNull(jobDispatcherService, "jobDispatcherService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  public PeripheralJob createPeripheralJob(String name, PostPeripheralJobRequestTO job) {
    requireNonNull(name, "name");
    requireNonNull(job, "job");

    return executorWrapper.callAndWait(() -> {
      // Check if the vehicle, location and transport order exist.
      if (job.getRelatedVehicle() != null
          && jobService.fetchObject(Vehicle.class, job.getRelatedVehicle()) == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + job.getRelatedVehicle());
      }
      if (job.getRelatedTransportOrder() != null
          && jobService.fetchObject(TransportOrder.class,
                                    job.getRelatedTransportOrder()) == null) {
        throw new ObjectUnknownException(
            "Unknown transport order: " + job.getRelatedTransportOrder()
        );
      }
      if (job.getPeripheralOperation().getLocationName() != null
          && jobService.fetchObject(Location.class,
                                    job.getPeripheralOperation().getLocationName()) == null) {
        throw new ObjectUnknownException(
            "Unknown location: " + job.getPeripheralOperation().getLocationName()
        );
      }

      // Peripheral jobs created via the web API are expected to be executed immediately and
      // require no completion. Therefore, explicitly ignore the corresponding provided values.
      PeripheralOperationCreationTO operationCreationTO = new PeripheralOperationCreationTO(
          job.getPeripheralOperation().getOperation(),
          job.getPeripheralOperation().getLocationName()
      );

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

      return jobService.createPeripheralJob(jobCreationTO);
    });
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
  public List<GetPeripheralJobResponseTO> getPeripheralJobs(
      @Nullable String relatedVehicle,
      @Nullable String relatedTransportOrder
  ) {
    return executorWrapper.callAndWait(() -> {
      // If a related vehicle is set, make sure it exists.
      TCSObjectReference<Vehicle> relatedVehicleRef
          = Optional.ofNullable(relatedVehicle)
              .map(name -> jobService.fetchObject(Vehicle.class, name))
              .map(Vehicle::getReference)
              .orElse(null);

      if (relatedVehicle != null && relatedVehicleRef == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + relatedVehicle);
      }

      // If a related transport order is set, make sure it exists.
      TCSObjectReference<TransportOrder> relatedOrderRef
          = Optional.ofNullable(relatedTransportOrder)
              .map(name -> jobService.fetchObject(TransportOrder.class, name))
              .map(TransportOrder::getReference)
              .orElse(null);

      if (relatedTransportOrder != null && relatedOrderRef == null) {
        throw new ObjectUnknownException("Unknown oransport order: " + relatedVehicle);
      }

      return jobService.fetchObjects(
          PeripheralJob.class,
          Filters.peripheralJobWithRelatedVehicle(relatedVehicleRef)
              .and(Filters.peripheralJobWithRelatedTransportOrder(relatedOrderRef))
      )
          .stream()
          .map(GetPeripheralJobResponseTO::fromPeripheralJob)
          .sorted(Comparator.comparing(GetPeripheralJobResponseTO::getName))
          .collect(Collectors.toList());
    });
  }

  /**
   * Find a peripheral job by name.
   *
   * @param name The name of the peripheral job.
   * @return The peripheral job state.
   */
  public GetPeripheralJobResponseTO getPeripheralJobByName(@Nonnull String name) {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      PeripheralJob job = jobService.fetchObject(PeripheralJob.class, name);
      if (job == null) {
        throw new ObjectUnknownException("Unknown peripheral job: " + name);
      }

      return GetPeripheralJobResponseTO.fromPeripheralJob(job);
    });
  }
}
