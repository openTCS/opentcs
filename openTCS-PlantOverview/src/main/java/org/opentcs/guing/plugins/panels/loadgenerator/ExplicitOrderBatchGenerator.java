/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.model.Location;
import org.opentcs.data.order.DriveOrder;
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
  private static final Logger log
      = LoggerFactory.getLogger(ExplicitOrderBatchGenerator.class);
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
    List<DriveOrder.Destination> destinations = new LinkedList<>();
    for (DriveOrderStructure currentOrder : curData.getDriveOrders()) {
      // Get the target of the drive order
      Location destLoc = kernel.getTCSObject(Location.class,
                                             currentOrder.getDriveOrderLocation());
      // Get the operation of the drive order
      String destOp
          = currentOrder.getDriveOrderVehicleOperation();
      // Add the drive order to the list
      destinations.add(new DriveOrder.Destination(destLoc.getReference(),
                                                  destOp));
    }

    // Create a new transport order, set the indended vehicle, 
    // the deadline, the properties and acivate it
    TransportOrder newOrder = kernel.createTransportOrder(destinations);
    kernel.setTransportOrderIntendedVehicle(newOrder.getReference(),
                                            curData.getIntendedVehicle());
    kernel.setTransportOrderDeadline(newOrder.getReference(),
                                     curData.getDeadline().getTime());
    for (Map.Entry<String, String> curEntry : curData.getProperties().entrySet()) {
      kernel.setTCSObjectProperty(newOrder.getReference(),
                                  curEntry.getKey(),
                                  curEntry.getValue());
    }
    kernel.activateTransportOrder(newOrder.getReference());
    return newOrder;
  }
}
