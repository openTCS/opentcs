// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.time.Instant;
import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DestinationState;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DriveOrderTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.RouteTO;

/**
 * Unit tests for {@link GetTransportOrderResponseTO}.
 */
class GetTransportOrderResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @SuppressWarnings("deprecation")
  @Test
  void jsonSample() {
    GetTransportOrderResponseTO to
        = new GetTransportOrderResponseTO()
            .setDispensable(true)
            .setName("some-order")
            .setHistory(
                new ObjectHistoryTO(
                    List.of(
                        new ObjectHistoryTO.ObjectHistoryEntryTO(
                            Instant.EPOCH,
                            "code-1",
                            List.of("supp-1")
                        )
                    )
                )
            )
            .setDependencies(List.of("order-1"))
            .setDriveOrders(
                List.of(
                    new DriveOrderTO(
                        "DriveOrder-1",
                        new DriveOrderTO.DestinationTO(
                            "location-1",
                            "operation-1",
                            List.of(new Property("key", "value"))
                        ),
                        "T-Order1",
                        new RouteTO(
                            1000, List.of(
                                new RouteTO.Step(
                                    "path1",
                                    "point1",
                                    "point2",
                                    RouteTO.Step.VehicleOrientationTO.FORWARD,
                                    1,
                                    50,
                                    true,
                                    RouteTO.Step.ReroutingTypeTO.REGULAR
                                )
                            )
                        ),
                        DriveOrderTO.StateTO.OPERATING
                    )
                )
            )
            .setCurrentDriveOrderIndex(1)
            .setCurrentRouteStepIndex(1)
            .setCreationTime(Instant.EPOCH)
            .setDeadline(Instant.EPOCH)
            .setFinishedTime(Instant.EPOCH)
            .setPeripheralReservationToken("some-token")
            .setWrappingSequence("some-sequence")
            .setType("some-type")
            .setState(GetTransportOrderResponseTO.State.BEING_PROCESSED)
            .setIntendedVehicle("some-vehicle")
            .setProcessingVehicle("some-vehicle")
            .setProperties(
                List.of(
                    new Property("some-key", "some-value"),
                    new Property("another-key", "another-value")
                )
            )
            .setDestinations(
                List.of(
                    new DestinationState()
                        .setLocationName("some-location")
                        .setOperation("some-operation")
                        .setState(DestinationState.State.TRAVELLING)
                        .setProperties(
                            List.of(
                                new Property("some-key", "some-value"),
                                new Property("some-other-key", "some-other-value")
                            )
                        )
                )
            );

    Approvals.verify(jsonBinder.toJson(to));
  }

}
