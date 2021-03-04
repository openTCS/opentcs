/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;

/**
 * Checks for transport orders that are still in state RAW, and attempts to prepare them for
 * assignment.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CheckNewOrdersPhase
    implements Phase {

  /**
   * The object service
   */
  private final TCSObjectService objectService;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  private final TransportOrderUtil transportOrderUtil;
  /**
   * The dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public CheckNewOrdersPhase(TCSObjectService objectService,
                             Router router,
                             TransportOrderUtil transportOrderUtil,
                             DefaultDispatcherConfiguration configuration) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.router = requireNonNull(router, "router");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  @Override
  public void run() {
    objectService.fetchObjects(TransportOrder.class, this::inRawState).stream()
        .forEach(order -> checkRawTransportOrder(order));
  }

  private void checkRawTransportOrder(TransportOrder order) {
    requireNonNull(order, "order");

    // Check if the transport order is routable.
    if (configuration.dismissUnroutableTransportOrders()
        && router.checkRoutability(order).isEmpty()) {
      transportOrderUtil.updateTransportOrderState(order.getReference(),
                                                   TransportOrder.State.UNROUTABLE);
      return;
    }
    transportOrderUtil.updateTransportOrderState(order.getReference(),
                                                 TransportOrder.State.ACTIVE);
    // The transport order has been activated - dispatch it.
    // Check if it has unfinished dependencies.
    if (!transportOrderUtil.hasUnfinishedDependencies(order)) {
      transportOrderUtil.updateTransportOrderState(order.getReference(),
                                                   TransportOrder.State.DISPATCHABLE);
    }
  }

  private boolean inRawState(TransportOrder order) {
    return order.hasState(TransportOrder.State.RAW);
  }
}
