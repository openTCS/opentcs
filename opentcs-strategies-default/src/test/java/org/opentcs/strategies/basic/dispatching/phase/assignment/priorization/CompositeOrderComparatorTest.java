// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.CompositeOrderComparator;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByDeadline;

/**
 * Unit tests for {@link CompositeOrderComparator}.
 */
class CompositeOrderComparatorTest {

  private CompositeOrderComparator comparator;
  private DefaultDispatcherConfiguration configuration;
  private Map<String, Comparator<TransportOrder>> availableComparators;

  @BeforeEach
  void setUp() {
    configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    availableComparators = new HashMap<>();
  }

  @Test
  void sortNamesUpForOtherwiseEqualInstances() {

    Mockito.when(configuration.orderPriorities())
        .thenReturn(List.of());
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
  void sortsByAgeAndName() {
    Mockito.when(configuration.orderPriorities())
        .thenReturn(List.of());
    comparator = new CompositeOrderComparator(configuration, availableComparators);

    Instant creationTime = Instant.now();
    TransportOrder candidate1 = candidateWithNameAndCreationtime(
        "AA",
        creationTime.minusSeconds(1)
    );
    TransportOrder candidate2 = candidateWithNameAndCreationtime(
        "CC",
        creationTime.minusSeconds(2)
    );
    TransportOrder candidate3 = candidateWithNameAndCreationtime(
        "BB",
        creationTime.minusSeconds(2)
    );

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
  void sortsByAgeAndNameAndDeadline() {
    String deadlineKey = "BY_DEADLINE";
    Mockito.when(configuration.orderPriorities())
        .thenReturn(List.of(deadlineKey));
    availableComparators.put(
        deadlineKey,
        new TransportOrderComparatorByDeadline()
    );

    comparator = new CompositeOrderComparator(configuration, availableComparators);

    Instant currentTime = Instant.now();
    TransportOrder candidate1
        = candidateWithNameCreationtimeAndDeadline(
            "AA",
            currentTime.minusSeconds(2),//Creation
            currentTime.plusSeconds(2)
        );//Deadline
    TransportOrder candidate2
        = candidateWithNameCreationtimeAndDeadline(
            "CC",
            currentTime.minusSeconds(2),//Creation
            currentTime.plusSeconds(1)
        );//Deadline
    TransportOrder candidate3
        = candidateWithNameCreationtimeAndDeadline(
            "BB",
            currentTime.minusSeconds(2),//Creation
            currentTime.plusSeconds(2)
        );//Deadline
    TransportOrder candidate4
        = candidateWithNameCreationtimeAndDeadline(
            "DD",
            currentTime.minusSeconds(1),//Creation
            currentTime.plusSeconds(5)
        );//Deadline

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

  private TransportOrder candidateWithNameAndCreationtime(
      String ordername,
      Instant creationTime
  ) {
    return new TransportOrder(ordername, new ArrayList<>())
        .withCreationTime(creationTime);
  }

  private TransportOrder candidateWithNameCreationtimeAndDeadline(
      String ordername,
      Instant creationTime,
      Instant deadline
  ) {
    return new TransportOrder(ordername, new ArrayList<>())
        .withCreationTime(creationTime)
        .withDeadline(deadline);
  }

}
