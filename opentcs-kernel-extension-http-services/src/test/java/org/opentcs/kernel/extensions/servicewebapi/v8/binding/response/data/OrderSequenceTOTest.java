// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

/**
 * Tests for @{link {@link OrderSequenceTO}}.
 */
public class OrderSequenceTOTest {
  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createOrderSequenceMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createOrderSequenceFull()));
  }

  private OrderSequenceTO createOrderSequenceMinimal() {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new OrderSequenceTO()
        .setName("order-sequence-1")
        .setProperties(Map.of())
        .setHistory(new ObjectHistoryTO().setEntries(List.of()))
        .setOrderTypes(List.of())
        .setOrders(List.of())
        .setFinishedIndex(4)
        .setComplete(true)
        .setFinished(true)
        .setFailureFatal(true)
        .setIntendedVehicle(null)
        .setProcessingVehicle(null)
        .setCreationTime(time)
        .setFinishedTime(null);
  }

  private OrderSequenceTO createOrderSequenceFull() {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new OrderSequenceTO()
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
        .setOrderTypes(List.of("type-1"))
        .setOrders(List.of("order-1", "order-2"))
        .setFinishedIndex(4)
        .setComplete(true)
        .setFinished(true)
        .setFailureFatal(true)
        .setIntendedVehicle("vehicle-1")
        .setProcessingVehicle("vehicle-2")
        .setCreationTime(time)
        .setFinishedTime(time.plus(1, ChronoUnit.HOURS));
  }
}
