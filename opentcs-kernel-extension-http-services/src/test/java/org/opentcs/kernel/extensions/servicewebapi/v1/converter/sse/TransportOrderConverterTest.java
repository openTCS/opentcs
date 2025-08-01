// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter.sse;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.TimestampScrubber;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.TransportOrderEventTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.TransportOrderEventTO.DriveOrderTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.TransportOrderEventTO.DriveOrderTO.RouteTO.ReroutingTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse.TransportOrderEventTO.DriveOrderTO.RouteTO.VehicleOrientationTO;

/**
 * Tests for {@link TransportOrderConverter}.
 */
class TransportOrderConverterTest {

  private JsonBinder jsonBinder;
  private TransportOrderConverter converter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    converter = new TransportOrderConverter();
  }

  @Test
  void convert() {
    TransportOrder order = new TransportOrder("order-1", List.of())
        .withProperties(Map.of("key-1", "value-1", "key-2", "value-2"))
        .withHistoryEntry(
            new ObjectHistory.Entry(Instant.EPOCH, "event-code-1", List.of("supplement-1"))
        )
        .withType("type-1")
        .withDependencies(
            Set.of(
                new TransportOrder("dependency-1", List.of()).getReference(),
                new TransportOrder("dependency-2", List.of()).getReference()
            )
        )
        .withPeripheralReservationToken("reservation-token-1")
        .withCurrentDriveOrderIndex(3)
        .withCurrentRouteStepIndex(2)
        .withState(TransportOrder.State.FINISHED)
        .withCreationTime(Instant.EPOCH)
        .withDeadline(Instant.EPOCH)
        .withFinishedTime(Instant.EPOCH)
        .withIntendedVehicle(new Vehicle("vehicle-1").getReference())
        .withProcessingVehicle(new Vehicle("vehicle-2").getReference())
        .withWrappingSequence(new OrderSequence("sequence-1").getReference())
        .withDispensable(true);
    order = order.withDriveOrders(
        List.of(
            new DriveOrder(
                "drive-order-1",
                new DriveOrder.Destination(
                    new Location(
                        "location-1",
                        new LocationType("location-type-1").getReference()
                    ).getReference()
                )
                    .withOperation("operation-1")
                    .withProperties(Map.of("key-3", "value-3"))
            )
                .withTransportOrder(order.getReference())
                .withRoute(
                    new Route(
                        List.of(
                            new Route.Step(
                                new Path(
                                    "path-1-2",
                                    new Point("point-1").getReference(),
                                    new Point("point-2").getReference()
                                ),
                                new Point("point-1"),
                                new Point("point-2"),
                                Vehicle.Orientation.FORWARD,
                                1,
                                200
                            ),
                            new Route.Step(
                                null,
                                null,
                                new Point("point-2"),
                                Vehicle.Orientation.FORWARD,
                                2,
                                100
                            )
                                .withReroutingType(ReroutingType.REGULAR)
                                .withExecutionAllowed(false)
                        )
                    )
                )
                .withState(DriveOrder.State.FINISHED)
        )
    );

    TransportOrderEventTO.TransportOrderTO result = converter.convert(order);

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
    Instant time = Instant.parse("2025-07-24T12:03:00Z");
    TransportOrder order = new TransportOrder("order-1", List.of())
        .withCreationTime(time)
        .withDeadline(time.plus(1, ChronoUnit.HOURS))
        .withFinishedTime(time.plus(2, ChronoUnit.HOURS))
        .withHistoryEntry(
            new ObjectHistory.Entry(
                time.plus(3, ChronoUnit.HOURS),
                "event-code-1",
                List.of("supplement-1")
            )
        );

    TransportOrderEventTO.TransportOrderTO result = converter.convert(order);

    assertThat(result.getCreationTime()).isEqualTo(time);
    assertThat(result.getDeadline()).isEqualTo(time.plus(1, ChronoUnit.HOURS));
    assertThat(result.getFinishedTime()).isEqualTo(time.plus(2, ChronoUnit.HOURS));
    assertThat(result.getHistory().getEntries())
        .extracting(
            ObjectHistoryTO.ObjectHistoryEntryTO::getTimestamp,
            ObjectHistoryTO.ObjectHistoryEntryTO::getEventCode,
            ObjectHistoryTO.ObjectHistoryEntryTO::getSupplements
        )
        .contains(
            Tuple.tuple(
                time.plus(3, ChronoUnit.HOURS),
                "event-code-1",
                List.of("supplement-1")
            )
        );
  }

  @ParameterizedTest
  @EnumSource(TransportOrder.State.class)
  void convertsAllTransportOrderStates(TransportOrder.State state) {
    TransportOrder order = new TransportOrder("order-1", List.of()).withState(state);

    TransportOrderEventTO.TransportOrderTO result = converter.convert(order);

    TransportOrderEventTO.StateTO expectedState = switch (state) {
      case RAW -> TransportOrderEventTO.StateTO.RAW;
      case ACTIVE -> TransportOrderEventTO.StateTO.ACTIVE;
      case DISPATCHABLE -> TransportOrderEventTO.StateTO.DISPATCHABLE;
      case BEING_PROCESSED -> TransportOrderEventTO.StateTO.BEING_PROCESSED;
      case WITHDRAWN -> TransportOrderEventTO.StateTO.WITHDRAWN;
      case FINISHED -> TransportOrderEventTO.StateTO.FINISHED;
      case FAILED -> TransportOrderEventTO.StateTO.FAILED;
      case UNROUTABLE -> TransportOrderEventTO.StateTO.UNROUTABLE;
    };
    assertThat(result.getState()).isEqualTo(expectedState);
  }

  @ParameterizedTest
  @EnumSource(DriveOrder.State.class)
  void convertsAllDriveOrderStates(DriveOrder.State state) {
    TransportOrder order = new TransportOrder(
        "order-1",
        List.of(
            new DriveOrder(
                "drive-order-1",
                new DriveOrder.Destination(new Point("point-1").getReference())
            ).withState(state)
        )
    );

    TransportOrderEventTO.TransportOrderTO result = converter.convert(order);

    DriveOrderTO.StateTO expectedState = switch (state) {
      case PRISTINE -> DriveOrderTO.StateTO.PRISTINE;
      case TRAVELLING -> DriveOrderTO.StateTO.TRAVELLING;
      case OPERATING -> DriveOrderTO.StateTO.OPERATING;
      case FINISHED -> DriveOrderTO.StateTO.FINISHED;
      case FAILED -> DriveOrderTO.StateTO.FAILED;
    };
    assertThat(result.getDriveOrders().getFirst().getState()).isEqualTo(expectedState);
  }

  @ParameterizedTest
  @EnumSource(Vehicle.Orientation.class)
  void convertsAllVehicleOrientations(Vehicle.Orientation orientation) {
    TransportOrder order = new TransportOrder(
        "order-1",
        List.of(
            new DriveOrder(
                "drive-order-1",
                new DriveOrder.Destination(new Point("point-1").getReference())
            ).withRoute(
                new Route(
                    List.of(
                        new Route.Step(
                            null,
                            null,
                            new Point("point-1"),
                            orientation,
                            0,
                            1
                        )
                    )
                )
            )
        )
    );

    TransportOrderEventTO.TransportOrderTO result = converter.convert(order);

    VehicleOrientationTO expectedOrientation
        = switch (orientation) {
          case FORWARD -> VehicleOrientationTO.FORWARD;
          case BACKWARD -> VehicleOrientationTO.BACKWARD;
          case UNDEFINED -> VehicleOrientationTO.UNDEFINED;
        };
    assertThat(
        result.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getVehicleOrientation()
    ).isEqualTo(expectedOrientation);
  }

  @ParameterizedTest
  @EnumSource(ReroutingType.class)
  void convertsAllReroutingTypes(ReroutingType reroutingType) {
    TransportOrder order = new TransportOrder(
        "order-1",
        List.of(
            new DriveOrder(
                "drive-order-1",
                new DriveOrder.Destination(new Point("point-1").getReference())
            ).withRoute(
                new Route(
                    List.of(
                        new Route.Step(
                            null,
                            null,
                            new Point("point-1"),
                            Vehicle.Orientation.FORWARD,
                            0,
                            1
                        )
                            .withReroutingType(reroutingType)
                    )
                )
            )
        )
    );

    TransportOrderEventTO.TransportOrderTO result = converter.convert(order);

    ReroutingTypeTO expectedReroutingType = switch (reroutingType) {
      case REGULAR -> ReroutingTypeTO.REGULAR;
      case FORCED -> ReroutingTypeTO.FORCED;
    };
    assertThat(
        result.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getReroutingType()
    ).isEqualTo(expectedReroutingType);
  }
}
