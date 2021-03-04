/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.model.Location;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.TransportOrder;

/**
 * Randomly creates batches of transport orders.
 * Destinations and operations chosen are random and not guaranteed to work in
 * a real plant.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class RandomOrderBatchCreator
    implements OrderBatchCreator {

  /**
   * The transport order sergice we talk to.
   */
  private final TransportOrderService transportOrderService;
  /**
   * The dispatcher service.
   */
  private final DispatcherService dispatcherService;
  /**
   * The number of transport orders per batch.
   */
  private final int batchSize;
  /**
   * The number of drive orders per transport order.
   */
  private final int orderSize;
  /**
   * The locations in the model.
   */
  private final List<Location> locations;
  /**
   * A random number generator for selecting locations and operations.
   */
  private final Random random = new Random();

  /**
   * Creates a new RandomOrderBatchCreator.
   *
   * @param transportOrderService The transport order service.
   * @param dispatcherService The dispatcher service.
   * @param batchSize The number of transport orders per batch.
   * @param orderSize The number of drive orders per transport order.
   */
  public RandomOrderBatchCreator(TransportOrderService transportOrderService,
                                 DispatcherService dispatcherService,
                                 int batchSize,
                                 int orderSize) {
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.batchSize = batchSize;
    this.orderSize = orderSize;
    locations = new ArrayList<>(transportOrderService.fetchObjects(Location.class));
  }

  @Override
  public Set<TransportOrder> createOrderBatch()
      throws KernelRuntimeException {
    Set<TransportOrder> createdOrders = new HashSet<>();
    if (locations.isEmpty()) {
      return createdOrders;
    }
    for (int i = 0; i < batchSize; i++) {
      createdOrders.add(createSingleOrder());
    }

    dispatcherService.dispatch();

    return createdOrders;
  }

  private TransportOrder createSingleOrder()
      throws KernelRuntimeException {
    List<DestinationCreationTO> dests = new ArrayList<>();
    for (int j = 0; j < orderSize; j++) {
      Location destLoc = locations.get(random.nextInt(locations.size()));
      dests.add(new DestinationCreationTO(destLoc.getName(), Destination.OP_NOP));
    }
    TransportOrder newOrder = transportOrderService.createTransportOrder(
        new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(), dests));

    return newOrder;
  }
}
