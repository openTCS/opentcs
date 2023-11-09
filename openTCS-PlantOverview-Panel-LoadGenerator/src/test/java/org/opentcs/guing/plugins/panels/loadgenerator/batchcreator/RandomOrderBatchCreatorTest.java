/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator.batchcreator;

import java.util.List;
import java.util.Set;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.TransportOrder;

/**
 * Tests for {@link RandomOrderBatchCreator}.
 */
class RandomOrderBatchCreatorTest {

  private final Point point= new Point("point1");
  private final LocationType unsuitableLocType =  new LocationType("unsuitableLocType")
                                                      .withAllowedOperations(List.of("park"));
  private final LocationType suitableLocType = new LocationType("suitableLocType")
                                                   .withAllowedOperations(List.of("park", "NOP"));
  private final Location unsuitableLoc = new Location("unsuitableLoc",
                                                      unsuitableLocType.getReference());
  private final Location suitableLoc = new Location("suitableLoc",
                                                    suitableLocType.getReference());
  private DispatcherService dispatcherService;
  private TransportOrderService transportOrderService;

  @BeforeEach
  void setUp() {
    dispatcherService = mock(DispatcherService.class);
    transportOrderService = mock(TransportOrderService.class);
  }

  @Test
  void givenUnsuitableTypeWithoutLinkThenCreateNoOrders() {
    when(transportOrderService.fetchObjects(LocationType.class))
        .thenReturn(Set.of(unsuitableLocType));
    when(transportOrderService.fetchObjects(Location.class))
        .thenReturn(Set.of(unsuitableLoc));
    RandomOrderBatchCreator batchCreator = new RandomOrderBatchCreator(transportOrderService,
                                                                       dispatcherService,
                                                                       10,
                                                                       3);

    Set<TransportOrder> result = batchCreator.createOrderBatch();

    assertThat(result, is(empty()));
    verify(transportOrderService, never())
        .createTransportOrder(any(TransportOrderCreationTO.class));
  }

  @Test
  void givenSuitableTypeWithoutLinkThenCreateNoOrders() {
    when(transportOrderService.fetchObjects(LocationType.class))
        .thenReturn(Set.of(suitableLocType));
    when(transportOrderService.fetchObjects(Location.class))
        .thenReturn(Set.of(suitableLoc));
    RandomOrderBatchCreator batchCreator = new RandomOrderBatchCreator(transportOrderService,
                                                                       dispatcherService,
                                                                       10,
                                                                       3);

    Set<TransportOrder> result = batchCreator.createOrderBatch();

    assertThat(result, is(empty()));
    verify(transportOrderService, never())
        .createTransportOrder(any(TransportOrderCreationTO.class));
  }

  @Test
  void givenUnsuitableTypeWithLinkThenCreateNoOrders() {
    Location.Link unsuitableLink = new Location.Link(unsuitableLoc.getReference(),
                                                     point.getReference());
    when(transportOrderService.fetchObjects(LocationType.class))
        .thenReturn(Set.of(unsuitableLocType));
    when(transportOrderService.fetchObjects(Location.class))
        .thenReturn(Set.of(unsuitableLoc.withAttachedLinks(Set.of(unsuitableLink))));
    RandomOrderBatchCreator batchCreator = new RandomOrderBatchCreator(transportOrderService,
                                                                       dispatcherService,
                                                                       10,
                                                                       3);

    Set<TransportOrder> result = batchCreator.createOrderBatch();

    assertThat(result, is(empty()));
    verify(transportOrderService, never())
        .createTransportOrder(any(TransportOrderCreationTO.class));
  }

  @Test
  void givenSuitableTypeWithLinkThenCreateOrders() {
    Location.Link suitableLink = new Location.Link(suitableLoc.getReference(),
                                                   point.getReference());
    when(transportOrderService.fetchObjects(LocationType.class))
        .thenReturn(Set.of(suitableLocType));
    when(transportOrderService.fetchObjects(Location.class))
        .thenReturn(Set.of(suitableLoc.withAttachedLinks(Set.of(suitableLink))));
    when(transportOrderService.createTransportOrder(any(TransportOrderCreationTO.class)))
        .thenAnswer(invocation -> new TransportOrder(randomUUID().toString(),
                                                     List.of()));
    RandomOrderBatchCreator batchCreator = new RandomOrderBatchCreator(transportOrderService,
                                                                       dispatcherService,
                                                                       10,
                                                                       3);

    Set<TransportOrder> result = batchCreator.createOrderBatch();

    assertThat(result, hasSize(10));
    verify(transportOrderService, times(10))
        .createTransportOrder(any(TransportOrderCreationTO.class));
  }
}
