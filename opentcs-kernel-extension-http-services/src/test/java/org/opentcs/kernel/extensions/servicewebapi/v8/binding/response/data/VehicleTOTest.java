// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ColorTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.PoseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ResourceTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.TripleTO;

/**
 * Tests for {@link VehicleTO}.
 */
class VehicleTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createVehicleMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createVehicleFull()));
  }

  private VehicleTO createVehicleMinimal() {
    return new VehicleTO()
        .setName("some-vehicle")
        .setProperties(Map.of())
        .setHistory(new ObjectHistoryTO().setEntries(List.of()))
        .setBoundingBox(
            new BoundingBoxTO()
                .setLength(50)
                .setWidth(40)
                .setHeight(30)
                .setReferenceOffset(
                    new CoupleTO()
                        .setX(20)
                        .setY(10)
                )
        )
        .setEnergyLevelThresholdSet(
            new VehicleTO.EnergyLevelThresholdSetTO()
                .setEnergyLevelGood(90)
                .setEnergyLevelCritical(30)
                .setEnergyLevelSufficientlyRecharged(30)
                .setEnergyLevelFullyRecharged(90)
        )
        .setEnergyLevel(48)
        .setMaxVelocity(10)
        .setMaxReverseVelocity(20)
        .setRechargeOperation("some-recharge-operation")
        .setLoadHandlingDevices(List.of())
        .setState(
            new VehicleTO.TimestampedVehicleStateTO()
                .setState(VehicleTO.StateTO.EXECUTING)
                .setTimestamp(Instant.parse("2025-01-29T11:48:37.000Z"))
        )
        .setProcState(
            new VehicleTO.TimestampedVehicleProcStateTO()
                .setProcState(VehicleTO.ProcStateTO.PROCESSING_ORDER)
                .setTimestamp(Instant.parse("2025-01-29T11:41:17.000Z"))
        )
        .setIntegrationLevel(VehicleTO.IntegrationLevelTO.TO_BE_UTILIZED)
        .setPaused(true)
        .setTransportOrder(null)
        .setOrderSequence(null)
        .setAcceptableOrderTypes(List.of())
        .setClaimedResources(List.of())
        .setAllocatedResources(List.of())
        .setCurrentPosition(null)
        .setPose(
            new PoseTO()
                .setPosition(null)
                .setOrientationAngle(null)
        )
        .setEnvelopeKey("envelopeType-01")
        .setLayout(
            new VehicleTO.LayoutTO().setRouteColor(
                new ColorTO().setRed(1).setGreen(2).setBlue(3)
            )
        );
  }

  private VehicleTO createVehicleFull() {
    return new VehicleTO()
        .setName("some-vehicle")
        .setProperties(
            new TreeMap<>(
                Map.of(
                    "some-key", "some-value",
                    "some-other-key", "some-other-value"
                )
            )
        )
        .setHistory(
            new ObjectHistoryTO()
                .setEntries(
                    List.of(
                        new ObjectHistoryTO.ObjectHistoryEntryTO()
                            .setEventCode("some-event-code")
                            .setTimestamp(Instant.parse("2025-01-29T11:38:37.000Z"))
                            .setSupplements(List.of("some-supplement"))
                    )
                )
        )
        .setBoundingBox(
            new BoundingBoxTO()
                .setLength(50)
                .setWidth(40)
                .setHeight(30)
                .setReferenceOffset(
                    new CoupleTO()
                        .setX(20)
                        .setY(10)
                )
        )
        .setEnergyLevelThresholdSet(
            new VehicleTO.EnergyLevelThresholdSetTO()
                .setEnergyLevelGood(90)
                .setEnergyLevelCritical(30)
                .setEnergyLevelSufficientlyRecharged(30)
                .setEnergyLevelFullyRecharged(90)
        )
        .setEnergyLevel(48)
        .setMaxVelocity(10)
        .setMaxReverseVelocity(20)
        .setRechargeOperation("some-recharge-operation")
        .setLoadHandlingDevices(
            List.of(
                new VehicleTO.LoadHandlingDeviceTO()
                    .setLabel("some-load-handling-device")
                    .setFull(true)
            )
        )
        .setState(
            new VehicleTO.TimestampedVehicleStateTO()
                .setState(VehicleTO.StateTO.EXECUTING)
                .setTimestamp(Instant.parse("2025-01-29T11:48:37.000Z"))
        )
        .setProcState(
            new VehicleTO.TimestampedVehicleProcStateTO()
                .setProcState(VehicleTO.ProcStateTO.PROCESSING_ORDER)
                .setTimestamp(Instant.parse("2025-01-29T11:41:17.000Z"))
        )
        .setIntegrationLevel(VehicleTO.IntegrationLevelTO.TO_BE_UTILIZED)
        .setPaused(true)
        .setTransportOrder("some-order")
        .setOrderSequence("some-order-sequence")
        .setAcceptableOrderTypes(
            List.of(
                new VehicleTO.AcceptableOrderTypeTO()
                    .setName("OrderType001")
                    .setPriority(23),
                new VehicleTO.AcceptableOrderTypeTO()
                    .setName("OrderType002")
                    .setPriority(42)
            )
        )
        .setClaimedResources(
            List.of(
                List.of(
                    new ResourceTO()
                        .setName("some-path")
                        .setType(ResourceTO.ResourceTypeTO.PATH),
                    new ResourceTO()
                        .setName("some-point")
                        .setType(ResourceTO.ResourceTypeTO.POINT),
                    new ResourceTO()
                        .setName("some-location")
                        .setType(ResourceTO.ResourceTypeTO.LOCATION)
                ),
                List.of(
                    new ResourceTO()
                        .setName("some-other-path")
                        .setType(ResourceTO.ResourceTypeTO.PATH),
                    new ResourceTO()
                        .setName("some-other-point")
                        .setType(ResourceTO.ResourceTypeTO.POINT)
                )
            )
        )
        .setAllocatedResources(
            List.of(
                List.of(
                    new ResourceTO()
                        .setName("some-path")
                        .setType(ResourceTO.ResourceTypeTO.PATH),
                    new ResourceTO()
                        .setName("some-point")
                        .setType(ResourceTO.ResourceTypeTO.POINT),
                    new ResourceTO()
                        .setName("some-location")
                        .setType(ResourceTO.ResourceTypeTO.LOCATION)
                ),
                List.of(
                    new ResourceTO()
                        .setName("some-other-path")
                        .setType(ResourceTO.ResourceTypeTO.PATH),
                    new ResourceTO()
                        .setName("some-other-point")
                        .setType(ResourceTO.ResourceTypeTO.POINT)
                )
            )
        )
        .setCurrentPosition("some-point")
        .setPose(
            new PoseTO()
                .setPosition(new TripleTO().setX(1).setY(2).setZ(3))
                .setOrientationAngle(90.0)
        )
        .setEnvelopeKey("envelopeType-01")
        .setLayout(
            new VehicleTO.LayoutTO().setRouteColor(
                new ColorTO().setRed(1).setGreen(2).setBlue(3)
            )
        );
  }
}
