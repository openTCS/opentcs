// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.TimestampScrubber;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.OrderSequenceEventTO;

/**
 * Tests for {@link OrderSequenceConverter}.
 */
class OrderSequenceConverterTest {

  private JsonBinder jsonBinder;
  private OrderSequenceConverter converter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    converter = new OrderSequenceConverter();
  }

  @Test
  void convert() {
    OrderSequence orderSequence = new OrderSequence("sequence-1")
        .withProperties(new TreeMap<>(Map.of("key-1", "value-1", "key-2", "value-2")))
        .withHistoryEntry(
            new ObjectHistory.Entry(Instant.EPOCH, "event-code-1", List.of("supplement-1"))
        )
        .withOrderTypes(Set.of("some-type"))
        .withOrder(new TransportOrder("order-1", List.of()).getReference())
        .withOrder(new TransportOrder("order-2", List.of()).getReference())
        .withFinishedIndex(1)
        .withComplete(true)
        .withFinished(true)
        .withFailureFatal(true)
        .withIntendedVehicle(new Vehicle("vehicle-1").getReference())
        .withProcessingVehicle(new Vehicle("vehicle-2").getReference())
        .withCreationTime(Instant.EPOCH)
        .withFinishedTime(Instant.EPOCH);

    OrderSequenceEventTO.OrderSequenceTO result = converter.convert(orderSequence);

    Approvals.verify(
        jsonBinder.toJson(result),
        // Scrub timestamps in the result's JSON representation as some of them may be generated
        // during test runtime, making it impossible to use ApprovalTests.
        new Options(new TimestampScrubber())
    );
  }

  /**
   * Ensures that timestamps are converted correctly. (Testing this is necessary, since timestamps
   * are scrubbed in the ApprovalTest and therefore their conversion is not covered there.)
   */
  @Test
  void convertsTimestampsCorrectly() {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    OrderSequence orderSequence = new OrderSequence("sequence-1")
        .withHistoryEntry(
            new ObjectHistory.Entry(
                time.plus(2, ChronoUnit.HOURS),
                "event-code-1",
                List.of("supplement-1")
            )
        )
        .withCreationTime(time)
        .withFinishedTime(time.plus(1, ChronoUnit.HOURS));

    OrderSequenceEventTO.OrderSequenceTO result = converter.convert(orderSequence);

    assertThat(result.getCreationTime()).isEqualTo(time);
    assertThat(result.getFinishedTime()).isEqualTo(time.plus(1, ChronoUnit.HOURS));
    assertThat(result.getHistory().getEntries())
        .extracting(
            ObjectHistoryTO.ObjectHistoryEntryTO::getTimestamp,
            ObjectHistoryTO.ObjectHistoryEntryTO::getEventCode,
            ObjectHistoryTO.ObjectHistoryEntryTO::getSupplements
        )
        .contains(
            Tuple.tuple(
                time.plus(2, ChronoUnit.HOURS),
                "event-code-1",
                List.of("supplement-1")
            )
        );
  }
}
