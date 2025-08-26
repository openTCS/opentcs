// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.workingset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for {@link DefaultOrderSequenceCleanupApproval}.
 */
class DefaultOrderSequenceCleanupApprovalTest {

  private TransportOrderPoolManager orderPoolManager;
  private TCSObjectRepository objectRepo;
  private DefaultTransportOrderCleanupApproval defaultTransportOrderCleanupApproval;
  private CreationTimeThreshold creationTimeThreshold;
  private DefaultOrderSequenceCleanupApproval approval;

  @BeforeEach
  void setUp() {
    orderPoolManager = mock();
    objectRepo = mock();
    defaultTransportOrderCleanupApproval = mock();
    creationTimeThreshold = mock();
    given(creationTimeThreshold.getCurrentThreshold())
        .willReturn(Instant.parse("2024-01-01T12:00:00.00Z"));
    given(orderPoolManager.getObjectRepo()).willReturn(objectRepo);

    approval = new DefaultOrderSequenceCleanupApproval(
        orderPoolManager,
        defaultTransportOrderCleanupApproval,
        creationTimeThreshold
    );
  }

  @Test
  void approveOrderSequence() {
    OrderSequence sequence = createOrderSequence()
        .withFinished(true)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));

    assertTrue(approval.test(sequence));
  }

  @Test
  void disapproveOrderSequenceNotFinished() {
    OrderSequence sequence = createOrderSequence()
        .withFinished(false)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));

    assertFalse(approval.test(sequence));
  }

  @Test
  void disapproveOrderSequenceWithRelatedTransportOrderCreatedAfterCreationTimeThreshold() {
    TransportOrder order = createTransportOrder()
        .withState(TransportOrder.State.FINISHED)
        .withCreationTime(Instant.parse("2024-01-01T15:00:00.00Z"));
    OrderSequence sequence = createOrderSequence()
        .withOrder(order.getReference())
        .withFinished(true);
    given(objectRepo.getObject(TransportOrder.class, order.getReference())).willReturn(order);

    assertFalse(approval.test(sequence));
  }

  @Test
  void disapproveOrderSequenceCreatedAfterCurrentThreshold() {
    OrderSequence sequence = createOrderSequence()
        .withFinished(true)
        .withCreationTime(Instant.parse("2024-01-01T15:00:00.00Z"));

    assertFalse(approval.test(sequence));
  }

  @Test
  void disapproveOrderSequenceWithUnapprovedTransportOrder() {
    TransportOrder order = createTransportOrder()
        .withState(TransportOrder.State.FINISHED)
        .withCreationTime(Instant.parse("2024-01-01T09:00:00.00Z"));
    OrderSequence sequence = createOrderSequence()
        .withOrder(order.getReference())
        .withFinished(true);
    given(objectRepo.getObjects(eq(TransportOrder.class), any())).willReturn(Set.of(order));
    given(defaultTransportOrderCleanupApproval.test(order)).willReturn(false);

    assertFalse(approval.test(sequence));
  }

  private OrderSequence createOrderSequence() {
    return new OrderSequence("some-sequence");
  }

  private TransportOrder createTransportOrder() {
    return new TransportOrder("some-order", List.of());
  }
}
