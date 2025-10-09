// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.GetTransportOrderResponseTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.getevents.OrderStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.DriveOrderTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.RouteTO;

/**
 * Tests for {@link TransportOrderConverter}.
 */
public class TransportOrderConverterTest {
  private final TransportOrderConverter transportOrderConverter = new TransportOrderConverter();

  @Test
  void checkToOrderStatusMessage() {
    TransportOrder t1
        = new TransportOrder(
            "T1", List.of(
                new DriveOrder("O1", new DriveOrder.Destination(new Point("P1").getReference()))
            )
        )
            .withDispensable(true)
            .withPeripheralReservationToken("some token")
            .withWrappingSequence(new OrderSequence("Os1").getReference())
            .withType("some type")
            .withIntendedVehicle(new Vehicle("V1").getReference())
            .withProcessingVehicle(new Vehicle("V2").getReference())
            .withState(TransportOrder.State.DISPATCHABLE)
            .withProperties(Map.of("some-key", "some-value"));

    OrderStatusMessage statusMessage = transportOrderConverter.toOrderStatusMessage(
        t1, 555, Instant.EPOCH
    );

    assertThat(statusMessage.getOrderName(), is("T1"));
    assertThat(statusMessage.getDestinations().size(), is(1));
    assertThat(statusMessage.getDestinations().getFirst().getLocationName(), is("P1"));
    assertThat(statusMessage.isDispensable(), is(true));
    assertThat(statusMessage.getPeripheralReservationToken(), is("some token"));
    assertThat(statusMessage.getWrappingSequence(), is("Os1"));
    assertThat(statusMessage.getOrderType(), is("some type"));
    assertThat(statusMessage.getIntendedVehicle(), is("V1"));
    assertThat(statusMessage.getProcessingVehicleName(), is("V2"));
    assertThat(statusMessage.getOrderState(), is(OrderStatusMessage.OrderState.DISPATCHABLE));
    assertThat(statusMessage.getProperties().size(), is(1));
    assertThat(statusMessage.getProperties().getFirst().getKey(), is("some-key"));
    assertThat(statusMessage.getProperties().getFirst().getValue(), is("some-value"));
  }

  @Test
  void checkToGetTransportOrderResponse() {
    Point p1 = new Point("P1");
    Point p2 = new Point("P2");
    TransportOrder t1
        = new TransportOrder(
            "T1", List.of(
                new DriveOrder(
                    "D1",
                    new DriveOrder.Destination(p1.getReference())
                        .withOperation(DriveOrder.Destination.OP_MOVE)
                        .withProperties(Map.of("some-key", "some-value"))
                )
                    .withTransportOrder(new TransportOrder("T1", List.of()).getReference())
                    .withRoute(
                        new Route(
                            List.of(
                                new Route.Step(
                                    new Path(
                                        "Path1",
                                        p1.getReference(),
                                        p2.getReference()
                                    ),
                                    p1,
                                    p2,
                                    Vehicle.Orientation.BACKWARD,
                                    1,
                                    300
                                )
                                    .withExecutionAllowed(true)
                                    .withReroutingType(ReroutingType.FORCED)
                            )
                        )
                    )
                    .withState(DriveOrder.State.OPERATING)
            )
        )
            .withDispensable(true)
            .withDependencies(Set.of(new TransportOrder("T2", List.of()).getReference()))
            .withHistory(
                new ObjectHistory()
                    .withEntries(List.of(new ObjectHistory.Entry(Instant.EPOCH, "some-code")))
            )
            .withCurrentDriveOrderIndex(1)
            .withCurrentRouteStepIndex(1)
            .withCreationTime(Instant.EPOCH)
            .withDeadline(Instant.EPOCH)
            .withFinishedTime(Instant.EPOCH)
            .withPeripheralReservationToken("some token")
            .withWrappingSequence(new OrderSequence("Os1").getReference())
            .withType("some type")
            .withIntendedVehicle(new Vehicle("V1").getReference())
            .withProcessingVehicle(new Vehicle("V2").getReference())
            .withState(TransportOrder.State.DISPATCHABLE)
            .withProperties(Map.of("some-key", "some-value"));

    GetTransportOrderResponseTO response = transportOrderConverter.toGetTransportOrderResponse(t1);

    assertThat(response.getName(), is("T1"));
    assertThat(response.getDependencies().size(), is(1));
    assertThat(response.getDependencies().getFirst(), is("T2"));
    assertThat(response.getDriveOrders().size(), is(1));
    assertThat(response.getDriveOrders().getFirst().getName(), is("D1"));
    assertThat(
        response.getDriveOrders().getFirst().getDestination().getDestination(),
        is("P1")
    );
    assertThat(
        response.getDriveOrders().getFirst().getDestination().getOperation(),
        is(DriveOrder.Destination.OP_MOVE)
    );
    assertThat(
        response.getDriveOrders().getFirst().getDestination().getProperties().size(),
        is(1)
    );
    assertThat(
        response.getDriveOrders().getFirst().getDestination().getProperties().getFirst().getKey(),
        is("some-key")
    );
    assertThat(
        response.getDriveOrders().getFirst().getDestination().getProperties().getFirst().getValue(),
        is("some-value")
    );
    assertThat(response.getDriveOrders().getFirst().getTransportOrder(), is("T1"));
    assertThat(response.getDriveOrders().getFirst().getRoute().getCosts(), is(300L));
    assertThat(response.getDriveOrders().getFirst().getRoute().getSteps().size(), is(1));
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getPath(),
        is("Path1")
    );
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getSourcePoint(),
        is("P1")
    );
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getDestinationPoint(),
        is("P2")
    );
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst()
            .getVehicleOrientation(),
        is(RouteTO.Step.VehicleOrientationTO.BACKWARD)
    );
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getRouteIndex(),
        is(1)
    );
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getCosts(),
        is(300L)
    );
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst().isExecutionAllowed(),
        is(true)
    );
    assertThat(
        response.getDriveOrders().getFirst().getRoute().getSteps().getFirst().getReroutingType(),
        is(RouteTO.Step.ReroutingTypeTO.FORCED)
    );
    assertThat(response.getDriveOrders().getFirst().getState(), is(DriveOrderTO.StateTO.OPERATING));
    assertThat(response.getCurrentDriveOrderIndex(), is(1));
    assertThat(response.getCurrentRouteStepIndex(), is(1));
    assertThat(response.isDispensable(), is(true));
    assertThat(response.getCreationTime(), is(Instant.EPOCH));
    assertThat(response.getFinishedTime(), is(Instant.EPOCH));
    assertThat(response.getDeadline(), is(Instant.EPOCH));
    assertThat(response.getPeripheralReservationToken(), is("some token"));
    assertThat(response.getWrappingSequence(), is("Os1"));
    assertThat(response.getType(), is("some type"));
    assertThat(response.getIntendedVehicle(), is("V1"));
    assertThat(response.getProcessingVehicle(), is("V2"));
    assertThat(response.getState(), is(GetTransportOrderResponseTO.State.DISPATCHABLE));
    assertThat(response.getProperties().size(), is(1));
    assertThat(response.getProperties().getFirst().getKey(), is("some-key"));
    assertThat(response.getProperties().getFirst().getValue(), is("some-value"));
  }
}
