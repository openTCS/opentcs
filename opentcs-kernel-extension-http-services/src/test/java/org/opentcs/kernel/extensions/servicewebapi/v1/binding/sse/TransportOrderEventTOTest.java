// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.TransportOrderEventTO.DriveOrderTO.RouteTO.ReroutingTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.TransportOrderEventTO.DriveOrderTO.RouteTO.VehicleOrientationTO;

/**
 * Tests for {@link TransportOrderEventTO}.
 */
class TransportOrderEventTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSample() {
    TransportOrderEventTO to = new TransportOrderEventTO(
        createTransportOrder(TransportOrderEventTO.StateTO.RAW),
        createTransportOrder(TransportOrderEventTO.StateTO.BEING_PROCESSED)
    );

    Approvals.verify(jsonBinder.toJson(to));
  }

  private TransportOrderEventTO.TransportOrderTO createTransportOrder(
      TransportOrderEventTO.StateTO state
  ) {
    Instant time = Instant.parse("2025-07-22T12:03:00Z");
    return new TransportOrderEventTO.TransportOrderTO()
        .setName("order-1")
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
        .setType("type-1")
        .setDependencies(List.of("dependency-1", "dependency-2"))
        .setDriveOrders(
            List.of(
                new TransportOrderEventTO.DriveOrderTO()
                    .setName("drive-order-1")
                    .setDestination(
                        new TransportOrderEventTO.DriveOrderTO.DestinationTO()
                            .setDestination("destination-1")
                            .setOperation("operation-1")
                            .setProperties(
                                new TreeMap<>(Map.of("key-3", "value-3", "key-4", "value-4"))
                            )
                    )
                    .setTransportOrder("order-1")
                    .setRoute(
                        new TransportOrderEventTO.DriveOrderTO.RouteTO()
                            .setSteps(
                                List.of(
                                    new TransportOrderEventTO.DriveOrderTO.RouteTO.StepTO()
                                        .setPath("path-1")
                                        .setSourcePoint("point-1")
                                        .setDestinationPoint("point-2")
                                        .setVehicleOrientation(VehicleOrientationTO.FORWARD)
                                        .setRouteIndex(1)
                                        .setCosts(100)
                                        .setExecutionAllowed(true)
                                        .setReroutingType(ReroutingTypeTO.REGULAR)
                                )
                            )
                            .setCosts(200)

                    )
                    .setState(TransportOrderEventTO.DriveOrderTO.StateTO.TRAVELLING)
            )
        )
        .setPeripheralReservationToken("reservation-token-1")
        .setCurrentDriveOrderIndex(3)
        .setCurrentRouteStepIndex(5)
        .setState(state)
        .setCreationTime(time)
        .setDeadline(time.plus(1, ChronoUnit.HOURS))
        .setFinishedTime(time.plus(30, ChronoUnit.MINUTES))
        .setIntendedVehicle("vehicle-1")
        .setProcessingVehicle("vehicle-2")
        .setWrappingSequence("sequence-1")
        .setDispensable(true);
  }
}
