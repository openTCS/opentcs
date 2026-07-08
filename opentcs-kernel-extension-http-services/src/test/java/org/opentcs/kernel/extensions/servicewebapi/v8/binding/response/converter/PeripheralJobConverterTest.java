// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PeripheralJobTO.PeripheralOperationTO.ExecutionTriggerTO.AFTER_ALLOCATION;
import static org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PeripheralJobTO.PeripheralOperationTO.ExecutionTriggerTO.AFTER_MOVEMENT;
import static org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PeripheralJobTO.PeripheralOperationTO.ExecutionTriggerTO.IMMEDIATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.TimestampScrubber;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PeripheralJobTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

/**
 * Tests for {@link PeripheralJobConverter}.
 */
class PeripheralJobConverterTest {

  private JsonBinder jsonBinder;
  private PeripheralJobConverter converter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    converter = new PeripheralJobConverter();
  }

  @Test
  void convert() {
    PeripheralJob peripheralJob = new PeripheralJob(
        "job-1",
        "reservation-token-1",
        new PeripheralOperation(
            new Location(
                "location-1",
                new LocationType("location-type-1").getReference()
            ).getReference(),
            "operation-1",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    )
        .withProperties(Map.of("key-1", "value-1", "key-2", "value-2"))
        .withHistoryEntry(
            new ObjectHistory.Entry(Instant.EPOCH, "event-code-1", List.of("supplement-1"))
        )
        .withRelatedVehicle(new Vehicle("vehicle-1").getReference())
        .withRelatedTransportOrder(new TransportOrder("order-1", List.of()).getReference())
        .withState(PeripheralJob.State.FINISHED)
        .withCreationTime(Instant.EPOCH)
        .withFinishedTime(Instant.EPOCH);

    PeripheralJobTO result = converter.convert(peripheralJob);

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
    Instant time = Instant.parse("2025-07-23T12:03:00Z");
    PeripheralJob peripheralJob = new PeripheralJob(
        "job-1",
        "reservation-token-1",
        new PeripheralOperation(
            new Location(
                "location-1",
                new LocationType("location-type-1").getReference()
            ).getReference(),
            "operation-1",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    )
        .withCreationTime(time)
        .withFinishedTime(time.plus(1, ChronoUnit.HOURS))
        .withHistoryEntry(
            new ObjectHistory.Entry(
                time.plus(2, ChronoUnit.HOURS),
                "event-code-1",
                List.of("supplement-1")
            )
        );

    PeripheralJobTO result = converter.convert(peripheralJob);

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

  @ParameterizedTest
  @EnumSource(PeripheralJob.State.class)
  void convertsAllStates(PeripheralJob.State state) {
    PeripheralJob job = new PeripheralJob(
        "job-1",
        "reservation-token-1",
        new PeripheralOperation(
            new Location(
                "location-1",
                new LocationType("location-type-1").getReference()
            ).getReference(),
            "operation-1",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    )
        .withState(state);

    PeripheralJobTO result = converter.convert(job);

    PeripheralJobTO.StateTO expectedState = switch (state) {
      case TO_BE_PROCESSED -> PeripheralJobTO.StateTO.TO_BE_PROCESSED;
      case BEING_PROCESSED -> PeripheralJobTO.StateTO.BEING_PROCESSED;
      case FINISHED -> PeripheralJobTO.StateTO.FINISHED;
      case FAILED -> PeripheralJobTO.StateTO.FAILED;
    };
    assertThat(result.getState()).isEqualTo(expectedState);
  }

  @ParameterizedTest
  @EnumSource(PeripheralOperation.ExecutionTrigger.class)
  void convertsAllExecutionTriggers(PeripheralOperation.ExecutionTrigger executionTrigger) {
    PeripheralJob job = new PeripheralJob(
        "job-1",
        "reservation-token-1",
        new PeripheralOperation(
            new Location(
                "location-1",
                new LocationType("location-type-1").getReference()
            ).getReference(),
            "operation-1",
            executionTrigger,
            true
        )
    );

    PeripheralJobTO result = converter.convert(job);

    PeripheralJobTO.PeripheralOperationTO.ExecutionTriggerTO expectedExecutionTrigger
        = switch (executionTrigger) {
          case IMMEDIATE -> IMMEDIATE;
          case AFTER_ALLOCATION -> AFTER_ALLOCATION;
          case AFTER_MOVEMENT -> AFTER_MOVEMENT;
        };
    assertThat(result.getPeripheralOperation().getExecutionTrigger())
        .isEqualTo(expectedExecutionTrigger);
  }

  @Test
  void convertsInstantMaxToNull() {
    PeripheralJob job = new PeripheralJob(
        "job-1",
        "reservation-token-1",
        new PeripheralOperation(
            new Location(
                "location-1",
                new LocationType("location-type-1").getReference()
            ).getReference(),
            "operation-1",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    )
        .withFinishedTime(Instant.MAX);

    PeripheralJobTO result = converter.convert(job);

    assertThat(result.getFinishedTime()).isNull();
  }
}
