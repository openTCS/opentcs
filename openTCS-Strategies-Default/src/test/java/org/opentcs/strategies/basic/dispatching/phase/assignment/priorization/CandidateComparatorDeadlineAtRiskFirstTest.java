/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.time.Instant;
import static java.time.Instant.now;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import org.junit.*;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.AssignmentCandidate;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.candidate.CandidateComparatorDeadlineAtRiskFirst;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorDeadlineAtRiskFirst;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class CandidateComparatorDeadlineAtRiskFirstTest {

  private CandidateComparatorDeadlineAtRiskFirst comparator;

  @Before
  public void setUp() {
    DefaultDispatcherConfiguration configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    Mockito.when(configuration.deadlineAtRiskPeriod()).thenReturn(Long.valueOf(60 * 60 * 1000));

    this.comparator = new CandidateComparatorDeadlineAtRiskFirst(
        new TransportOrderComparatorDeadlineAtRiskFirst(configuration)
    );
  }

  @Test
  public void sortCriticalDeadlinesUp() {
    AssignmentCandidate candidate1 = candidateWithDeadline(now().plus(270, ChronoUnit.MINUTES));
    AssignmentCandidate candidate2 = candidateWithDeadline(now().plus(30, ChronoUnit.MINUTES));
    AssignmentCandidate candidate3 = candidateWithDeadline(now().plus(180, ChronoUnit.MINUTES));

    List<AssignmentCandidate> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate2)));
  }

  private AssignmentCandidate candidateWithDeadline(Instant time) {
    TransportOrder deadlinedOrder
        = new TransportOrder("Some order", new ArrayList<>()).withDeadline(time);
    Route.Step dummyStep = new Route.Step(null,
                                          null,
                                          new Point("Point1"),
                                          Vehicle.Orientation.FORWARD,
                                          1);
    Route route = new Route(Arrays.asList(dummyStep), 10);
    List<DriveOrder> driveOrders = Arrays.asList(
        new DriveOrder(new DriveOrder.Destination(new Point("Point2").getReference()))
            .withRoute(route)
    );

    return new AssignmentCandidate(new Vehicle("Vehicle1"),
                                   deadlinedOrder,
                                   driveOrders);
  }

  private TransportOrderComparatorDeadlineAtRiskFirst createOrderComparator() {
    DefaultDispatcherConfiguration configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    Mockito.when(configuration.deadlineAtRiskPeriod()).thenReturn(Long.valueOf(10));
    return new TransportOrderComparatorDeadlineAtRiskFirst(configuration);
  }
}
