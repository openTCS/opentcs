// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;

/**
 * Tests for {@link OrderSequenceEventTO}.
 */
class OrderSequenceEventTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    OrderSequenceEventTO to = new OrderSequenceEventTO(
        createOrderSequence(true),
        createOrderSequence(false)
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private OrderSequenceEventTO.OrderSequenceTO createOrderSequence(boolean finished) {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new OrderSequenceEventTO.OrderSequenceTO()
        .setName("order-sequence-1")
        .setProperties(new TreeMap<>(Map.of("key-1", "value-1", "key-2", "value-2")))
        .setHistory(
            new ObjectHistoryTO().setEntries(
                List.of(
                    new ObjectHistoryTO.ObjectHistoryEntryTO()
                        .setEventCode("event-code-1")
                        .setTimestamp(time)
                        .setSupplements(List.of("supplement-1", "supplement-2"))
                )
            )
        )
        .setType("type-1")
        .setOrderTypes(List.of("type-1"))
        .setOrders(List.of("order-1", "order-2"))
        .setFinishedIndex(4)
        .setComplete(true)
        .setFinished(finished)
        .setFailureFatal(true)
        .setIntendedVehicle("vehicle-1")
        .setProcessingVehicle("vehicle-2")
        .setCreationTime(time)
        .setFinishedTime(time.plus(1, ChronoUnit.HOURS));
  }
}
