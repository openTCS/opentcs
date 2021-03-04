/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a re-routing of vehicles.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RerouteTask
    implements Runnable {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RerouteTask.class);
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * Provides utility methods for rerouting vehicles.
   */
  private final RerouteUtil rerouteUtil;

  /**
   * Creates a new instance.
   *
   * @param objectService The transport order service.
   * @param rerouteUtil Provides utility methods for rerouting vehicles.
   */
  @Inject
  public RerouteTask(TCSObjectService objectService,
                     RerouteUtil rerouteUtil) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.rerouteUtil = requireNonNull(rerouteUtil, "rerouteUtil");
  }

  @Override
  public void run() {
    for (Vehicle vehicle : objectService.fetchObjects(Vehicle.class)) {
      // Ignore vehicles that don't process any order
      if (!vehicle.isProcessingOrder()) {
        LOG.debug("{}: Not processing any transport order, ignoring.", vehicle.getName());
        continue;
      }

      // For every vehicle that's affected, perform a re-routing and update the transport order
      LOG.debug("{}: Rerouting due to topology change.", vehicle.getName());
      rerouteUtil.reroute(vehicle);
    }
  }
}
