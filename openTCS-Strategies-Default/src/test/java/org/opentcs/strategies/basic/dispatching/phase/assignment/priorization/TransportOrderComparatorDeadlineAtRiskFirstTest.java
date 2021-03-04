/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import org.junit.*;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorDeadlineAtRiskFirst;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class TransportOrderComparatorDeadlineAtRiskFirstTest {

  private TransportOrderComparatorDeadlineAtRiskFirst comparator;

  private DefaultDispatcherConfiguration configuration;

  @Before
  public void setUp() {
    configuration = Mockito.mock(DefaultDispatcherConfiguration.class);
    when(configuration.deadlineAtRiskPeriod()).thenReturn(Long.valueOf(60 * 60 * 1000));

    comparator = new TransportOrderComparatorDeadlineAtRiskFirst(configuration);
  }

  @Test
  public void sortCriticalDeadlinesUp() {
    TransportOrder plainOrder = new TransportOrder("Some order ", new ArrayList<>());
    TransportOrder order1 = plainOrder.withDeadline(Instant.now().plus(150, ChronoUnit.MINUTES));
    TransportOrder order2 = plainOrder.withDeadline(Instant.now().plus(5, ChronoUnit.MINUTES));
    TransportOrder order3 = plainOrder.withDeadline(Instant.now().plus(170, ChronoUnit.MINUTES));

    List<TransportOrder> list = new ArrayList<>();
    list.add(order1);
    list.add(order2);
    list.add(order3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(order2)));
  }

}
