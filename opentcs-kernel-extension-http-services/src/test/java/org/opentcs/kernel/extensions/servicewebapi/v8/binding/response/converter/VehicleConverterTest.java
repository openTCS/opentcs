// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

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
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.kernel.extensions.servicewebapi.TimestampScrubber;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

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
                    createPath("path-1").getReference()
                ),
                Set.of(
                    new Point("point-2").getReference(),
                    createPath("path-2").getReference(),
                    createLocation("location-2").getReference()
                )
            )
        )
        .withAllocatedResources(
            List.of(
                Set.of(
                    new Point("point-3").getReference(),
                    createPath("path-3").getReference()
                ),
                Set.of(
                    new Point("point-4").getReference(),
                    createPath("path-4").getReference(),
                    createLocation("location-4").getReference()
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

    VehicleTO result = converter.convert(vehicle);

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

    VehicleTO result = converter.convert(vehicle);

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

    VehicleTO result = converter.convert(vehicle);

    VehicleTO.StateTO expectedState = switch (state) {
      case UNKNOWN -> VehicleTO.StateTO.UNKNOWN;
      case UNAVAILABLE -> VehicleTO.StateTO.UNAVAILABLE;
      case ERROR -> VehicleTO.StateTO.ERROR;
      case IDLE -> VehicleTO.StateTO.IDLE;
      case EXECUTING -> VehicleTO.StateTO.EXECUTING;
      case CHARGING -> VehicleTO.StateTO.CHARGING;
    };
    assertThat(result.getState().getState()).isEqualTo(expectedState);
  }

  @ParameterizedTest
  @EnumSource(Vehicle.ProcState.class)
  void convertsAllProcStates(Vehicle.ProcState procState) {
    Vehicle vehicle = new Vehicle("vehicle-1").withProcState(procState);

    VehicleTO result = converter.convert(vehicle);

    VehicleTO.ProcStateTO expectedProcState = switch (procState) {
      case IDLE -> VehicleTO.ProcStateTO.IDLE;
      case AWAITING_ORDER -> VehicleTO.ProcStateTO.AWAITING_ORDER;
      case PROCESSING_ORDER -> VehicleTO.ProcStateTO.PROCESSING_ORDER;
    };
    assertThat(result.getProcState().getProcState()).isEqualTo(expectedProcState);
  }

  @ParameterizedTest
  @EnumSource(Vehicle.IntegrationLevel.class)
  void convertsAllIntegrationLevels(Vehicle.IntegrationLevel integrationLevel) {
    Vehicle vehicle = new Vehicle("vehicle-1").withIntegrationLevel(integrationLevel);

    VehicleTO result = converter.convert(vehicle);

    VehicleTO.IntegrationLevelTO expectedIntegrationLevel = switch (integrationLevel) {
      case TO_BE_IGNORED -> VehicleTO.IntegrationLevelTO.TO_BE_IGNORED;
      case TO_BE_NOTICED -> VehicleTO.IntegrationLevelTO.TO_BE_NOTICED;
      case TO_BE_RESPECTED -> VehicleTO.IntegrationLevelTO.TO_BE_RESPECTED;
      case TO_BE_UTILIZED -> VehicleTO.IntegrationLevelTO.TO_BE_UTILIZED;
    };
    assertThat(result.getIntegrationLevel()).isEqualTo(expectedIntegrationLevel);
  }

  @Test
  void convertsNullMembersToNull() {
    Vehicle vehicle = new Vehicle("vehicle-1")
        .withTransportOrder(null)
        .withOrderSequence(null)
        .withCurrentPosition(null);

    VehicleTO result = converter.convert(vehicle);

    assertThat(result.getTransportOrder()).isNull();
    assertThat(result.getOrderSequence()).isNull();
    assertThat(result.getCurrentPosition()).isNull();
  }

  @Test
  void convertsOrientationAngleNaNToNull() {
    Vehicle vehicle = new Vehicle("vehicle-1").withPose(new Pose(null, Double.NaN));

    VehicleTO result = converter.convert(vehicle);

    assertThat(result.getPose().getOrientationAngle()).isNull();
  }

  private Path createPath(String name) {
    return new Path(
        name,
        new Point("dummy-source").getReference(),
        new Point("dummy-destination").getReference()
    );
  }

  private Location createLocation(String name) {
    return new Location(
        name,
        new LocationType("dummy-type").getReference()
    );
  }
}
