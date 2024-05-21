/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Router;
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
  /**
   * Test dependencies.
   */
  private Router router;

  @BeforeEach
  void setUp() {
    router = mock(Router.class);
    driveOrderMerger = new RegularDriveOrderMerger(router);
  }

  @Test
  void shouldMergeDriveOrders() {
    // Arrange
    DriveOrder orderA = createDriveOrder(10, "A", "B", "C", "D", "E", "F", "G");
    DriveOrder orderB = createDriveOrder(10, "D", "H", "I", "J");
    when(router.getCosts(any(Vehicle.class), any(Point.class), any(Point.class), anySet()))
        .thenReturn(20L);
    Route expected = createDriveOrder(20, "A", "B", "C", "D", "H", "I", "J").getRoute();

    // Act
    Route actual = driveOrderMerger.mergeDriveOrders(
        orderA,
        orderB,
        TransportOrder.ROUTE_STEP_INDEX_DEFAULT,
        new Vehicle("Vehicle")
    ).getRoute();

    // Assert
    assertStepsEqualsIgnoringReroutingType(expected, actual);
  }

  private DriveOrder createDriveOrder(long routeCosts, String startPoint, String... pointNames) {
    List<Point> points = new ArrayList<>();
    for (String pointName : pointNames) {
      points.add(new Point(pointName));
    }
    DriveOrder.Destination dest
        = new DriveOrder.Destination(points.get(points.size() - 1).getReference());
    return new DriveOrder(dest).withRoute(createRoute(new Point(startPoint), points, routeCosts));
  }

  private Route createRoute(Point startPoint, List<Point> points, long costs) {
    List<Route.Step> routeSteps = new ArrayList<>();
    Point srcPoint = startPoint;
    Point destPoint = points.get(0);
    Path path = new Path(srcPoint.getName() + " --- " + destPoint.getName(),
                         srcPoint.getReference(),
                         destPoint.getReference());
    routeSteps.add(new Route.Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, 0));

    for (int i = 1; i < points.size(); i++) {
      srcPoint = points.get(i - 1);
      destPoint = points.get(i);
      path = new Path(srcPoint.getName() + " --- " + destPoint.getName(),
                      srcPoint.getReference(),
                      destPoint.getReference());
      routeSteps.add(new Route.Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, i));
    }
    return new Route(routeSteps, costs);
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
          && stepA.isExecutionAllowed() == stepB.isExecutionAllowed()
      );
    }
  }
}
