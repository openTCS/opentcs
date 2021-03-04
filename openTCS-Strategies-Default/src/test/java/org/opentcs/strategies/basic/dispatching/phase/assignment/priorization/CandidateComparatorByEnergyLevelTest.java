/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import org.junit.*;
import static org.junit.Assert.assertThat;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByEnergyLevel;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class CandidateComparatorByEnergyLevelTest {

  private CandidateComparatorByEnergyLevel comparator;

  @Before
  public void setUp() {
    comparator = new CandidateComparatorByEnergyLevel();
  }

  @Test
  public void sortHighEnergyCandidatesUp() {
    AssignmentCandidate candidate1 = candidateWithVehicleEnergyLevel(10);
    AssignmentCandidate candidate2 = candidateWithVehicleEnergyLevel(50);
    AssignmentCandidate candidate3 = candidateWithVehicleEnergyLevel(30);

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate2)));
    assertThat(list.get(1), is(theInstance(candidate3)));
    assertThat(list.get(2), is(theInstance(candidate1)));

  }

  private AssignmentCandidate candidateWithVehicleEnergyLevel(int vehicleEnergy) {
    TransportOrder plainOrder = new TransportOrder("TOrder1", new ArrayList<>());
    Route.Step dummyStep = new Route.Step(null, null, new Point("Point-001"), Vehicle.Orientation.FORWARD, 1);
    Route route = new Route(Arrays.asList(dummyStep), 10);
    List<DriveOrder> driveOrders = new LinkedList<>();
    driveOrders.add(new DriveOrder(new DriveOrder.Destination(new Point("Point-001").getReference()))
        .withRoute(route));

    return new AssignmentCandidate(new Vehicle("EnergyVehicle").withEnergyLevel(vehicleEnergy),
                                   plainOrder,
                                   driveOrders);
  }

}
