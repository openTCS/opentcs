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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import org.junit.*;
import static org.junit.Assert.assertThat;
import org.mockito.Mockito;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeVehicleCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByInitialRoutingCosts;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class CompositeVehicleCandidateComparatorTest {

  private CompositeVehicleCandidateComparator comparator;
  private DefaultDispatcherConfiguration configuration;
  private Map<String, Comparator<AssignmentCandidate>> availableComparators;

  @Before
  public void setUp() {
    configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    availableComparators = new HashMap<>();
  }

  @Test
  public void sortNamesUpForOtherwiseEqualInstances() {

    Mockito.when(configuration.vehicleCandidatePriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeVehicleCandidateComparator(configuration, availableComparators);

    AssignmentCandidate candidate1 = candidateWithName("AA");
    AssignmentCandidate candidate2 = candidateWithName("CC");
    AssignmentCandidate candidate3 = candidateWithName("AB");

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate1)));
    assertThat(list.get(1), is(theInstance(candidate3)));
    assertThat(list.get(2), is(theInstance(candidate2)));
  }

  @Test
  public void sortsByNameAndEnergylevel() {
    Mockito.when(configuration.vehicleCandidatePriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeVehicleCandidateComparator(configuration, availableComparators);

    AssignmentCandidate candidate1 = candidateWithNameEnergylevel("AA", 1);
    AssignmentCandidate candidate2 = candidateWithNameEnergylevel("CC", 2);
    AssignmentCandidate candidate3 = candidateWithNameEnergylevel("BB", 2);

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate3)));
    assertThat(list.get(1), is(theInstance(candidate2)));
    assertThat(list.get(2), is(theInstance(candidate1)));
  }

  @Test
  public void sortsByNameAndRoutingCostAndEnergyLevel() {
    String initRoutingCostKey = "BY_INITIAL_ROUTING_COSTS";
    Mockito.when(configuration.vehicleCandidatePriorities())
        .thenReturn(Arrays.asList(initRoutingCostKey));
    availableComparators.put(initRoutingCostKey,
                             new CandidateComparatorByInitialRoutingCosts());

    comparator = new CompositeVehicleCandidateComparator(configuration, availableComparators);

    AssignmentCandidate candidate1 = candidateWithNameEnergylevelInitialRoutingCosts("AA", 3, 60);
    AssignmentCandidate candidate2 = candidateWithNameEnergylevelInitialRoutingCosts("CC", 2, 60);
    AssignmentCandidate candidate3 = candidateWithNameEnergylevelInitialRoutingCosts("BB", 1, 20);
    AssignmentCandidate candidate4 = candidateWithNameEnergylevelInitialRoutingCosts("DD", 1, 20);

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);
    list.add(candidate4);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate3)));
    assertThat(list.get(1), is(theInstance(candidate4)));
    assertThat(list.get(2), is(theInstance(candidate1)));
    assertThat(list.get(3), is(theInstance(candidate2)));
  }

  private AssignmentCandidate candidateWithName(String name) {
    TransportOrder trasportOrder = new TransportOrder("TOrder-1", new ArrayList<>());
    return new AssignmentCandidate(new Vehicle(name),
                                   trasportOrder,
                                   buildDriveOrders(10));
  }

  private AssignmentCandidate candidateWithNameEnergylevel(String name,
                                                           int energyLevel) {
    TransportOrder trasportOrder = new TransportOrder("TOrder-1", new ArrayList<>());
    return new AssignmentCandidate(new Vehicle(name).withEnergyLevel(energyLevel),
                                   trasportOrder,
                                   buildDriveOrders(10));
  }

  private AssignmentCandidate candidateWithNameEnergylevelInitialRoutingCosts(String name,
                                                                              int energyLevel,
                                                                              long routingCost) {
    TransportOrder trasportOrder = new TransportOrder("TOrder-1", new ArrayList<>());
    return new AssignmentCandidate(new Vehicle(name).withEnergyLevel(energyLevel),
                                   trasportOrder,
                                   buildDriveOrders(routingCost));
  }

  private List<DriveOrder> buildDriveOrders(long routingCost) {
    Route.Step dummyStep
        = new Route.Step(null, null, new Point("Point1"), Vehicle.Orientation.FORWARD, 1);
    Route route = new Route(Arrays.asList(dummyStep), routingCost);
    List<DriveOrder> driveOrders = new LinkedList<>();
    driveOrders.add(new DriveOrder(new DriveOrder.Destination(new Point("Point2").getReference()))
        .withRoute(route));
    return driveOrders;
  }

}
