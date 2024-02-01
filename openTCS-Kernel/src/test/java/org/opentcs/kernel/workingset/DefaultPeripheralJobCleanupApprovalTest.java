/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;

/**
 * Test for {@link DefaultPeripheralJobCleanupApproval}.
 */
class DefaultPeripheralJobCleanupApprovalTest {

  private CreationTimeThreshold creationTimeThreshold;
  private DefaultPeripheralJobCleanupApproval approval;

  @BeforeEach
  void setUp() {
    creationTimeThreshold = mock();
    given(creationTimeThreshold.getCurrentThreshold())
        .willReturn(Instant.parse("2024-01-01T12:00:00.00Z"));

    approval = new DefaultPeripheralJobCleanupApproval(creationTimeThreshold);
  }

  @ParameterizedTest
  @EnumSource(value = PeripheralJob.State.class,
              names = {"FINISHED", "FAILED"})
  void approvePeripheralJob(PeripheralJob.State state) {
    PeripheralJob job = createPeripheralJob()
        .withState(state)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));

    assertTrue(approval.test(job));
  }

  @ParameterizedTest
  @EnumSource(value = PeripheralJob.State.class,
              mode = EnumSource.Mode.EXCLUDE,
              names = {"FINISHED", "FAILED"})
  void disapprovePeripheralJobNotInFinalState(PeripheralJob.State state) {
    PeripheralJob job = createPeripheralJob()
        .withState(state)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));

    assertFalse(approval.test(job));
  }

  @Test
  void disapprovePeripheralJobCreatedAfterCurrentThreshold() {
    PeripheralJob job = createPeripheralJob()
        .withState(PeripheralJob.State.FINISHED)
        .withCreationTime(Instant.parse("2024-01-01T15:00:00.00Z"));

    assertFalse(approval.test(job));
  }

  private PeripheralJob createPeripheralJob() {
    LocationType locationType = new LocationType("some-location-type");
    Location location = new Location("some-location", locationType.getReference());
    PeripheralOperation peripheralOperation
        = new PeripheralOperation(
            location.getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        );
    return new PeripheralJob("some-job-name", "some-token", peripheralOperation);
  }
}
