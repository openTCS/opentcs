/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator.batchcreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.TransportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Randomly creates batches of transport orders.
 * Destinations and operations chosen are random and not guaranteed to work in
 * a real plant.
 */
public class RandomOrderBatchCreator
    implements OrderBatchCreator {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RandomOrderBatchCreator.class);
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
    this.locations = initializeLocations();
  }

  @Override
  public Set<TransportOrder> createOrderBatch()
      throws KernelRuntimeException {
    Set<TransportOrder> createdOrders = new HashSet<>();

    if (this.locations.isEmpty()) {
      LOG.info("Could not find suitable destination locations");
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
      Location destLoc = this.locations.get(random.nextInt(this.locations.size()));
      dests.add(new DestinationCreationTO(destLoc.getName(), Destination.OP_NOP));
    }
    return transportOrderService.createTransportOrder(
        new TransportOrderCreationTO("TOrder-", dests).withIncompleteName(true)
    );
  }

  private List<Location> initializeLocations() {
    Set<TCSObjectReference<LocationType>> suitableLocationTypeRefs =
        transportOrderService.fetchObjects(LocationType.class)
            .stream()
            .filter(locationType -> locationType.isAllowedOperation(Destination.OP_NOP))
            .map(TCSObject::getReference)
            .collect(Collectors.toSet());

    return transportOrderService.fetchObjects(Location.class)
               .stream()
               .filter(location -> !location.getAttachedLinks().isEmpty())
               .filter(location -> suitableLocationTypeRefs.contains(location.getType()))
               .collect(Collectors.toList());
  }
}
