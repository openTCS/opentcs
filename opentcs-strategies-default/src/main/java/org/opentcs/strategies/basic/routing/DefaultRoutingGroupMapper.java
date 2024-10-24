// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing;

import static org.opentcs.components.kernel.Router.PROPKEY_ROUTING_GROUP;

import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.data.model.Vehicle;

/**
 * Determines a vehicle's routing group by reading it's {@link Router#PROPKEY_ROUTING_GROUP}
 * property. Returns {@link #DEFAULT_ROUTING_GROUP} if the property does not exist or is invalid.
 */
public class DefaultRoutingGroupMapper
    implements
      GroupMapper {

  /**
   * The default value of a vehicle's routing group.
   */
  private static final String DEFAULT_ROUTING_GROUP = "";

  /**
   * Creates a new instance.
   */
  public DefaultRoutingGroupMapper() {
  }

  @Override
  public String apply(Vehicle vehicle) {
    String propVal = vehicle.getProperty(PROPKEY_ROUTING_GROUP);

    return propVal == null ? DEFAULT_ROUTING_GROUP : propVal;
  }
}
