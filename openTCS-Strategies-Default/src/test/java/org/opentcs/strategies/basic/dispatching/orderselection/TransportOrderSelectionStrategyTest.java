/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.CompositeTransportOrderSelectionVeto;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.TransportOrderSelectionVeto;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.opentcs.util.Comparators;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class TransportOrderSelectionStrategyTest {

  /**
   * The class to test.
   */
  private TransportOrderSelectionStrategy orderSelectionStrategy;

  private LocalKernel kernel;

  private Router router;

  private ProcessabilityChecker processabilityChecker;

  private CompositeTransportOrderSelectionVeto transportOrderSelectionVeto;

  public TransportOrderSelectionStrategyTest() {
  }

  @Before
  public void setUp() {
    kernel = mock(LocalKernel.class);
    router = mock(Router.class);
    processabilityChecker = mock(ProcessabilityChecker.class);
    Set<TransportOrderSelectionVeto> vetos
        = new HashSet<>(Arrays.asList(new VetoIfTransportOrderUnavailable()));
    transportOrderSelectionVeto = new CompositeTransportOrderSelectionVeto(vetos);
    orderSelectionStrategy = new TransportOrderSelectionStrategy(kernel,
                                                                 router,
                                                                 processabilityChecker,
                                                                 mock(OrderReservationPool.class),
                                                                 Comparators.ordersByDeadline(),
                                                                 transportOrderSelectionVeto);
  }

  @Test
  public void shouldReturnNoOrder() {
    TransportOrder unavailableOrder = createTransportOrder("UnavailableOrder");
    Set<TransportOrder> orders = new HashSet<>(Arrays.asList(unavailableOrder));

    when(kernel.getTCSObjects(TransportOrder.class)).thenReturn(orders);
    when(kernel.getTCSObjects(OrderSequence.class)).thenReturn(new HashSet<>());

    VehicleOrderSelection result = orderSelectionStrategy.selectOrder(new Vehicle("VehicleName"));

    assertNull(result);
  }

  @Test
  public void shouldReturnAvailableOrder() {
    TransportOrder availableOrder = createTransportOrder("AvailableOrder");
    TransportOrder unavailableOrder = createTransportOrder("UnavailableOrder");
    Set<TransportOrder> orders = new HashSet<>(Arrays.asList(availableOrder, unavailableOrder));

    when(kernel.getTCSObjects(TransportOrder.class)).thenReturn(orders);
    when(kernel.getTCSObjects(OrderSequence.class)).thenReturn(new HashSet<>());
    when(router.getRoute(any(Vehicle.class), nullable(Point.class), eq(availableOrder)))
        .thenReturn(Optional.of(new ArrayList<>()));
    when(processabilityChecker.checkProcessability(any(Vehicle.class), eq(availableOrder)))
        .thenReturn(true);

    VehicleOrderSelection result = orderSelectionStrategy.selectOrder(new Vehicle("VehicleName"));

    assertNotNull(result);
    assertEquals(availableOrder, result.getTransportOrder());
  }

  private TransportOrder createTransportOrder(String name) {
    return new TransportOrder(name, new ArrayList<>())
        .withState(TransportOrder.State.DISPATCHABLE);
  }

  private class VetoIfTransportOrderUnavailable
      implements TransportOrderSelectionVeto {

    @Override
    public boolean test(TransportOrder t) {
      return t.getName().contains("Unavailable");
    }
  }
}
