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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.selection.TransportOrderSelectionFilter;
import org.opentcs.strategies.basic.dispatching.selection.orders.CompositeTransportOrderSelectionFilter;

/**
 * Unit tests for {@link CompositeTransportOrderSelectionFilter}.
 */
class CompositeTransportOrderSelectionFilterTest {

  private static final String NAME_TRANSPORT = "Transport";
  private static final String NAME_ORDER = "Order";
  private static final String NAME_TRANSPORT_ORDER = "TransportOrder";
  private static final String NAME_RANDOM = "SomeRandomName";

  /**
   * The class to test.
   */
  private CompositeTransportOrderSelectionFilter transportOrderSelectionFilter;

  private List<TransportOrder> transportOrders;

  CompositeTransportOrderSelectionFilterTest() {
  }

  @BeforeEach
  void setUp() {
    transportOrders = new ArrayList<>();
    transportOrders.add(createTransportOrder(NAME_TRANSPORT));
    transportOrders.add(createTransportOrder(NAME_ORDER));
    transportOrders.add(createTransportOrder(NAME_TRANSPORT_ORDER));
    transportOrders.add(createTransportOrder(NAME_RANDOM));
  }

  @Test
  void shouldFilterNoTransportOrders() {
    Set<TransportOrderSelectionFilter> filters
        = new HashSet<>(Arrays.asList(new RefuseAllFilter(),
                                      new FilterIfTransportOrderNameContainsTransport(),
                                      new FilterIfTransportOrderNameContainsOrder()));
    transportOrderSelectionFilter = new CompositeTransportOrderSelectionFilter(filters);

    long remainingTransportOrders = transportOrders.stream()
        .filter(order -> transportOrderSelectionFilter.apply(order).isEmpty())
        .count();

    assertEquals(0, remainingTransportOrders);
  }

  @Test
  void shouldFilterTransportOrdersContainingTransport() {
    Set<TransportOrderSelectionFilter> filters
        = new HashSet<>(Arrays.asList(new FilterIfTransportOrderNameContainsTransport()));
    transportOrderSelectionFilter = new CompositeTransportOrderSelectionFilter(filters);

    long remainingTransportOrders = transportOrders.stream()
        .filter(order -> transportOrderSelectionFilter.apply(order).isEmpty())
        .count();

    assertEquals(2, remainingTransportOrders);
  }

  @Test
  void shouldFilterTransportOrdersContainingOrder() {
    Set<TransportOrderSelectionFilter> filters
        = new HashSet<>(Arrays.asList(new FilterIfTransportOrderNameContainsOrder()));
    transportOrderSelectionFilter = new CompositeTransportOrderSelectionFilter(filters);

    long remainingTransportOrders = transportOrders.stream()
        .filter(order -> transportOrderSelectionFilter.apply(order).isEmpty())
        .count();

    assertEquals(2, remainingTransportOrders);
  }

  @Test
  void shouldFilterTransportOrdersContainingTransportOrOrder() {
    Set<TransportOrderSelectionFilter> filters
        = new HashSet<>(Arrays.asList(new FilterIfTransportOrderNameContainsTransport(),
                                      new FilterIfTransportOrderNameContainsOrder()));
    transportOrderSelectionFilter = new CompositeTransportOrderSelectionFilter(filters);

    List<TransportOrder> remainingTransportOrders = transportOrders.stream()
        .filter(order -> !transportOrderSelectionFilter.apply(order).isEmpty())
        .collect(Collectors.toList());

    assertEquals(3, remainingTransportOrders.size());
  }

  private TransportOrder createTransportOrder(String name) {
    return new TransportOrder(name, new ArrayList<>());
  }

  private class RefuseAllFilter
      implements TransportOrderSelectionFilter {

    @Override
    public Collection<String> apply(TransportOrder t) {
      return Arrays.asList("just no");
    }
  }

  private class FilterIfTransportOrderNameContainsTransport
      implements TransportOrderSelectionFilter {

    @Override
    public Collection<String> apply(TransportOrder t) {
      return t.getName().contains("Transport")
          ? new ArrayList<>()
          : Arrays.asList("order name does not contain 'Transport'");
    }
  }

  private class FilterIfTransportOrderNameContainsOrder
      implements TransportOrderSelectionFilter {

    @Override
    public Collection<String> apply(TransportOrder t) {
      return t.getName().contains("Order")
          ? new ArrayList<>()
          : Arrays.asList("order name does not contain 'Order'");
    }
  }

}
