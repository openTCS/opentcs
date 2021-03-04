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
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderComparator;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByDeadline;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class CompositeOrderComparatorTest {

  private CompositeOrderComparator comparator;
  private DefaultDispatcherConfiguration configuration;
  private Map<String, Comparator<TransportOrder>> availableComparators;

  @Before
  public void setUp() {
    configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    availableComparators = new HashMap<>();
  }

  @Test
  public void sortNamesUpForOtherwiseEqualInstances() {

    Mockito.when(configuration.orderPriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeOrderComparator(configuration, availableComparators);

    TransportOrder candidate1 = new TransportOrder("AA", new ArrayList<>());
    TransportOrder candidate2 = new TransportOrder("CC", new ArrayList<>());
    TransportOrder candidate3 = new TransportOrder("AB", new ArrayList<>());

    List<TransportOrder> list = new ArrayList<>();
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
    Mockito.when(configuration.orderPriorities())
        .thenReturn(new LinkedList<>());
    comparator = new CompositeOrderComparator(configuration, availableComparators);

    TransportOrder candidate1 = candidateWithNameAndCreationtime("AA", now().minusSeconds(1));
    TransportOrder candidate2 = candidateWithNameAndCreationtime("CC", now().minusSeconds(2));
    TransportOrder candidate3 = candidateWithNameAndCreationtime("BB", now().minusSeconds(2));

    List<TransportOrder> list = new ArrayList<>();
    list.add(candidate1);
    list.add(candidate2);
    list.add(candidate3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(candidate3)));
    assertThat(list.get(1), is(theInstance(candidate2)));
    assertThat(list.get(2), is(theInstance(candidate1)));
  }

  @Test
  public void sortsByAgeAndNameAndDeadline() {
    String deadlineKey = "BY_DEADLINE";
    Mockito.when(configuration.orderPriorities())
        .thenReturn(Arrays.asList(deadlineKey));
    availableComparators.put(deadlineKey,
                             new TransportOrderComparatorByDeadline());

    comparator = new CompositeOrderComparator(configuration, availableComparators);

    TransportOrder candidate1
        = candidateWithNameCreationtimeAndDeadline("AA",
                                                   now().minusSeconds(2),//Creation
                                                   now().plusSeconds(2));//Deadline
    TransportOrder candidate2
        = candidateWithNameCreationtimeAndDeadline("CC",
                                                   now().minusSeconds(2),//Creation
                                                   now().plusSeconds(1));//Deadline
    TransportOrder candidate3
        = candidateWithNameCreationtimeAndDeadline("BB",
                                                   now().minusSeconds(2),//Creation
                                                   now().plusSeconds(2));//Deadline
    TransportOrder candidate4
        = candidateWithNameCreationtimeAndDeadline("DD",
                                                   now().minusSeconds(1),//Creation
                                                   now().plusSeconds(5));//Deadline

    List<TransportOrder> list = new ArrayList<>();
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

  private TransportOrder candidateWithNameAndCreationtime(String ordername,
                                                          Instant creationTime) {
    return new TransportOrder(ordername, new ArrayList<>())
        .withCreationTime(creationTime);
  }

  private TransportOrder candidateWithNameCreationtimeAndDeadline(String ordername,
                                                                  Instant creationTime,
                                                                  Instant deadline) {
    return new TransportOrder(ordername, new ArrayList<>())
        .withCreationTime(creationTime)
        .withDeadline(deadline);
  }

}
