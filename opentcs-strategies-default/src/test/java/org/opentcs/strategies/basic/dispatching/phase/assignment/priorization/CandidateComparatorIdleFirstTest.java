// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorIdleFirst;

/**
 */
class CandidateComparatorIdleFirstTest {

  private CandidateComparatorIdleFirst comparator;

  @BeforeEach
  void setUp() {
    comparator = new CandidateComparatorIdleFirst();
  }

  @Test
  void sortVehiclesIdleFirst() {
    AssignmentCandidate candidate1 = candidateWithVehicleState(Vehicle.State.CHARGING);
    AssignmentCandidate candidate2 = candidateWithVehicleState(Vehicle.State.IDLE);
    AssignmentCandidate candidate3 = candidateWithVehicleState(Vehicle.State.CHARGING);

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate2)));
    assertThat(list.subList(1, 3), contains(candidate1, candidate3));
  }

  private AssignmentCandidate candidateWithVehicleState(Vehicle.State vehicleState) {
    TransportOrder transportOrder = new TransportOrder("TOrder1", new ArrayList<>());
    Route.Step dummyStep
        = new Route.Step(
            null,
            new Point("Point1"),
            new Point("Point2"),
            Vehicle.Orientation.FORWARD,
            1,
            10
        );
    Route route = new Route(Arrays.asList(dummyStep));
    List<DriveOrder> driveOrders = List.of(
        new DriveOrder("order1", new DriveOrder.Destination(new Point("Point2").getReference()))
            .withRoute(route)
    );

    return new AssignmentCandidate(
        new Vehicle("Vehicle1").withState(vehicleState),
        transportOrder,
        driveOrders
    );
  }
}
