/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This interface declares the methods a router module for the openTCS
 * kernel must implement.
 * <p>
 * A router finds routes from a start point to an end point, rating them
 * according to implementation specific criteria/costs parameters.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Router
    extends Lifecycle {

  /**
   * The key of a vehicle property defining the group of vehicles that may share the same routing.
   * <p>
   * The value is expected to be an integer.
   * </p>
   */
  String PROPKEY_ROUTING_GROUP = "tcs:routingGroup";

  /**
   * Notifies the router of changes in the topology.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default void topologyChanged() {
    updateRoutingTables();
  }

  /**
   * Notifies the router of changes in the topology and triggers an update of its routing tables.
   *
   * @deprecated Implement and use {@link #topologyChanged()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void updateRoutingTables();

  /**
   * Checks the general routability of a given transport order.
   *
   * @param order The transport order to check for routability.
   * @return A set of vehicles for which a route for the given transport order
   * would be computable.
   */
  @Nonnull
  Set<Vehicle> checkRoutability(@Nonnull TransportOrder order);

  /**
   * Returns a complete route for a given vehicle that starts on a specified
   * point and allows the vehicle to process a given transport order.
   * The route is encapsulated into drive orders which correspond to those drive
   * orders that the transport order is composed of. The transport order itself
   * is not modified.
   *
   * @param vehicle The vehicle for which the calculated route must be passable.
   * @param sourcePoint The position at which the vehicle would start processing
   * the transport order (i.e. the vehicle's current position).
   * @param transportOrder The transport order to be processed by the vehicle.
   * @return A list of drive orders containing the complete calculated route for
   * the given transport order, passable the given vehicle and starting on the
   * given point, or the empty optional, if no such route exists.
   */
  @Nonnull
  Optional<List<DriveOrder>> getRoute(@Nonnull Vehicle vehicle,
                                      @Nonnull Point sourcePoint,
                                      @Nonnull TransportOrder transportOrder);

  /**
   * Returns a route from one point to another, passable for a given vehicle.
   *
   * @param vehicle The vehicle for which the route must be passable.
   * @param sourcePoint The starting point of the route to calculate.
   * @param destinationPoint The end point of the route to calculate.
   * @return The calculated route, or the empty optional, if a route between the
   * given points does not exist.
   */
  @Nonnull
  Optional<Route> getRoute(@Nonnull Vehicle vehicle,
                           @Nonnull Point sourcePoint,
                           @Nonnull Point destinationPoint);

  /**
   * Returns the costs for travelling a route from one point to another with a
   * given vehicle.
   *
   * @param vehicle The vehicle for which the route must be passable.
   * @param sourcePoint The starting point of the route.
   * @param destinationPoint The end point of the route.
   * @return The costs of the route, or <code>Long.MAX_VALUE</code>, if no such
   * route exists.
   */
  long getCosts(@Nonnull Vehicle vehicle,
                @Nonnull Point sourcePoint,
                @Nonnull Point destinationPoint);

  /**
   * Returns the costs for travelling a route from one point to another with a
   * given vehicle.
   *
   * @param vehicle The vehicle for which the route must be passable.
   * @param srcPointRef The starting point reference of the route.
   * @param dstPointRef The end point reference of the route.
   * @return The costs of the route, or <code>Long.MAX_VALUE</code>, if no such
   * route exists.
   */
  long getCostsByPointRef(@Nonnull Vehicle vehicle,
                          @Nonnull TCSObjectReference<Point> srcPointRef,
                          @Nonnull TCSObjectReference<Point> dstPointRef);

  /**
   * Returns the costs for travelling a route from one location to another with
   * a given vehicle.
   *
   * @param vehicle The vehicle the costs shall be calculated for.
   * @param srcRef A reference to the source location
   * @param destRef A reference to the destination location
   * @return The costs of the route, or
   * <code>Long.MAX_VALUE</code>, if no such route exists.
   */
  long getCosts(@Nonnull Vehicle vehicle,
                @Nonnull TCSObjectReference<Location> srcRef,
                @Nonnull TCSObjectReference<Location> destRef);

  /**
   * Notifies the router of a route being selected for a vehicle.
   *
   * @param vehicle The vehicle for which a route is being selected.
   * @param driveOrders The drive orders encapsulating the route being selected,
   * or <code>null</code>, if no route is being selected for the vehicle (i.e.
   * an existing entry for the given vehicle would be removed).
   */
  void selectRoute(@Nonnull Vehicle vehicle, @Nullable List<DriveOrder> driveOrders);

  /**
   * Returns an unmodifiable view on the selected routes the router knows about.
   * The returned map contains an entry for each vehicle for which a selected
   * route is known.
   *
   * @return An unmodifiable view on the selected routes the router knows about.
   */
  @Nonnull
  Map<Vehicle, List<DriveOrder>> getSelectedRoutes();

  /**
   * Returns all points which are currently targeted by any vehicle.
   *
   * @return A set of all points currently targeted by any vehicle.
   */
  @Nonnull
  Set<Point> getTargetedPoints();

  /**
   * Returns a human readable text describing this router's internal state.
   *
   * @return A human readable text describing this router's internal state.
   * @deprecated Does not serve any real purpose and will be removed.
   */
  @Nonnull
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  default String getInfo() {
    return "";
  }
}
