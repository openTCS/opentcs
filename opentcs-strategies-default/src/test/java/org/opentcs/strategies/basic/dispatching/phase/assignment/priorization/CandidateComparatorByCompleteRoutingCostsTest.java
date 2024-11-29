// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import static org.hamcrest.MatcherAssert.assertThat;
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
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByCompleteRoutingCosts;

/**
 */
class CandidateComparatorByCompleteRoutingCostsTest {

  private CandidateComparatorByCompleteRoutingCosts comparator;

  @BeforeEach
  void setUp() {
    comparator = new CandidateComparatorByCompleteRoutingCosts();
  }

  @Test
  void sortCheapCandidatesUp() {
    AssignmentCandidate candidate1 = candidateWithRoutingCost(10);
    AssignmentCandidate candidate2 = candidateWithRoutingCost(50);
    AssignmentCandidate candidate3 = candidateWithRoutingCost(30);

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate1)));
    assertThat(list.get(1), is(theInstance(candidate3)));
    assertThat(list.get(2), is(theInstance(candidate2)));

  }

  private AssignmentCandidate candidateWithRoutingCost(long cost) {
    TransportOrder trasportOrder = new TransportOrder("TOrder1", new ArrayList<>());
    Route.Step dummyStep = new Route.Step(
        null,
        new Point("Point1"),
        new Point("Point2"),
        Vehicle.Orientation.FORWARD,
        1,
        cost
    );
    Route route = new Route(Arrays.asList(dummyStep));
    List<DriveOrder> driveOrders = List.of(
        new DriveOrder(new DriveOrder.Destination(new Point("Point2").getReference()))
            .withRoute(route)
    );

    return new AssignmentCandidate(new Vehicle("Vehicle1"), trasportOrder, driveOrders);
  }

}
