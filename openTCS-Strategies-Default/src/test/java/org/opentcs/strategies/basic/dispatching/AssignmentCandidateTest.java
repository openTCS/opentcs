/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

/**
 * Unit tests for {@link AssignmentCandidate}.
 */
class AssignmentCandidateTest {

  private Vehicle vehicle;
  private TransportOrder transportOrder;
  private List<DriveOrder> driveOrders;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle("vehicle1");
    Point point = new Point("point");
    DriveOrder.Destination destination = new DriveOrder.Destination(point.getReference());
    DriveOrder driveOrder = new DriveOrder(destination);
    driveOrders = List.of(driveOrder);
    transportOrder = new TransportOrder("transportOrder1", driveOrders);
  }

  @Test
  void exceptionWhenDriveOrdersEmpty() {
    driveOrders = List.of();

    Exception exception = assertThrows(
        IllegalArgumentException.class, () -> {
          AssignmentCandidate assignmentCandidate = new AssignmentCandidate(vehicle,
                                                                            transportOrder,
                                                                            driveOrders);
        });

    assertThat(exception.getMessage(), containsString("driveOrders is empty"));
  }

  @Test
  void exceptionWhenRouteEmpty() {
    Exception exception = assertThrows(
        IllegalArgumentException.class, () -> {
          AssignmentCandidate assignmentCandidate = new AssignmentCandidate(vehicle,
                                                                            transportOrder,
                                                                            driveOrders);
        });

    assertThat(exception.getMessage(), containsString("a drive order's route is null"));
  }

  @Test
  void calculatesCompleteRoutingCosts() {
    Point point1 = new Point("point1");
    Point point2 = new Point("point2");
    Route.Step step1 = new Route.Step(
        null, null, point1, Vehicle.Orientation.FORWARD, 0, true, null
    );
    Route.Step step2 = new Route.Step(
        null, null, point2, Vehicle.Orientation.FORWARD, 0, true, null
    );
    Route route1 = new Route(List.of(step1), 1234);
    Route route2 = new Route(List.of(step2), 5678);
    DriveOrder driveOrder1 = new DriveOrder(new DriveOrder.Destination(point1.getReference()));
    DriveOrder driveOrder2 = new DriveOrder(new DriveOrder.Destination(point2.getReference()));
    driveOrder1 = driveOrder1.withRoute(route1);
    driveOrder2 = driveOrder2.withRoute(route2);
    driveOrders = List.of(driveOrder1, driveOrder2);

    AssignmentCandidate assignmentCandidate = new AssignmentCandidate(vehicle,
                                                                      transportOrder,
                                                                      driveOrders);

    assertThat(assignmentCandidate.getCompleteRoutingCosts(), comparesEqualTo(6912L));
  }
}
