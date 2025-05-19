// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.AcceptableOrderTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;

/**
 * Unit tests for {@link GetVehicleResponseTO}.
 */
class GetVehicleResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, 90.0})
  void jsonSample(double orientationAngle) {
    GetVehicleResponseTO to
        = new GetVehicleResponseTO()
            .setName("some-vehicle")
            .setProperties(
                new TreeMap<>(
                    Map.of(
                        "some-key", "some-value",
                        "some-other-key", "some-other-value"
                    )
                )
            )
            .setBoundingBox(new BoundingBoxTO(50, 40, 30, new CoupleTO(0, 0)))
            .setEnergyLevelGood(90)
            .setEnergyLevelCritical(30)
            .setEnergyLevelSufficientlyRecharged(30)
            .setEnergyLevelFullyRecharged(90)
            .setEnergyLevel(48)
            .setIntegrationLevel(VehicleTO.IntegrationLevel.TO_BE_UTILIZED)
            .setPaused(false)
            .setProcState(VehicleTO.ProcState.PROCESSING_ORDER)
            .setProcStateTimestamp(Instant.parse("2025-01-29T11:41:17.000Z"))
            .setTransportOrder("some-order")
            .setCurrentPosition("some-point")
            .setPrecisePosition(new GetVehicleResponseTO.PrecisePosition(1, 2, 3))
            .setOrientationAngle(orientationAngle)
            .setState(VehicleTO.State.EXECUTING)
            .setStateTimestamp(Instant.parse("2025-01-29T11:48:37.000Z"))
            .setEnvelopeKey("envelopeType-01")
            .setAllocatedResources(
                List.of(
                    List.of("some-path", "some-point"),
                    List.of("some-other-path", "some-other-point")
                )
            )
            .setClaimedResources(
                List.of(
                    List.of("some-path", "some-point"),
                    List.of("some-other-path", "some-other-point")
                )
            )
            .setAcceptableOrderTypes(
                List.of(
                    new AcceptableOrderTypeTO("OrderType001", 23),
                    new AcceptableOrderTypeTO("OrderType002", 42)
                )
            );

    Approvals.verify(
        jsonBinder.toJson(to),
        Approvals.NAMES.withParameters("orientationAngle-" + orientationAngle)
    );
  }

}
