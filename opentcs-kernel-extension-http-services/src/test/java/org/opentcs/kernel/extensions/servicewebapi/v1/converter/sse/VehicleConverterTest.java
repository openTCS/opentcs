// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.TimestampScrubber;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.VehicleEventTO;

/**
 * Tests for {@link VehicleConverter}.
 */
class VehicleConverterTest {

  private JsonBinder jsonBinder;
  private VehicleConverter converter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    converter = new VehicleConverter();
  }

  @Test
  void convert() {
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withProperties(Map.of("key-1", "value-1", "key-2", "value-2"))
        .withHistoryEntry(
            new ObjectHistory.Entry(Instant.EPOCH, "event-code-1", List.of("supplement-1"))
        )
        .withBoundingBox(
            new BoundingBox(1, 2, 3)
                .withReferenceOffset(new Couple(4, 5))
        )
        .withEnergyLevelThresholdSet(new Vehicle.EnergyLevelThresholdSet(1, 2, 3, 4))
        .withMaxVelocity(200)
        .withMaxReverseVelocity(100)
        .withRechargeOperation("recharge-operation")
        .withProcState(Vehicle.ProcState.IDLE)
        .withTransportOrder(new TransportOrder("transport-order-1", List.of()).getReference())
        .withOrderSequence(new OrderSequence("order-sequence-1").getReference())
        .withAcceptableOrderTypes(
            Set.of(
                new AcceptableOrderType("acceptable-order-type-1", 2),
                new AcceptableOrderType("acceptable-order-type-2", 5)
            )
        )
        .withClaimedResources(
            List.of(
                Set.of(
                    new Point("point-1").getReference(),
                    new Point("point-2").getReference()
                ),
                Set.of(
                    new Point("point-3").getReference(),
                    new Point("point-4").getReference()
                )
            )
        )
        .withAllocatedResources(
            List.of(
                Set.of(
                    new Point("point-5").getReference(),
                    new Point("point-6").getReference()
                ),
                Set.of(
                    new Point("point-7").getReference(),
                    new Point("point-8").getReference()
                )
            )
        )
        .withState(Vehicle.State.IDLE)
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withPaused(true)
        .withCurrentPosition(new Point("point-5").getReference())
        .withPose(new Pose(new Triple(1, 2, 3), 4.5))
        .withEnergyLevel(42)
        .withLoadHandlingDevices(List.of(new LoadHandlingDevice("lhd-1", true)))
        .withEnvelopeKey("envelope-key-1")
        .withLayout(
            new Vehicle.Layout()
                .withRouteColor(new Color(20, 40, 60))
        );

    VehicleEventTO.VehicleTO result = converter.convert(vehicle);

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
    Instant time = Instant.parse("2025-07-25T12:03:00Z");
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withHistoryEntry(
            new ObjectHistory.Entry(time, "event-code-1", List.of("supplement-1"))
        );

    VehicleEventTO.VehicleTO result = converter.convert(vehicle);

    assertThat(result.getHistory().getEntries())
        .extracting(
            ObjectHistoryTO.ObjectHistoryEntryTO::getTimestamp,
            ObjectHistoryTO.ObjectHistoryEntryTO::getEventCode,
            ObjectHistoryTO.ObjectHistoryEntryTO::getSupplements
        )
        .contains(Tuple.tuple(time, "event-code-1", List.of("supplement-1")));
    assertThat(result.getState().getTimestamp()).isEqualTo(vehicle.getStateTimestamp());
    assertThat(result.getProcState().getTimestamp()).isEqualTo(vehicle.getProcStateTimestamp());
  }

  @ParameterizedTest
  @EnumSource(Vehicle.State.class)
  void convertsAllStates(Vehicle.State state) {
    Vehicle vehicle = new Vehicle("vehicle-1").withState(state);

    VehicleEventTO.VehicleTO result = converter.convert(vehicle);

    VehicleEventTO.StateTO expectedState = switch (state) {
      case UNKNOWN -> VehicleEventTO.StateTO.UNKNOWN;
      case UNAVAILABLE -> VehicleEventTO.StateTO.UNAVAILABLE;
      case ERROR -> VehicleEventTO.StateTO.ERROR;
      case IDLE -> VehicleEventTO.StateTO.IDLE;
      case EXECUTING -> VehicleEventTO.StateTO.EXECUTING;
      case CHARGING -> VehicleEventTO.StateTO.CHARGING;
    };
    assertThat(result.getState().getState()).isEqualTo(expectedState);
  }

  @ParameterizedTest
  @EnumSource(Vehicle.ProcState.class)
  void convertsAllProcStates(Vehicle.ProcState procState) {
    Vehicle vehicle = new Vehicle("vehicle-1").withProcState(procState);

    VehicleEventTO.VehicleTO result = converter.convert(vehicle);

    VehicleEventTO.ProcStateTO expectedProcState = switch (procState) {
      case IDLE -> VehicleEventTO.ProcStateTO.IDLE;
      case AWAITING_ORDER -> VehicleEventTO.ProcStateTO.AWAITING_ORDER;
      case PROCESSING_ORDER -> VehicleEventTO.ProcStateTO.PROCESSING_ORDER;
    };
    assertThat(result.getProcState().getProcState()).isEqualTo(expectedProcState);
  }

  @ParameterizedTest
  @EnumSource(Vehicle.IntegrationLevel.class)
  void convertsAllIntegrationLevels(Vehicle.IntegrationLevel integrationLevel) {
    Vehicle vehicle = new Vehicle("vehicle-1").withIntegrationLevel(integrationLevel);

    VehicleEventTO.VehicleTO result = converter.convert(vehicle);

    VehicleEventTO.IntegrationLevelTO expectedIntegrationLevel = switch (integrationLevel) {
      case TO_BE_IGNORED -> VehicleEventTO.IntegrationLevelTO.TO_BE_IGNORED;
      case TO_BE_NOTICED -> VehicleEventTO.IntegrationLevelTO.TO_BE_NOTICED;
      case TO_BE_RESPECTED -> VehicleEventTO.IntegrationLevelTO.TO_BE_RESPECTED;
      case TO_BE_UTILIZED -> VehicleEventTO.IntegrationLevelTO.TO_BE_UTILIZED;
    };
    assertThat(result.getIntegrationLevel()).isEqualTo(expectedIntegrationLevel);
  }
}
