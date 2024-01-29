/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.theInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.PeripheralJobService;
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
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * Unit tests for {@link PeripheralJobHandler}.
 */
class PeripheralJobHandlerTest {

  private PeripheralJobService jobService;
  private PeripheralDispatcherService jobDispatcherService;
  private KernelExecutorWrapper executorWrapper;

  private PeripheralJobHandler handler;

  @BeforeEach
  void setUp() {
    jobService = mock();
    jobDispatcherService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new PeripheralJobHandler(jobService,
                                       jobDispatcherService,
                                       executorWrapper);
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
            true)
    )
        .withRelatedVehicle(vehicle.getReference())
        .withRelatedTransportOrder(order.getReference())
        .withProperty("some-prop-key", "some-prop-value");
    given(jobService.fetchObject(Location.class, "some-location"))
        .willReturn(location);
    given(jobService.fetchObject(Vehicle.class, "some-vehicle"))
        .willReturn(vehicle);
    given(jobService.fetchObject(TransportOrder.class, "some-order"))
        .willReturn(order);
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
                    .setExecutionTrigger(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION)
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
    given(
        jobService.fetchObjects(ArgumentMatchers.<Class<PeripheralJob>>any(), any())
    )
        .willReturn(Set.of(job1, job2));

    // Act
    List<GetPeripheralJobResponseTO> result = handler.getPeripheralJobs(null, null);

    // Assert
    assertThat(result, hasSize(2));
    then(jobService).should().fetchObjects(ArgumentMatchers.<Class<PeripheralJob>>any(), any());
  }

  @Test
  void retrievePeripheralJobsFilteredByRelatedVehicle() {
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
    Vehicle vehicle = new Vehicle("some-vehicle");
    given(jobService.fetchObject(Vehicle.class, "some-vehicle"))
        .willReturn(vehicle);
    given(
        jobService.fetchObjects(ArgumentMatchers.<Class<PeripheralJob>>any(), any())
    )
        .willReturn(Set.of(job1, job2));

    // Act & Assert: happy path
    List<GetPeripheralJobResponseTO> result = handler.getPeripheralJobs("some-vehicle", null);

    assertThat(result, hasSize(2));
    then(jobService).should().fetchObjects(ArgumentMatchers.<Class<PeripheralJob>>any(), any());

    // Act & Assert: nonexistent vehicle
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getPeripheralJobs("some-unknown-vehicle", null));
  }

  @Test
  void retrievePeripheralJobsFilteredByRelatedTransportOrder() {
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
    TransportOrder transportOrder = new TransportOrder("some-order", List.of());
    given(jobService.fetchObject(TransportOrder.class, "some-order"))
        .willReturn(transportOrder);
    given(
        jobService.fetchObjects(ArgumentMatchers.<Class<PeripheralJob>>any(), any())
    )
        .willReturn(Set.of(job1, job2));

    // Act & Assert: happy path
    List<GetPeripheralJobResponseTO> result = handler.getPeripheralJobs(null, "some-order");

    assertThat(result, hasSize(2));
    then(jobService).should().fetchObjects(ArgumentMatchers.<Class<PeripheralJob>>any(), any());

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
    given(jobService.fetchObject(PeripheralJob.class, "some-job"))
        .willReturn(job);

    // Act & Assert: happy path
    GetPeripheralJobResponseTO result = handler.getPeripheralJobByName("some-job");
    assertThat(result, is(notNullValue()));
    then(jobService).should().fetchObject(PeripheralJob.class, "some-job");

    // Act & Assert: nonexistent order
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.getPeripheralJobByName("some-unknown-order"));
  }
}
