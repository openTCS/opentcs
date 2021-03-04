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
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
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
   * The kernel we talk to.
   */
  private final Kernel kernel;
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
   * @param kernel The kernel
   * @param batchSize The number of transport orders per batch.
   * @param orderSize The number of drive orders per transport order.
   */
  public RandomOrderBatchCreator(final Kernel kernel,
                                 final int batchSize,
                                 final int orderSize) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.batchSize = batchSize;
    this.orderSize = orderSize;
    locations = new ArrayList<>(kernel.getTCSObjects(Location.class));
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
    return createdOrders;
  }

  private TransportOrder createSingleOrder()
      throws KernelRuntimeException {
    List<DestinationCreationTO> dests = new ArrayList<>();
    for (int j = 0; j < orderSize; j++) {
      Location destLoc = locations.get(random.nextInt(locations.size()));
      dests.add(new DestinationCreationTO(destLoc.getName(), Destination.OP_NOP));
    }
    TransportOrder newOrder = kernel.createTransportOrder(
        new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(), dests));
    kernel.activateTransportOrder(newOrder.getReference());
    return newOrder;
  }
}
