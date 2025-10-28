// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;

/**
 * This interface declares the methods a router module for the openTCS
 * kernel must implement.
 * <p>
 * A router finds routes from a start point to an end point, rating them
 * according to implementation specific criteria/costs parameters.
 * </p>
 */
public interface Router
    extends
      Lifecycle {

  /**
   * The key of a vehicle property defining the group of vehicles that may share the same routing.
   * <p>
   * The value is expected to be an integer.
   * </p>
   */
  String PROPKEY_ROUTING_GROUP = "tcs:routingGroup";
  /**
   * The key (prefix) of a path property defining the routing cost when its travelled in forward
   * direction.
   * <p>
   * The value is expected to be a (long) integer.
   * </p>
   */
  String PROPKEY_ROUTING_COST_FORWARD = "tcs:routingCostForward";
  /**
   * The key (prefix) of a path property defining the routing cost when its travelled in reverse
   * direction.
   * <p>
   * The value is expected to be a (long) integer.
   * </p>
   */
  String PROPKEY_ROUTING_COST_REVERSE = "tcs:routingCostReverse";

  /**
   * Notifies the router to update its routing topology with respect to the given paths.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param paths The paths to update in the routing topology. An empty set of paths results in the
   * router updating the entire routing topology.
   */
  void updateRoutingTopology(
      @Nonnull
      Set<Path> paths
  );

  /**
   * Checks the routability of a given transport order.
   * <p>
   * The check for routability is affected by path properties and configured edge evaluators. This
   * means that whether a transport order is considered routable <em>can</em> change between
   * consecutive calls to this method.
   * </p>
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param order The transport order to check for routability.
   * @return A set of vehicles for which a route for the given transport order
   * would be computable.
   */
  @Nonnull
  Set<Vehicle> checkRoutability(
      @Nonnull
      TransportOrder order
  );

  /**
   * Checks the general routability of a given transport order.
   * <p>
   * The check for general routability is <em>not</em> affected by any path properties or any
   * configured edge evaluators. This means that whether a transport order is considered generally
   * routable <em>will not</em> change between consecutive calls to this method.
   * </p>
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param order The transport order to check for routability.
   * @return {@code true}, if the transport order is generally routable, otherwise {@code false}.
   */
  boolean checkGeneralRoutability(
      @Nonnull
      TransportOrder order
  );

  /**
   * Returns possible complete route sequences for a given vehicle that start on a specified point
   * and allow the vehicle to process a given transport order.
   * <p>
   * The routes in a route sequence (as well as their order) correspond to the drive orders that
   * the given transport order is composed of.
   * </p>
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param vehicle The vehicle for which the calculated routes must be passable.
   * @param sourcePoint The position at which the vehicle would start processing the transport order
   * (e.g. the vehicle's current position).
   * @param transportOrder The transport order to be processed by the vehicle.
   * @param maxRouteCount The maximum number of route sequences to return.
   * @return A set of route sequences that allow the given vehicle to process the given transport
   * order, or an empty set, if no such route exists.
   */
  @Nonnull
  Set<List<Route>> getRoutes(
      @Nonnull
      Vehicle vehicle,
      @Nonnull
      Point sourcePoint,
      @Nonnull
      TransportOrder transportOrder,
      int maxRouteCount
  );

  /**
   * Returns possible routes from one point to another, passable by a given vehicle.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param vehicle The vehicle for which the calculated routes must be passable.
   * @param sourcePoint The starting point of the routes to calculate.
   * @param destinationPoint The end point of the routes to calculate.
   * @param resourcesToAvoid Resources to avoid when calculating the routes.
   * @param maxRouteCount The maximum number of routes to return.
   * @return A set of routes, or an empty set, if no routes between the given points exist.
   */
  @Nonnull
  Set<Route> getRoutes(
      @Nonnull
      Vehicle vehicle,
      @Nonnull
      Point sourcePoint,
      @Nonnull
      Point destinationPoint,
      @Nonnull
      Set<TCSResourceReference<?>> resourcesToAvoid,
      int maxRouteCount
  );
}
