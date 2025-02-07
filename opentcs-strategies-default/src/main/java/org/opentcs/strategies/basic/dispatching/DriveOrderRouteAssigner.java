// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opentcs.components.kernel.RouteSelector;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

/**
 * Assigns {@link Route}s to {@link DriveOrder}s.
 */
public class DriveOrderRouteAssigner {

  private final Router router;
  private final DefaultDispatcherConfiguration configuration;
  private final RouteSelector routeSelector;

  /**
   * Creates a new instance.
   *
   * @param router The router instance calculating routes and their costs.
   * @param routeSelector Selects a route from a set of routes.
   * @param configuration The dispatcher configuration.
   */
  @Inject
  public DriveOrderRouteAssigner(
      Router router,
      RouteSelector routeSelector,
      DefaultDispatcherConfiguration configuration
  ) {
    this.router = requireNonNull(router, "router");
    this.routeSelector = requireNonNull(routeSelector, "routeSelector");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  /**
   * Tries to (compute and) assign {@link Route}s to the {@link DriveOrder}s in the given
   * {@link TransportOrder}.
   *
   * @param order The transport order whose drive orders are to be assigned routes.
   * @param vehicle The vehicle that is intended to process the transport order (i.e, the vehicle
   * for which to calculate the routes).
   * @param startPosition The position at which the vehicle would start processing the transport
   * order.
   * @return An optional containing a list of drive orders with assigned routes, or an empty
   * optional, if no routes could be assigned.
   */
  public Optional<List<DriveOrder>> tryAssignRoutes(
      TransportOrder order,
      Vehicle vehicle,
      Point startPosition
  ) {
    return routeSelector.selectSequence(
        router.getRoutes(vehicle, startPosition, order, configuration.maxRoutesToConsider())
    )
        .map(routes -> {
          List<DriveOrder> driveOrderList = new ArrayList<>();
          for (int i = 0; i < routes.size(); i++) {
            driveOrderList.add(order.getFutureDriveOrders().get(i).withRoute(routes.get(i)));
          }
          return driveOrderList;
        });
  }
}
