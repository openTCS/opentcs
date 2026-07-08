// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.sse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ColorTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.PoseTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ResourceTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.TripleTO;

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
        createVehicle(VehicleTO.IntegrationLevelTO.TO_BE_UTILIZED),
        createVehicle(VehicleTO.IntegrationLevelTO.TO_BE_RESPECTED)
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private VehicleTO createVehicle(
      VehicleTO.IntegrationLevelTO integrationLevel
  ) {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new VehicleTO()
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
            new BoundingBoxTO()
                .setLength(1)
                .setWidth(2)
                .setHeight(3)
                .setReferenceOffset(new CoupleTO().setX(4).setY(5))
        )
        .setEnergyLevelThresholdSet(
            new VehicleTO.EnergyLevelThresholdSetTO()
                .setEnergyLevelCritical(1)
                .setEnergyLevelGood(2)
                .setEnergyLevelSufficientlyRecharged(3)
                .setEnergyLevelFullyRecharged(4)
        )
        .setMaxVelocity(200)
        .setMaxReverseVelocity(100)
        .setRechargeOperation("recharge-operation")
        .setProcState(
            new VehicleTO.TimestampedVehicleProcStateTO()
                .setProcState(VehicleTO.ProcStateTO.IDLE)
                .setTimestamp(time)
        )
        .setTransportOrder("transport-order-1")
        .setOrderSequence("order-sequence-1")
        .setAcceptableOrderTypes(
            List.of(
                new VehicleTO.AcceptableOrderTypeTO()
                    .setName("acceptable-order-type-1")
                    .setPriority(2),
                new VehicleTO.AcceptableOrderTypeTO()
                    .setName("acceptable-order-type-2")
                    .setPriority(5)
            )
        )
        .setClaimedResources(
            List.of(
                List.of(
                    new ResourceTO()
                        .setName("resource-1")
                        .setType(ResourceTO.ResourceTypeTO.PATH),
                    new ResourceTO()
                        .setName("resource-2")
                        .setType(ResourceTO.ResourceTypeTO.POINT)
                )
            )
        )
        .setAllocatedResources(
            List.of(
                List.of(
                    new ResourceTO()
                        .setName("resource-3")
                        .setType(ResourceTO.ResourceTypeTO.PATH),
                    new ResourceTO()
                        .setName("resource-4")
                        .setType(ResourceTO.ResourceTypeTO.POINT)
                ),
                List.of(
                    new ResourceTO()
                        .setName("resource-5")
                        .setType(ResourceTO.ResourceTypeTO.PATH),
                    new ResourceTO()
                        .setName("resource-6")
                        .setType(ResourceTO.ResourceTypeTO.POINT),
                    new ResourceTO()
                        .setName("resource-7")
                        .setType(ResourceTO.ResourceTypeTO.LOCATION)
                )
            )
        )
        .setState(
            new VehicleTO.TimestampedVehicleStateTO()
                .setState(VehicleTO.StateTO.IDLE)
                .setTimestamp(time)
        )
        .setIntegrationLevel(integrationLevel)
        .setPaused(true)
        .setCurrentPosition("position-1")
        .setPose(
            new PoseTO()
                .setPosition(new TripleTO().setX(1).setY(2).setZ(3))
                .setOrientationAngle(4.5)
        )
        .setEnergyLevel(42)
        .setLoadHandlingDevices(
            List.of(
                new VehicleTO.LoadHandlingDeviceTO()
                    .setLabel("lhd-1")
                    .setFull(true)
            )
        )
        .setEnvelopeKey("envelope-key-1")
        .setLayout(
            new VehicleTO.LayoutTO()
                .setRouteColor(
                    new ColorTO()
                        .setRed(60)
                        .setGreen(20)
                        .setBlue(40)
                )
        );
  }
}
