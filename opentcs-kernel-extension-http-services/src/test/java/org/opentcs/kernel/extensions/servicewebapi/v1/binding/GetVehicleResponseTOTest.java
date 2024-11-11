// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
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
            .setIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
            .setPaused(false)
            .setProcState(Vehicle.ProcState.PROCESSING_ORDER)
            .setTransportOrder("some-order")
            .setCurrentPosition("some-point")
            .setPrecisePosition(new GetVehicleResponseTO.PrecisePosition(1, 2, 3))
            .setOrientationAngle(orientationAngle)
            .setState(Vehicle.State.EXECUTING)
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
            .setAllowedOrderTypes(
                List.of(
                    "OrderType001",
                    "OrderType002"
                )
            );

    Approvals.verify(
        jsonBinder.toJson(to),
        Approvals.NAMES.withParameters("orientationAngle-" + orientationAngle)
    );
  }

}
