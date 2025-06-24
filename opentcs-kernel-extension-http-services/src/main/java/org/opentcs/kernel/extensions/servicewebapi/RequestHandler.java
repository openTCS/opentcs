// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi;

import io.javalin.apibuilder.EndpointGroup;
import org.opentcs.components.Lifecycle;

/**
 * A request handler.
 */
public interface RequestHandler
    extends
      Lifecycle {

  /**
   * Creates the handler's routes for the web service.
   */
  EndpointGroup createRoutes();
}
