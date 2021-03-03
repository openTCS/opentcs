/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.kernel.module.routing;

import com.google.common.collect.Table;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.logging.Logger;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.kernel.workingset.Model;

/**
 * A base class for routing table builders.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class RoutingTableBuilderAbstract {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(RoutingTableBuilderAbstract.class.getName());
  /**
   * An evaluator to compute the costs for routes.
   */
  protected final RouteEvaluator routeEvaluator;
  /**
   * The model this routing net is based on.
   */
  protected Model model;
  /**
   * The vehicle for which to compute the routing net.
   */
  protected Vehicle vehicle;
  /**
   * The routing table.
   */
  protected Table<TCSObjectReference<Point>, TCSObjectReference<Point>, RoutingTable.Entry> routingTable;

  /**
   * Creates a new instance.
   *
   * @param routeEvaluator The evaluator to be used to compute costs for routes.
   */
  RoutingTableBuilderAbstract(RouteEvaluator routeEvaluator) {
    this.routeEvaluator = requireNonNull(routeEvaluator, "routeEvaluator");
  }

  /**
   * Checks whether the given point is the destination point of any of the given
   * route steps.
   *
   * @param point The point to check for.
   * @param steps The route steps.
   * @return <code>true</code> if, and only if, the given point is the
   * destination point of any of the given route steps.
   */
  protected boolean visitedPointOnRoute(Point point,
                                        List<Route.Step> steps) {
    requireNonNull(point, "point");
    requireNonNull(steps, "steps");
    for (Route.Step curStep : steps) {
      if (Objects.equals(point, curStep.getDestinationPoint())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Modifies the routing table to use the given static route.
   *
   * @param staticRoute The static route.
   */
  protected void integrateStaticRoute(StaticRoute staticRoute) {
    requireNonNull(staticRoute, "staticRoute");
    // Ignore incomplete static routes.
    if (!staticRoute.isValid()) {
      log.warning("Skipping invalid static route " + staticRoute);
      return;
    }
    Point startPoint = model.getPoint(staticRoute.getSourcePoint());
    List<Route.Step> steps = toRouteSteps(staticRoute);
    long costs = routeEvaluator.computeCosts(vehicle, startPoint, steps);
    updateTableEntry(staticRoute.getSourcePoint(),
                     staticRoute.getDestinationPoint(),
                     steps,
                     costs);
  }

  /**
   * Sets the routing table entry for the given source and destination points.
   *
   * @param source A reference to the source point.
   * @param destination A reference to the destination point.
   * @param steps The steps for the route from source to destination point.
   * @param costs The costs for the route from source to destination point.
   */
  protected void updateTableEntry(TCSObjectReference<Point> source,
                                  TCSObjectReference<Point> destination,
                                  List<Route.Step> steps,
                                  long costs) {
    routingTable.put(source,
                     destination,
                     new RoutingTable.Entry(source, destination, steps, costs));
  }

  /**
   * Creates a list of route steps from the given static route.
   *
   * @param staticRoute The static route.
   * @return A list of route steps from the given static route.
   */
  private List<Route.Step> toRouteSteps(StaticRoute staticRoute) {
    assert staticRoute.isValid();

    List<Route.Step> result = new LinkedList<>();
    Iterator<TCSObjectReference<Point>> hopIter = staticRoute.getHops().iterator();
    Point previousHop = model.getPoint(hopIter.next());
    while (hopIter.hasNext()) {
      Point hop = model.getPoint(hopIter.next());
      Path hopPath = getPathBetween(previousHop, hop);
      Vehicle.Orientation orientation
          = Objects.equals(hopPath.getSourcePoint(), previousHop)
          ? Vehicle.Orientation.FORWARD
          : Vehicle.Orientation.BACKWARD;
      result.add(new Route.Step(hopPath, hop, orientation, result.size()));
      previousHop = hop;
    }
    return result;
  }

  /**
   * Returns the path in the model connecting the two given points.
   *
   * @param point1 A point.
   * @param point2 Another point.
   * @return The path in the model connecting the two given points.
   * @throws IllegalArgumentException If the given points are not connected.
   */
  private Path getPathBetween(Point point1, Point point2) throws IllegalArgumentException {
    requireNonNull(point1, "point1");
    requireNonNull(point2, "point2");
    for (TCSObjectReference<Path> outPathRef : point1.getOutgoingPaths()) {
      Path outPath = model.getPath(outPathRef);
      if (Objects.equals(point2.getReference(), outPath.getDestinationPoint())) {
        return outPath;
      }
    }
    for (TCSObjectReference<Path> inPathRef : point1.getIncomingPaths()) {
      Path inPath = model.getPath(inPathRef);
      if (Objects.equals(point2.getReference(), inPath.getSourcePoint())) {
        return inPath;
      }
    }
    throw new IllegalArgumentException(
        point1 + " and " + point2 + " are not connected.");
  }
}
