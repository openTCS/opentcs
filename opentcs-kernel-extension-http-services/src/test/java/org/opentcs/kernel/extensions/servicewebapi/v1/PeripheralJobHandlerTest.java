// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.theInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetPeripheralJobResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PostPeripheralJobRequestTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PeripheralJobConverter;
import org.opentcs.kernel.extensions.servicewebapi.v1.converter.PeripheralOperationConverter;

/**
 * Unit tests for {@link PeripheralJobHandler}.
 */
class PeripheralJobHandlerTest {

  private InternalPeripheralJobService jobService;
  private PeripheralDispatcherService jobDispatcherService;
  private KernelExecutorWrapper executorWrapper;
  private PeripheralJobConverter peripheralJobConverter;

  private PeripheralJobHandler handler;

  @BeforeEach
  void setUp() {
    jobService = mock();
    jobDispatcherService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());
    peripheralJobConverter = new PeripheralJobConverter(new PeripheralOperationConverter());

    handler = new PeripheralJobHandler(
        jobService,
        jobDispatcherService,
        executorWrapper,
        peripheralJobConverter
    );
  }

  @Test
  void createPeripheralJob() {
    // Arrange
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    Vehicle vehicle = new Vehicle("some-vehicle");
    TransportOrder order = new TransportOrder("some-order", List.of());
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    )
        .withRelatedVehicle(vehicle.getReference())
        .withRelatedTransportOrder(order.getReference())
        .withProperty("some-prop-key", "some-prop-value");
    given(jobService.fetch(Location.class, "some-location"))
        .willReturn(Optional.of(location));
    given(jobService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicle));
    given(jobService.fetch(TransportOrder.class, "some-order"))
        .willReturn(Optional.of(order));
    given(jobService.createPeripheralJob(any(PeripheralJobCreationTO.class)))
        .willReturn(job);

    // Act
    PeripheralJob result = handler.createPeripheralJob(
        "some-job",
        new PostPeripheralJobRequestTO()
            .setIncompleteName(false)
            .setReservationToken("some-token")
            .setPeripheralOperation(
                new PeripheralOperationDescription()
                    .setLocationName("some-location")
                    .setOperation("some-operation")
                    .setExecutionTrigger(PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION)
                    .setCompletionRequired(true)
            )
            .setRelatedVehicle("some-vehicle")
            .setRelatedTransportOrder("some-order")
            .setProperties(List.of(new Property("some-prop-key", "some-prop-value")))
    );

    // Assert
    assertThat(result, is(theInstance(job)));
    then(jobService).should().createPeripheralJob(any(PeripheralJobCreationTO.class));
  }

  @Test
  void retrievePeripheralJobsUnfiltered() {
    // Arrange
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    PeripheralJob job1 = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );
    PeripheralJob job2 = new PeripheralJob(
        "some-job-2",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );
    given(jobService.stream(PeripheralJob.class))
        .willReturn(Stream.of(job1, job2));

    // Act
    List<GetPeripheralJobResponseTO> result = handler.getPeripheralJobs(null, null);

    // Assert
    assertThat(result, hasSize(2));
    then(jobService).should().stream(ArgumentMatchers.<Class<PeripheralJob>>any());
  }

  @Test
  void retrievePeripheralJobsFilteredByRelatedVehicle() {
    // Arrange
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    Vehicle vehicle = new Vehicle("some-vehicle");
    PeripheralJob job1 = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    )
        .withRelatedVehicle(vehicle.getReference());

    PeripheralJob job2 = new PeripheralJob(
        "some-job-2",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    )
        .withRelatedVehicle(vehicle.getReference());

    given(jobService.fetch(Vehicle.class, "some-vehicle"))
        .willReturn(Optional.of(vehicle));
    given(jobService.stream(PeripheralJob.class))
        .willReturn(Stream.of(job1, job2));

    // Act & Assert: happy path
    List<GetPeripheralJobResponseTO> result = handler.getPeripheralJobs("some-vehicle", null);

    assertThat(result, hasSize(2));
    then(jobService).should().stream(PeripheralJob.class);

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getPeripheralJobs("some-unknown-vehicle", null));
  }

  @Test
  void retrievePeripheralJobsFilteredByRelatedTransportOrder() {
    // Arrange
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    TransportOrder transportOrder = new TransportOrder("some-order", List.of());
    PeripheralJob job1 = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    )
        .withRelatedTransportOrder(transportOrder.getReference());

    PeripheralJob job2 = new PeripheralJob(
        "some-job-2",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    )
        .withRelatedTransportOrder(transportOrder.getReference());

    given(jobService.fetch(TransportOrder.class, "some-order"))
        .willReturn(Optional.of(transportOrder));
    given(jobService.stream(PeripheralJob.class))
        .willReturn(Stream.of(job1, job2));

    // Act & Assert: happy path
    List<GetPeripheralJobResponseTO> result = handler.getPeripheralJobs(null, "some-order");

    assertThat(result, hasSize(2));
    then(jobService).should().stream(PeripheralJob.class);

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getPeripheralJobs(null, "some-unknown-order"));
  }

  @Test
  void retrievPeripheralJobByName() {
    // Arrange
    Location location
        = new Location("some-location", new LocationType("some-location-type").getReference());
    PeripheralJob job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );
    given(jobService.fetch(PeripheralJob.class, "some-job"))
        .willReturn(Optional.of(job));

    // Act & Assert: happy path
    GetPeripheralJobResponseTO result = handler.getPeripheralJobByName("some-job");
    assertThat(result, is(notNullValue()));
    then(jobService).should().fetch(PeripheralJob.class, "some-job");

    // Act & Assert: nonexistent order
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getPeripheralJobByName("some-unknown-order"));
  }
}
