/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import org.junit.*;
import static org.junit.Assert.assertThat;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.priorization.transportorder.TransportOrderComparatorByName;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class TransportOrderComparatorByNameTest {

  private TransportOrderComparatorByName comparator;

  @Before
  public void setUp() {
    comparator = new TransportOrderComparatorByName();
  }

  @Test
  public void sortsAlphabeticallyByName() {
    TransportOrder order1 = new TransportOrder("AA", new LinkedList<>());
    TransportOrder order2 = new TransportOrder("CC", new LinkedList<>());
    TransportOrder order3 = new TransportOrder("AB", new LinkedList<>());

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
