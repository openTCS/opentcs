/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.time.Instant;
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
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderCandidateComparator;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorByDeadline;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class CompositeOrderCandidateComparatorTest {

  private CompositeOrderCandidateComparator comparator;
  private DefaultDispatcherConfiguration configuration;
  private Map<String, Comparator<AssignmentCandidate>> availableComparators;

  @Before
  public void setUp() {
    configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    availableComparators = new HashMap<>();

  }

  @Test
  public void sortAlphabeticallyForOtherwiseEqualInstances() {

    Mockito.when(configuration.orderCandidatePriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeOrderCandidateComparator(configuration, availableComparators);

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
  public void sortsByAgeAndName() {
    Mockito.when(configuration.orderCandidatePriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeOrderCandidateComparator(configuration, availableComparators);

    AssignmentCandidate candidate1 = candidateWithNameAndCreationtime("AA", 2);
    AssignmentCandidate candidate2 = candidateWithNameAndCreationtime("CC", 1);
    AssignmentCandidate candidate3 = candidateWithNameAndCreationtime("BB", 1);

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
  public void sortsByAgeAndNameAndInitialRoutingCost() {
    String deadlineKey = "BY_DEADLINE";
    Mockito.when(configuration.orderCandidatePriorities())
        .thenReturn(Arrays.asList(deadlineKey));
    availableComparators.put(deadlineKey,
                             new CandidateComparatorByDeadline());
    comparator = new CompositeOrderCandidateComparator(configuration, availableComparators);

    AssignmentCandidate candidate1 = candidateWithNameCreationtimeAndDeadline("AA", 3, 20);
    AssignmentCandidate candidate2 = candidateWithNameCreationtimeAndDeadline("CC", 2, 20);
    AssignmentCandidate candidate3 = candidateWithNameCreationtimeAndDeadline("BB", 1, 60);
    AssignmentCandidate candidate4 = candidateWithNameCreationtimeAndDeadline("DD", 1, 60);

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);
    list.add(candidate4);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate2)));
    assertThat(list.get(1), is(theInstance(candidate1)));
    assertThat(list.get(2), is(theInstance(candidate3)));
    assertThat(list.get(3), is(theInstance(candidate4)));
  }

  private AssignmentCandidate candidateWithName(String ordername) {
    TransportOrder trasportOrder = new TransportOrder(ordername, new ArrayList<>());
    return new AssignmentCandidate(new Vehicle("Vehicle1"), trasportOrder, buildPlainDriveOrders());
  }

  private AssignmentCandidate candidateWithNameAndCreationtime(String ordername,
                                                               long creationTime) {
    TransportOrder trasportOrder = new TransportOrder(ordername, new ArrayList<>())
        .withCreationTime(Instant.ofEpochMilli(creationTime));
    return new AssignmentCandidate(new Vehicle("Vehicle1"), trasportOrder, buildPlainDriveOrders());
  }

  private AssignmentCandidate candidateWithNameCreationtimeAndDeadline(String ordername,
                                                                       long creationTime,
                                                                       long deadline) {
    TransportOrder trasportOrder = new TransportOrder(ordername, new ArrayList<>())
        .withCreationTime(Instant.ofEpochMilli(creationTime))
        .withDeadline(Instant.ofEpochMilli(deadline));
    return new AssignmentCandidate(new Vehicle("Vehicle1"),
                                   trasportOrder,
                                   buildPlainDriveOrders());
  }

  private List<DriveOrder> buildPlainDriveOrders() {
    Route.Step dummyStep
        = new Route.Step(null, null, new Point("Point1"), Vehicle.Orientation.FORWARD, 1);
    Route route = new Route(Arrays.asList(dummyStep), 10);
    List<DriveOrder> driveOrders = new LinkedList<>();
    driveOrders.add(new DriveOrder(new DriveOrder.Destination(new Point("Point2").getReference()))
        .withRoute(route));
    return driveOrders;
  }

}
