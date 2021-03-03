/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.routing;

import com.google.common.collect.HashBasedTable;
import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.kernel.workingset.Model;

/**
 * Builds routing tables using a depth-first-search implementation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class RoutingTableBuilderDfs
    extends RoutingTableBuilderAbstract
    implements RoutingTableBuilder {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(RoutingTableBuilderDfs.class.getName());
  /**
   * The maximum search depth.
   */
  private final int searchDepth;
  /**
   * Whether to terminate the DFS early when a cheaper route to a point has
   * already been found.
   */
  private final boolean terminateEarly;

  /**
   * Creates a new instance.
   *
   * @param routeEvaluator The evaluator to be used to compute costs for routes.
   * @param searchDepth The maximum depth for the DFS algorithm.
   * @param terminateEarly Whether to terminate the DFS early when a cheaper
   * route to a point has already been found.
   */
  @Inject
  RoutingTableBuilderDfs(RouteEvaluator routeEvaluator,
                         @SearchDepth int searchDepth,
                         @TerminateEarly boolean terminateEarly) {
    super(routeEvaluator);
    this.searchDepth = searchDepth;
    this.terminateEarly = terminateEarly;
  }

  @Override
  public RoutingTable computeTable(Model model, Vehicle vehicle) {
    this.model = requireNonNull(model, "model");
    this.vehicle = requireNonNull(vehicle, "vehicle");

    routingTable = HashBasedTable.create();
    long timeStampBefore = System.currentTimeMillis();
    for (Point curPoint : model.getPoints(null)) {
      updateTableEntry(curPoint.getReference(),
                       curPoint.getReference(),
                       new LinkedList<Route.Step>(),
                       0);
      descendSuccessors(curPoint, curPoint, new LinkedList<Route.Step>());
    }
    double timePassed = (System.currentTimeMillis() - timeStampBefore) / 1000.0;
    log.log(Level.INFO,
            "Computed routing table for {0} in {1,number,#.##} seconds.",
            new Object[] {vehicle.getName(), timePassed});
    for (StaticRoute staticRoute : model.getStaticRoutes(null)) {
      integrateStaticRoute(staticRoute);
    }
    return new RoutingTable(routingTable);
  }

  /**
   *
   * @param startPoint The point we started from.
   * @param curPoint The point we're currently looking at.
   * @param steps The steps we travelled to get here.
   */
  private void computeTableEntries(Point startPoint,
                                   Point curPoint,
                                   LinkedList<Route.Step> steps) {
    requireNonNull(startPoint, "startPoint");
    requireNonNull(curPoint, "curPoint");
    requireNonNull(steps, "steps");

    long costs = routeEvaluator.computeCosts(vehicle, startPoint, steps);
    RoutingTable.Entry tableEntry
        = routingTable.get(startPoint.getReference(),
                           curPoint.getReference());
    // If we found a better route than any known one, update the table entry.
    if (tableEntry == null || costs < tableEntry.getCosts()) {
      updateTableEntry(startPoint.getReference(),
                       curPoint.getReference(),
                       new LinkedList<>(steps),
                       costs);
    }
    // If the route found is not better than an existing one and we should
    // terminate early, do so.
    // (Not knowing the cost function applied to the route, terminating here
    // might mean that a shorter route to one of the successors will not be
    // found. An exhaustive search might take much longer, however.)
    else if (terminateEarly) {
      return;
    }
    // If we have reached the maximum search depth, terminate the recursion.
    if (steps.size() > searchDepth) {
      return;
    }
    descendSuccessors(startPoint, curPoint, steps);
  }

  private void descendSuccessors(Point startPoint,
                                 Point curPoint,
                                 LinkedList<Route.Step> steps) {
    descendSuccessorsForward(startPoint, curPoint, steps);
    descendSuccessorsBackwards(startPoint, curPoint, steps);
  }

  private void descendSuccessorsForward(Point startPoint,
                                        Point curPoint,
                                        LinkedList<Route.Step> steps) {
    // Check all outgoing paths for more points to visit.
    for (TCSObjectReference<Path> outPathRef : curPoint.getOutgoingPaths()) {
      Path outPath = model.getPath(outPathRef);
      Point nextPoint = model.getPoint(outPath.getDestinationPoint());
      if (!visitedPointOnRoute(nextPoint, steps)
          && outPath.isNavigableForward()) {
        steps.addLast(new Route.Step(outPath,
                                     nextPoint,
                                     Vehicle.Orientation.FORWARD,
                                     steps.size()));
        computeTableEntries(startPoint, nextPoint, steps);
        steps.removeLast();
      }
    }
  }

  private void descendSuccessorsBackwards(Point startPoint,
                                          Point curPoint,
                                          LinkedList<Route.Step> steps) {
    // Check all incoming paths for more points to visit.
    for (TCSObjectReference<Path> inPathRef : curPoint.getIncomingPaths()) {
      Path inPath = model.getPath(inPathRef);
      Point nextPoint = model.getPoint(inPath.getSourcePoint());
      if (!visitedPointOnRoute(nextPoint, steps)
          && inPath.isNavigableReverse()) {
        steps.addLast(new Route.Step(inPath,
                                     nextPoint,
                                     Vehicle.Orientation.BACKWARD,
                                     steps.size()));
        computeTableEntries(startPoint, nextPoint, steps);
        steps.removeLast();
      }
    }
  }

  /**
   * Annotation type for injecting the maximum search depth.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface SearchDepth {
    // Nothing here.
  }

  /**
   * Annotation type for injecting whether to do a complete search or not.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  static @interface TerminateEarly {
    // Nothing here.
  }
}
