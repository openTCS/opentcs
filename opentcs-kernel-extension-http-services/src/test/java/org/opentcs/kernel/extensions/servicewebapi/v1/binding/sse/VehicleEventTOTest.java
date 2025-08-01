// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;

/**
 * Tests for {@link VehicleEventTO}.
 */
class VehicleEventTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    VehicleEventTO to = new VehicleEventTO(
        createVehicle(VehicleEventTO.IntegrationLevelTO.TO_BE_UTILIZED),
        createVehicle(VehicleEventTO.IntegrationLevelTO.TO_BE_RESPECTED)
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private VehicleEventTO.VehicleTO createVehicle(
      VehicleEventTO.IntegrationLevelTO integrationLevel
  ) {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new VehicleEventTO.VehicleTO()
        .setName("vehicle-1")
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
        .setBoundingBox(
            new VehicleEventTO.BoundingBoxTO()
                .setLength(1)
                .setWidth(2)
                .setHeight(3)
                .setReferenceOffset(new VehicleEventTO.CoupleTO().setX(4).setY(5))
        )
        .setEnergyLevelThresholdSet(
            new VehicleEventTO.EnergyLevelThresholdSetTO()
                .setEnergyLevelCritical(1)
                .setEnergyLevelGood(2)
                .setEnergyLevelSufficientlyRecharged(3)
                .setEnergyLevelFullyRecharged(4)
        )
        .setMaxVelocity(200)
        .setMaxReverseVelocity(100)
        .setRechargeOperation("recharge-operation")
        .setProcState(
            new VehicleEventTO.TimestampedVehicleProcStateTO()
                .setProcState(VehicleEventTO.ProcStateTO.IDLE)
                .setTimestamp(time)
        )
        .setTransportOrder("transport-order-1")
        .setOrderSequence("order-sequence-1")
        .setAcceptableOrderTypes(
            List.of(
                new VehicleEventTO.AcceptableOrderTypeTO()
                    .setName("acceptable-order-type-1")
                    .setPriority(2),
                new VehicleEventTO.AcceptableOrderTypeTO()
                    .setName("acceptable-order-type-2")
                    .setPriority(5)
            )
        )
        .setClaimedResources(
            List.of(
                List.of("resource-1", "resource-2")
            )
        )
        .setAllocatedResources(
            List.of(
                List.of("resource-3", "resource-4"),
                List.of("resource-5", "resource-6")
            )
        )
        .setState(
            new VehicleEventTO.TimestampedVehicleStateTO()
                .setState(VehicleEventTO.StateTO.IDLE)
                .setTimestamp(time)
        )
        .setIntegrationLevel(integrationLevel)
        .setPaused(true)
        .setCurrentPosition("position-1")
        .setPose(
            new VehicleEventTO.PoseTO()
                .setPosition(
                    new VehicleEventTO.TripleTO()
                        .setX(1)
                        .setY(2)
                        .setZ(3)
                )
                .setOrientationAngle(4.5)
        )
        .setEnergyLevel(42)
        .setLoadHandlingDevices(
            List.of(
                new VehicleEventTO.LoadHandlingDeviceTO()
                    .setLabel("lhd-1")
                    .setFull(true)
            )
        )
        .setEnvelopeKey("envelope-key-1")
        .setLayout(
            new VehicleEventTO.LayoutTO()
                .setRouteColor(
                    new VehicleEventTO.LayoutTO.ColorTO()
                        .setRed(60)
                        .setGreen(20)
                        .setBlue(40)
                )
        );
  }
}
