// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PeripheralJobTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

/**
 * Tests for {@link PeripheralJobEventTO}.
 */
class PeripheralJobEventTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    PeripheralJobEventTO to = new PeripheralJobEventTO(
        createPeripheralJob(PeripheralJobTO.StateTO.FINISHED),
        createPeripheralJob(PeripheralJobTO.StateTO.BEING_PROCESSED)
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private PeripheralJobTO createPeripheralJob(PeripheralJobTO.StateTO state) {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new PeripheralJobTO()
        .setName("job-1")
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
        .setReservationToken("reservation-token-1")
        .setRelatedVehicle("vehicle-1")
        .setRelatedTransportOrder("transport-order-1")
        .setPeripheralOperation(
            new PeripheralJobTO.PeripheralOperationTO()
                .setLocation("location-1")
                .setOperation("operation-1")
                .setExecutionTrigger(
                    PeripheralJobTO.PeripheralOperationTO.ExecutionTriggerTO.AFTER_MOVEMENT
                )
                .setCompletionRequired(true)
        )
        .setState(state)
        .setCreationTime(time)
        .setFinishedTime(time.plus(1, ChronoUnit.HOURS));
  }
}
