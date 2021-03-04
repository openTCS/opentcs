/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.data.order.TransportOrder;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class CompositeTransportOrderSelectionVetoTest {

  /**
   * The class to test.
   */
  private CompositeTransportOrderSelectionVeto transportOrderSelectionVeto;

  private List<TransportOrder> transportOrders;

  private static final String NAME_TRANSPORT = "Transport";
  private static final String NAME_ORDER = "Order";
  private static final String NAME_TRANSPORT_ORDER = "TransportOrder";
  private static final String NAME_RANDOM = "SomeRandomName";

  public CompositeTransportOrderSelectionVetoTest() {
  }

  @Before
  public void setUp() {
    transportOrders = new ArrayList<>();
    transportOrders.add(createTransportOrder(NAME_TRANSPORT));
    transportOrders.add(createTransportOrder(NAME_ORDER));
    transportOrders.add(createTransportOrder(NAME_TRANSPORT_ORDER));
    transportOrders.add(createTransportOrder(NAME_RANDOM));
  }

  @Test
  public void shouldVetoAllTransportOrders() {
    Set<TransportOrderSelectionVeto> vetos
        = new HashSet<>(Arrays.asList(new VetoAlways(),
                                      new VetoIfTransportOrderNameContainsTransport(),
                                      new VetoIfTransportOrderNameContainsOrder()));
    transportOrderSelectionVeto = new CompositeTransportOrderSelectionVeto(vetos);

    long remainingTransportOrders = transportOrders.stream()
        .filter(transportOrderSelectionVeto.negate())
        .count();

    assertEquals(0, remainingTransportOrders);
  }

  @Test
  public void shouldVetoTransportOrdersContainingTransport() {
    Set<TransportOrderSelectionVeto> vetos
        = new HashSet<>(Arrays.asList(new VetoIfTransportOrderNameContainsTransport()));
    transportOrderSelectionVeto = new CompositeTransportOrderSelectionVeto(vetos);

    long remainingTransportOrders = transportOrders.stream()
        .filter(transportOrderSelectionVeto.negate())
        .count();

    assertEquals(2, remainingTransportOrders);
  }

  @Test
  public void shouldVetoTransportOrdersContainingOrder() {
    Set<TransportOrderSelectionVeto> vetos
        = new HashSet<>(Arrays.asList(new VetoIfTransportOrderNameContainsOrder()));
    transportOrderSelectionVeto = new CompositeTransportOrderSelectionVeto(vetos);

    long remainingTransportOrders = transportOrders.stream()
        .filter(transportOrderSelectionVeto.negate())
        .count();

    assertEquals(2, remainingTransportOrders);
  }

  @Test
  public void shouldVetoTransportOrdersContainingTransportOrOrder() {
    Set<TransportOrderSelectionVeto> vetos
        = new HashSet<>(Arrays.asList(new VetoIfTransportOrderNameContainsTransport(),
                                      new VetoIfTransportOrderNameContainsOrder()));
    transportOrderSelectionVeto = new CompositeTransportOrderSelectionVeto(vetos);

    List<TransportOrder> remainingTransportOrders = transportOrders.stream()
        .filter(transportOrderSelectionVeto.negate())
        .collect(Collectors.toList());

    assertEquals(1, remainingTransportOrders.size());
    assertEquals(NAME_RANDOM, remainingTransportOrders.get(0).getName());
  }

  private TransportOrder createTransportOrder(String name) {
    return new TransportOrder(name, new ArrayList<>());
  }

  private class VetoAlways
      implements TransportOrderSelectionVeto {

    @Override
    public boolean test(TransportOrder t) {
      return true;
    }
  }

  private class VetoIfTransportOrderNameContainsTransport
      implements TransportOrderSelectionVeto {

    @Override
    public boolean test(TransportOrder t) {
      return t.getName().contains("Transport");
    }
  }

  private class VetoIfTransportOrderNameContainsOrder
      implements TransportOrderSelectionVeto {

    @Override
    public boolean test(TransportOrder t) {
      return t.getName().contains("Order");
    }
  }

}
