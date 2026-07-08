// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.TransportOrderTO.DriveOrderTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.TransportOrderTO.DriveOrderTO.RouteTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.TransportOrderTO.DriveOrderTO.RouteTO.StepTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.TransportOrderTO.DriveOrderTO.RouteTO.StepTO.VehicleOrientationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

/**
 * Tests for {@link TransportOrderTO}.
 */
class TransportOrderTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createTransportOrderMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createTransportOrderFull()));
  }

  private TransportOrderTO createTransportOrderMinimal() {
    return new TransportOrderTO()
        .setName("some-order")
        .setProperties(Map.of())
        .setHistory(new ObjectHistoryTO().setEntries(List.of()))
        .setType("some-type")
        .setDependencies(List.of())
        .setDriveOrders(List.of())
        .setPeripheralReservationToken(null)
        .setCurrentDriveOrderIndex(1)
        .setCurrentRouteStepIndex(2)
        .setState(TransportOrderTO.StateTO.ACTIVE)
        .setCreationTime(Instant.parse("2025-11-28T15:45:41.000Z"))
        .setDeadline(null)
        .setFinishedTime(null)
        .setIntendedVehicle(null)
        .setProcessingVehicle(null)
        .setWrappingSequence(null)
        .setDispensable(true);
  }

  private TransportOrderTO createTransportOrderFull() {
    return new TransportOrderTO()
        .setName("some-order")
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "some-other-key", "some-other-value"
            )
        )
        .setHistory(
            new ObjectHistoryTO()
                .setEntries(
                    List.of(
                        new ObjectHistoryTO.ObjectHistoryEntryTO()
                            .setEventCode("some-event-code")
                            .setTimestamp(Instant.parse("2025-11-28T15:55:41.000Z"))
                            .setSupplements(List.of("some-supplement"))
                    )
                )
        )
        .setType("some-type")
        .setDependencies(List.of("some-other-order", "yet-another-order"))
        .setDriveOrders(
            List.of(
                // Drive order with null values
                new DriveOrderTO()
                    .setName("drive-order-0")
                    .setDestination(
                        new DriveOrderTO.DestinationTO()
                            .setDestination("some-destination")
                            .setOperation("some-operation")
                            .setProperties(Map.of())
                    )
                    .setTransportOrder("some-order")
                    .setRoute(null)
                    .setState(DriveOrderTO.StateTO.PRISTINE),
                // Drive order without null values
                new DriveOrderTO()
                    .setName("drive-order-1")
                    .setDestination(
                        new DriveOrderTO.DestinationTO()
                            .setDestination("some-other-destination")
                            .setOperation("some-other-operation")
                            .setProperties(
                                Map.of(
                                    "yet-another-key", "yet-another-value"
                                )
                            )
                    )
                    .setTransportOrder("some-order")
                    .setRoute(
                        new RouteTO()
                            .setCosts(300)
                            .setSteps(
                                List.of(
                                    // Step with null values
                                    new StepTO()
                                        .setPath(null)
                                        .setSourcePoint(null)
                                        .setDestinationPoint("some-destination-point")
                                        .setVehicleOrientation(VehicleOrientationTO.FORWARD)
                                        .setRouteIndex(0)
                                        .setCosts(150)
                                        .setExecutionAllowed(true)
                                        .setReroutingType(null),
                                    // Step without null values
                                    new StepTO()
                                        .setPath("some-path")
                                        .setSourcePoint("some-source-point")
                                        .setDestinationPoint("some-other-destination-point")
                                        .setVehicleOrientation(VehicleOrientationTO.BACKWARD)
                                        .setRouteIndex(1)
                                        .setCosts(150)
                                        .setExecutionAllowed(true)
                                        .setReroutingType(StepTO.ReroutingTypeTO.REGULAR)
                                )
                            )
                    )
                    .setState(DriveOrderTO.StateTO.PRISTINE)
            )
        )
        .setPeripheralReservationToken("some-reservation-token")
        .setCurrentDriveOrderIndex(1)
        .setCurrentRouteStepIndex(2)
        .setState(TransportOrderTO.StateTO.FINISHED)
        .setCreationTime(Instant.parse("2025-11-28T15:45:41.000Z"))
        .setDeadline(Instant.parse("2025-11-28T19:45:41.000Z"))
        .setFinishedTime(Instant.parse("2025-11-28T16:45:41.000Z"))
        .setIntendedVehicle("some-intended-vehicle")
        .setProcessingVehicle("some-processing-vehicle")
        .setWrappingSequence("some-wrapping-sequence")
        .setDispensable(true);
  }
}
