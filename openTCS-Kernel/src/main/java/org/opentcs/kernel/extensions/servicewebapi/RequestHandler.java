/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import org.opentcs.components.Lifecycle;
import spark.Service;

/**
 * A request handler.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface RequestHandler
    extends Lifecycle {

  /**
   * Registers the handler's routes with the given service.
   *
   * @param service The service to register the routes with.
   */
  void addRoutes(Service service);
}
