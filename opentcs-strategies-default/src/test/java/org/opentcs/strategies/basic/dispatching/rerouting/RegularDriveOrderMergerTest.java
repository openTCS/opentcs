// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.rerouting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

/**
 * Test cases for {@link RegularDriveOrderMerger}.
 */
class RegularDriveOrderMergerTest {

  /**
   * Class under test.
   */
  private RegularDriveOrderMerger driveOrderMerger;

  @BeforeEach
  void setUp() {
    driveOrderMerger = new RegularDriveOrderMerger();
  }

  @Test
  void shouldMergeDriveOrders() {
    // Arrange
    DriveOrder orderA = createDriveOrder(1, "A", "B", "C", "D", "E", "F", "G");
    DriveOrder orderB = createDriveOrder(1, "D", "H", "I", "J", "K");
    Route expected = createDriveOrder(1, "A", "B", "C", "D", "H", "I", "J", "K").getRoute();

    // Act
    Route actual = driveOrderMerger.mergeDriveOrders(
        orderA,
        orderB,
        new TransportOrder("t1", List.of()),
        TransportOrder.ROUTE_STEP_INDEX_DEFAULT,
        new Vehicle("Vehicle")
    ).getRoute();

    // Assert
    assertStepsEqualsIgnoringReroutingType(expected, actual);
  }

  private DriveOrder createDriveOrder(int stepCost, String startPoint, String... pointNames) {
    List<Point> points = new ArrayList<>();
    for (String pointName : pointNames) {
      points.add(new Point(pointName));
    }
    DriveOrder.Destination dest
        = new DriveOrder.Destination(points.get(points.size() - 1).getReference());
    return new DriveOrder(dest).withRoute(createRoute(new Point(startPoint), points, stepCost));
  }

  private Route createRoute(Point startPoint, List<Point> points, int costPerStep) {
    List<Route.Step> routeSteps = new ArrayList<>();
    Point srcPoint = startPoint;
    Point destPoint = points.get(0);
    Path path = new Path(
        srcPoint.getName() + " --- " + destPoint.getName(),
        srcPoint.getReference(),
        destPoint.getReference()
    );
    routeSteps.add(
        new Route.Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, 0, costPerStep)
    );

    for (int i = 1; i < points.size(); i++) {
      srcPoint = points.get(i - 1);
      destPoint = points.get(i);
      path = new Path(
          srcPoint.getName() + " --- " + destPoint.getName(),
          srcPoint.getReference(),
          destPoint.getReference()
      );
      routeSteps.add(
          new Route.Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, i, costPerStep)
      );
    }
    return new Route(routeSteps);
  }

  private void assertStepsEqualsIgnoringReroutingType(Route routeA, Route routeB) {
    assertThat(routeA.getSteps().size(), is(routeB.getSteps().size()));
    for (int i = 0; i < routeA.getSteps().size(); i++) {
      Route.Step stepA = routeA.getSteps().get(i);
      Route.Step stepB = routeB.getSteps().get(i);
      assertTrue(
          Objects.equals(stepA.getPath(), stepB.getPath())
              && Objects.equals(stepA.getSourcePoint(), stepB.getSourcePoint())
              && Objects.equals(stepA.getDestinationPoint(), stepB.getDestinationPoint())
              && Objects.equals(stepA.getVehicleOrientation(), stepB.getVehicleOrientation())
              && stepA.getRouteIndex() == stepB.getRouteIndex()
              && stepA.getCosts() == stepB.getCosts()
              && stepA.isExecutionAllowed() == stepB.isExecutionAllowed()
      );
    }
  }
}
