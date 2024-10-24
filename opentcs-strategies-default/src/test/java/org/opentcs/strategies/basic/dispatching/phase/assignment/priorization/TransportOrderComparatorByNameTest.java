// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByName;

/**
 */
class TransportOrderComparatorByNameTest {

  private TransportOrderComparatorByName comparator;

  @BeforeEach
  void setUp() {
    comparator = new TransportOrderComparatorByName();
  }

  @Test
  void sortsAlphabeticallyByName() {
    TransportOrder order1 = new TransportOrder("AA", List.of());
    TransportOrder order2 = new TransportOrder("CC", List.of());
    TransportOrder order3 = new TransportOrder("AB", List.of());

    List<TransportOrder> list = new ArrayList<>();
    list.add(order1);
    list.add(order2);
    list.add(order3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(order1)));
    assertThat(list.get(1), is(theInstance(order3)));
    assertThat(list.get(2), is(theInstance(order2)));
  }

}
