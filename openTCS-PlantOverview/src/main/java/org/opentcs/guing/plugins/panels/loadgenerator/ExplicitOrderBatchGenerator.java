/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.data.order.TransportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A batch generator for creating explicit transport orders.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class ExplicitOrderBatchGenerator
    implements OrderBatchCreator {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExplicitOrderBatchGenerator.class);
  /**
   * The kernel we talk to.
   */
  private final Kernel kernel;
  /**
   * The TransportOrderData we're building the transport orders from.
   */
  private final List<TransportOrderData> data;

  /**
   * Creates a new ExplicitOrderBatchGenerator.
   *
   * @param kernel The kernel
   * @param data The transport order data
   */
  public ExplicitOrderBatchGenerator(final Kernel kernel,
                                     List<TransportOrderData> data) {
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    this.data = Objects.requireNonNull(data, "data is null");
  }

  @Override
  public Set<TransportOrder> createOrderBatch()
      throws KernelRuntimeException {
    Set<TransportOrder> createdOrders = new HashSet<>();
    for (TransportOrderData curData : data) {
      createdOrders.add(createSingleOrder(curData));
    }

    return createdOrders;
  }

  private TransportOrder createSingleOrder(TransportOrderData curData)
      throws KernelRuntimeException {
    TransportOrder newOrder = kernel.createTransportOrder(
        new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(),
                                     createDestinations(curData.getDriveOrders()))
            .setDeadline(ZonedDateTime.now().plusSeconds(curData.getDeadline().getTime() / 1000))
            .setIntendedVehicleName(curData.getIntendedVehicle() == null
                ? null
                : curData.getIntendedVehicle().getName())
            .setProperties(curData.getProperties()));

    kernel.activateTransportOrder(newOrder.getReference());
    return newOrder;
  }

  private List<DestinationCreationTO> createDestinations(List<DriveOrderStructure> structures) {
    List<DestinationCreationTO> result = new ArrayList<>();
    for (DriveOrderStructure currentOrder : structures) {
      result.add(new DestinationCreationTO(currentOrder.getDriveOrderLocation().getName(),
                                           currentOrder.getDriveOrderVehicleOperation()));
    }
    return result;
  }
}
