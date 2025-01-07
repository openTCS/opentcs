// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByOrderTypePriority;

/**
 * Tests for {@link CandidateComparatorByOrderTypePriority}.
 */
public class CandidateComparatorByOrderTypePriorityTest {

  private CandidateComparatorByOrderTypePriority comparator;

  @BeforeEach
  void setUp() {
    comparator = new CandidateComparatorByOrderTypePriority();
  }

  @Test
  void sortCandidatesAccordingToBestSuitedVehicle() {
    // Here, vehicleB is best suited because it matches the transport order's type, and it has the
    // highest priority.
    Vehicle vehicleA = new Vehicle("vehicle-a")
        .withAcceptableOrderTypes(Set.of(new AcceptableOrderType("some-type", 1)));
    Vehicle vehicleB = new Vehicle("vehicle-b")
        .withAcceptableOrderTypes(Set.of(new AcceptableOrderType("some-type", 0)));
    Vehicle vehicleC = new Vehicle("vehicle-c")
        .withAcceptableOrderTypes(Set.of(new AcceptableOrderType("some-other-type", -1)));
    TransportOrder transportOrder = new TransportOrder("transport-order", List.of())
        .withType("some-type");

    AssignmentCandidate candidateA
        = new AssignmentCandidate(vehicleA, transportOrder, nonEmptyDummyDriveOrders());
    AssignmentCandidate candidateB
        = new AssignmentCandidate(vehicleB, transportOrder, nonEmptyDummyDriveOrders());
    AssignmentCandidate candidateC
        = new AssignmentCandidate(vehicleC, transportOrder, nonEmptyDummyDriveOrders());

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidateA);
    list.add(candidateB);
    list.add(candidateC);

    list.sort(comparator);

    assertThat(list.get(0), is(candidateB));
    assertThat(list.get(1), is(candidateA));
    assertThat(list.get(2), is(candidateC));
  }

  @Test
  void sortCandidatesAccordingToBestSuitedTransportOrder() {
    // Here, transportOrderC is best suited because for this type the vehicle has the highest
    // priority configured.
    Vehicle vehicle = new Vehicle("vehicle")
        .withAcceptableOrderTypes(
            Set.of(
                new AcceptableOrderType("type-with-high-priority", -1),
                new AcceptableOrderType("type-with-medium-priority", 0),
                new AcceptableOrderType("type-with-low-priority", 1)
            )
        );
    TransportOrder transportOrderA = new TransportOrder("transport-order-a", List.of())
        .withType("type-with-medium-priority");
    TransportOrder transportOrderB = new TransportOrder("transport-order-b", List.of())
        .withType("type-with-low-priority");
    TransportOrder transportOrderC = new TransportOrder("transport-order-c", List.of())
        .withType("type-with-high-priority");

    AssignmentCandidate candidateA
        = new AssignmentCandidate(vehicle, transportOrderA, nonEmptyDummyDriveOrders());
    AssignmentCandidate candidateB
        = new AssignmentCandidate(vehicle, transportOrderB, nonEmptyDummyDriveOrders());
    AssignmentCandidate candidateC
        = new AssignmentCandidate(vehicle, transportOrderC, nonEmptyDummyDriveOrders());

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidateA);
    list.add(candidateB);
    list.add(candidateC);

    list.sort(comparator);

    assertThat(list.get(0), is(candidateC));
    assertThat(list.get(1), is(candidateA));
    assertThat(list.get(2), is(candidateB));
  }

  @Test
  void sortCandidatesAccordingToTheAnyTypePriority() {
    // Here, only the "any" type matches and vehicleA has the highest priority configured.
    Vehicle vehicleA = new Vehicle("vehicle-a")
        .withAcceptableOrderTypes(
            Set.of(
                new AcceptableOrderType("some-type", 1),
                new AcceptableOrderType(OrderConstants.TYPE_ANY, -1)
            )
        );
    Vehicle vehicleB = new Vehicle("vehicle-b")
        .withAcceptableOrderTypes(
            Set.of(
                new AcceptableOrderType("some-type", 0),
                new AcceptableOrderType(OrderConstants.TYPE_ANY, 0)
            )
        );
    TransportOrder transportOrder = new TransportOrder("transport-order", List.of())
        .withType("some-other-type");

    AssignmentCandidate candidateA
        = new AssignmentCandidate(vehicleA, transportOrder, nonEmptyDummyDriveOrders());
    AssignmentCandidate candidateB
        = new AssignmentCandidate(vehicleB, transportOrder, nonEmptyDummyDriveOrders());

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidateA);
    list.add(candidateB);

    list.sort(comparator);

    assertThat(list.get(0), is(candidateA));
    assertThat(list.get(1), is(candidateB));
  }

  @Test
  void ignoreTheAnyTypeIfThereIsAMatchingType() {
    // Here, the type that matches the one of the transport order should have precedence over the
    // "any" type.
    Vehicle vehicleA = new Vehicle("vehicle-a")
        .withAcceptableOrderTypes(
            Set.of(
                new AcceptableOrderType("some-type", 1),
                new AcceptableOrderType(OrderConstants.TYPE_ANY, -1)
            )
        );
    Vehicle vehicleB = new Vehicle("vehicle-b")
        .withAcceptableOrderTypes(Set.of(new AcceptableOrderType("some-type", 0)));
    TransportOrder transportOrder = new TransportOrder("transport-order", List.of())
        .withType("some-type");

    AssignmentCandidate candidateA
        = new AssignmentCandidate(vehicleA, transportOrder, nonEmptyDummyDriveOrders());
    AssignmentCandidate candidateB
        = new AssignmentCandidate(vehicleB, transportOrder, nonEmptyDummyDriveOrders());

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidateA);
    list.add(candidateB);

    list.sort(comparator);

    assertThat(list.get(0), is(candidateB));
    assertThat(list.get(1), is(candidateA));
  }

  @Test
  void considerTheAnyTypeIfItIsTheOnlyMatchingType() {
    // Here, the "any" type (with the highest priority) should have precedence over the matching
    // type (with the lowest priority).
    Vehicle vehicleA = new Vehicle("vehicle-a")
        .withAcceptableOrderTypes(
            Set.of(
                new AcceptableOrderType("some-type", 1)
            )
        );
    Vehicle vehicleB = new Vehicle("vehicle-b")
        .withAcceptableOrderTypes(
            Set.of(
                new AcceptableOrderType("some-other-type", 0),
                new AcceptableOrderType(OrderConstants.TYPE_ANY, 0)
            )
        );
    TransportOrder transportOrder = new TransportOrder("transport-order", List.of())
        .withType("some-type");

    AssignmentCandidate candidateA
        = new AssignmentCandidate(vehicleA, transportOrder, nonEmptyDummyDriveOrders());
    AssignmentCandidate candidateB
        = new AssignmentCandidate(vehicleB, transportOrder, nonEmptyDummyDriveOrders());

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidateA);
    list.add(candidateB);

    list.sort(comparator);

    assertThat(list.get(0), is(candidateB));
    assertThat(list.get(1), is(candidateA));
  }

  private List<DriveOrder> nonEmptyDummyDriveOrders() {
    Point destPoint = new Point("some-point");
    Route.Step dummyStep
        = new Route.Step(null, null, destPoint, Vehicle.Orientation.FORWARD, 1, 10);
    Route dummyRoute = new Route(List.of(dummyStep));
    return List.of(
        new DriveOrder(new DriveOrder.Destination(destPoint.getReference())).withRoute(dummyRoute)
    );
  }
}
