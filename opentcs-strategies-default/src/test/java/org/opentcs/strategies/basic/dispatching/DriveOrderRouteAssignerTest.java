// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.RouteSelector;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

/**
 * Tests for {@link DriveOrderRouteAssigner}.
 */
class DriveOrderRouteAssignerTest {

  private RouteSelector routeSelector;
  private DriveOrderRouteAssigner assigner;

  @BeforeEach
  void setUp() {
    Router router = mock();
    routeSelector = mock();
    DefaultDispatcherConfiguration configuration = mock();
    assigner = new DriveOrderRouteAssigner(router, routeSelector, configuration);
  }

  @Test
  void assignRoutesToDriveOrders() {
    Point pointA = new Point("A");
    Point pointB = new Point("B");
    DriveOrder driveOrderToA = new DriveOrder(
        "order1", new DriveOrder.Destination(pointA.getReference())
    );
    DriveOrder driveOrderToB = new DriveOrder(
        "order2", new DriveOrder.Destination(pointB.getReference())
    );
    List<DriveOrder> driveOrdersWithoutRoutes = List.of(driveOrderToA, driveOrderToB);
    TransportOrder order = new TransportOrder("order", driveOrdersWithoutRoutes);
    Vehicle vehicle = new Vehicle("vehicle");
    when(routeSelector.selectSequence(any()))
        .thenReturn(Optional.of(List.of(routeTo(pointA), routeTo(pointB))));

    Optional<List<DriveOrder>> result = assigner.tryAssignRoutes(order, vehicle, pointB);

    assertThat(result).isPresent();
    assertThat(result).contains(
        List.of(
            driveOrderToA.withTransportOrder(order.getReference()).withRoute(routeTo(pointA)),
            driveOrderToB.withTransportOrder(order.getReference()).withRoute(routeTo(pointB))
        )
    );
  }

  @Test
  void returnEmptyOptionalIfNoRouteSelected() {
    when(routeSelector.selectSequence(any())).thenReturn(Optional.empty());

    Optional<List<DriveOrder>> result = assigner.tryAssignRoutes(
        new TransportOrder("order", List.of()),
        new Vehicle("vehicle"),
        new Point("A")
    );

    assertThat(result).isEmpty();
  }

  private Route routeTo(Point point) {
    return new Route(
        List.of(
            new Route.Step(
                null,
                null,
                point,
                Vehicle.Orientation.FORWARD,
                0,
                0
            )
        )
    );
  }
}
