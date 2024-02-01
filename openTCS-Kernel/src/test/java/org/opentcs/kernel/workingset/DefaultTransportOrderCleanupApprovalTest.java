/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;

/**
 * Test for {@link DefaultTransportOrderCleanupApproval}.
 */
class DefaultTransportOrderCleanupApprovalTest {

  private PeripheralJobPoolManager peripheralJobPoolManager;
  private TCSObjectRepository objectRepo;
  private DefaultPeripheralJobCleanupApproval defaultPeripheralJobCleanupApproval;
  private CreationTimeThreshold creationTimeThreshold;
  private DefaultTransportOrderCleanupApproval approval;

  @BeforeEach
  void setUp() {
    peripheralJobPoolManager = mock();
    objectRepo = mock();
    defaultPeripheralJobCleanupApproval = mock();
    creationTimeThreshold = mock();
    given(peripheralJobPoolManager.getObjectRepo()).willReturn(objectRepo);
    given(creationTimeThreshold.getCurrentThreshold())
        .willReturn(Instant.parse("2024-01-01T12:00:00.00Z"));

    approval = new DefaultTransportOrderCleanupApproval(peripheralJobPoolManager,
                                                        defaultPeripheralJobCleanupApproval,
                                                        creationTimeThreshold);
  }

  @ParameterizedTest
  @EnumSource(value = TransportOrder.State.class,
              names = {"FINISHED", "FAILED", "UNROUTABLE"})
  void approveTransportOrder(TransportOrder.State state) {
    TransportOrder order = createTransportOrder()
        .withState(state)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));

    assertTrue(approval.test(order));
  }

  @ParameterizedTest
  @EnumSource(value = TransportOrder.State.class,
              mode = EnumSource.Mode.EXCLUDE,
              names = {"FINISHED", "FAILED", "UNROUTABLE"})
  void disapproveTransportOrderInNonFinalState(TransportOrder.State state) {
    TransportOrder order = createTransportOrder()
        .withState(state)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));

    assertFalse(approval.test(order));
  }

  @Test
  void disapproveTransportOrderRelatedToJobWithNonFinalState() {
    TransportOrder order = createTransportOrder()
        .withState(TransportOrder.State.FINISHED)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));
    PeripheralJob job = createPeripheralJob()
        .withState(PeripheralJob.State.BEING_PROCESSED)
        .withRelatedTransportOrder(order.getReference());
    given(objectRepo.getObjects(eq(PeripheralJob.class), any())).willReturn(Set.of(job));

    assertFalse(approval.test(order));
  }

  @Test
  void disapproveTransportOrderRelatedToUnapprovedJob() {
    TransportOrder order = createTransportOrder()
        .withState(TransportOrder.State.FINISHED)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));
    PeripheralJob job = createPeripheralJob()
        .withState(PeripheralJob.State.FAILED)
        .withRelatedTransportOrder(order.getReference());
    given(objectRepo.getObjects(eq(PeripheralJob.class), any())).willReturn(Set.of(job));
    given(defaultPeripheralJobCleanupApproval.test(job)).willReturn(false);

    assertFalse(approval.test(order));
  }

  @Test
  void disapproveTransportOrderCreatedAfterCreationTime() {
    TransportOrder order = createTransportOrder()
        .withState(TransportOrder.State.FINISHED)
        .withCreationTime(Instant.parse("2024-01-01T15:00:00.00Z"));

    assertFalse(approval.test(order));
  }

  private TransportOrder createTransportOrder() {
    return new TransportOrder("some-order", List.of());
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
