// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralJobResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostPeripheralJobRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PeripheralJobConverter;

/**
 * Handles requests related to peripheral jobs.
 */
public class PeripheralJobHandler {

  private final InternalPeripheralJobService jobService;
  private final PeripheralDispatcherService jobDispatcherService;
  private final KernelExecutorWrapper executorWrapper;
  private final PeripheralJobConverter peripheralJobConverter;

  /**
   * Creates a new instance.
   *
   * @param jobService Used to create peripheral jobs.
   * @param jobDispatcherService Used to dispatch peripheral jobs.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public PeripheralJobHandler(
      InternalPeripheralJobService jobService,
      PeripheralDispatcherService jobDispatcherService,
      KernelExecutorWrapper executorWrapper,
      PeripheralJobConverter peripheralJobConverter
  ) {
    this.jobService = requireNonNull(jobService, "jobService");
    this.jobDispatcherService = requireNonNull(jobDispatcherService, "jobDispatcherService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
    this.peripheralJobConverter = requireNonNull(peripheralJobConverter, "peripheralJobConverter");
  }

  public PeripheralJob createPeripheralJob(String name, PostPeripheralJobRequestTO job) {
    requireNonNull(name, "name");
    requireNonNull(job, "job");

    return executorWrapper.callAndWait(() -> {
      // Check if the vehicle, location and transport order exist.
      if (job.getRelatedVehicle() != null
          && jobService.fetch(Vehicle.class, job.getRelatedVehicle()).isEmpty()) {
        throw new ObjectUnknownException("Unknown vehicle: " + job.getRelatedVehicle());
      }
      if (job.getRelatedTransportOrder() != null
          && jobService.fetch(TransportOrder.class, job.getRelatedTransportOrder()).isEmpty()) {
        throw new ObjectUnknownException(
            "Unknown transport order: " + job.getRelatedTransportOrder()
        );
      }
      if (job.getPeripheralOperation().getLocationName() != null
          && jobService.fetch(
              Location.class,
              job.getPeripheralOperation().getLocationName()
          ).isEmpty()) {
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
          operationCreationTO
      )
          .withIncompleteName(job.isIncompleteName());
      if (job.getProperties() != null) {
        jobCreationTO = jobCreationTO.withProperties(
            job.getProperties().stream()
                .collect(
                    Collectors.toMap(
                        property -> property.getKey(),
                        property -> property.getValue()
                    )
                )
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
      @Nullable
      String relatedVehicle,
      @Nullable
      String relatedTransportOrder
  ) {
    return executorWrapper.callAndWait(() -> {
      // If a related vehicle is set, make sure it exists.
      TCSObjectReference<Vehicle> relatedVehicleRef
          = Optional.ofNullable(relatedVehicle)
              .map(name -> jobService.fetch(Vehicle.class, name).orElse(null))
              .map(Vehicle::getReference)
              .orElse(null);

      if (relatedVehicle != null && relatedVehicleRef == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + relatedVehicle);
      }

      // If a related transport order is set, make sure it exists.
      TCSObjectReference<TransportOrder> relatedOrderRef
          = Optional.ofNullable(relatedTransportOrder)
              .map(name -> jobService.fetch(TransportOrder.class, name).orElse(null))
              .map(TransportOrder::getReference)
              .orElse(null);

      if (relatedTransportOrder != null && relatedOrderRef == null) {
        throw new ObjectUnknownException("Unknown oransport order: " + relatedVehicle);
      }

      return jobService.stream(PeripheralJob.class)
          .filter(
              Filters.peripheralJobWithRelatedVehicle(relatedVehicleRef)
                  .and(Filters.peripheralJobWithRelatedTransportOrder(relatedOrderRef))
          )
          .map(peripheralJob -> peripheralJobConverter.toGetPeripheralJobResponseTO(peripheralJob))
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
  public GetPeripheralJobResponseTO getPeripheralJobByName(
      @Nonnull
      String name
  ) {
    requireNonNull(name, "name");

    return executorWrapper.callAndWait(() -> {
      PeripheralJob job = jobService.fetch(PeripheralJob.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown peripheral job: " + name));

      return peripheralJobConverter.toGetPeripheralJobResponseTO(job);
    });
  }
}
