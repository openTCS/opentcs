/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Unit tests for {@link PeripheralJobDispatcherHandler}.
 */
class PeripheralJobDispatcherHandlerTest {

  private PeripheralJobService jobService;
  private PeripheralDispatcherService jobDispatcherService;
  private KernelExecutorWrapper executorWrapper;

  private PeripheralJobDispatcherHandler handler;

  private Location location;
  private PeripheralJob job;

  @BeforeEach
  void setUp() {
    jobService = mock();
    jobDispatcherService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new PeripheralJobDispatcherHandler(jobService,
                                                 jobDispatcherService,
                                                 executorWrapper);

    location = new Location("some-location", new LocationType("some-location-type").getReference());
    job = new PeripheralJob(
        "some-job",
        "some-token",
        new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        )
    );

    given(jobService.fetchObject(Location.class, "some-location"))
        .willReturn(location);
    given(jobService.fetchObject(PeripheralJob.class, "some-job"))
        .willReturn(job);
  }

  @Test
  void withdrawPeripheralJobByLocation() {
    handler.withdrawPeripheralJobByLocation("some-location");
    then(jobDispatcherService).should().withdrawByLocation(location.getReference());
  }

  @Test
  void throwOnWithdrawPeripheralJobByUnknownLocation() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.withdrawPeripheralJobByLocation("some-unknown-location"));
  }

  @Test
  void withdrawPeripheralJob() {
    handler.withdrawPeripheralJob("some-job");
    then(jobDispatcherService).should().withdrawByPeripheralJob(job.getReference());
  }

  @Test
  void throwOnWithdrawUnknownPeripheralJob() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.withdrawPeripheralJob("some-unknown-job"));
  }
}
