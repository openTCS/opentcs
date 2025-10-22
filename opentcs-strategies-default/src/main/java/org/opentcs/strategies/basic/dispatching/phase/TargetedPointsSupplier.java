// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.TransportOrder;

/**
 * A point supplier that tries to find all points which are currently targeted by vehicles.
 */
public class TargetedPointsSupplier {

  /**
   * The object service.
   */
  private final InternalTCSObjectService objectService;

  @Inject
  public TargetedPointsSupplier(InternalTCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }

  /**
   * Returns all points which are currently targeted by vehicles.
   *
   * @return A set of all points currently targeted by vehicles.
   */
  public Set<Point> getTargetedPoints() {
    return objectService.stream(TransportOrder.class)
        .filter(order -> order.hasState(TransportOrder.State.BEING_PROCESSED))
        .map(
            transportOrder -> transportOrder.getAllDriveOrders().getLast().getRoute()
                .getFinalDestinationPoint()
        )
        .collect(Collectors.toSet());

  }
}
