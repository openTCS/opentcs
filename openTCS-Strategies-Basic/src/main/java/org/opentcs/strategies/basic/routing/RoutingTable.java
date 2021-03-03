/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import com.google.common.collect.Table;
import java.util.Collections;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route;

/**
 * Provides routing and distance data for the topology of a model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RoutingTable {

  /**
   * A constant for marking the costs for a route as infinite.
   */
  public static final long INFINITE_COSTS = Long.MAX_VALUE;
  /**
   * The actual table.
   */
  private final Table<TCSObjectReference<Point>, TCSObjectReference<Point>, Entry> routingTable;

  /**
   * Creates a new instance.
   *
   * @param routingTable The actual routing table.
   */
  RoutingTable(Table<TCSObjectReference<Point>, TCSObjectReference<Point>, Entry> routingTable) {
    this.routingTable = requireNonNull(routingTable, "routingTable");
  }

  /**
   * Returns a list of route steps to travel from a given source point to a
   * given destination point.
   *
   * @param srcPoint The source point.
   * @param destPoint The destination point.
   * @return A list of steps in the order they are to be travelled from
   * the source point to the destination point. The returned list does not
   * include a step for the source point. If source point and destination point
   * are identical, the returned list will be empty. If no route exists,
   * <code>null</code> will be returned.
   */
  public List<Route.Step> getRouteSteps(Point srcPoint, Point destPoint) {
    requireNonNull(srcPoint, "srcPoint is null");
    requireNonNull(destPoint, "destPoint is null");

    Entry entry = routingTable.get(srcPoint.getReference(),
                                   destPoint.getReference());
    if (entry == null) {
      return null;
    }
    return entry.steps;
  }

  /**
   * Returns the costs for travelling the shortest route from one point to
   * another.
   *
   * @param srcPoint The starting point.
   * @param destPoint The destination point.
   * @return The costs for travelling the shortest route from the starting point
   * to the destination point. If no route exists,
   * {@link #INFINITE_COSTS INFINITE_COSTS} will be
   * returned.
   */
  public long getCosts(Point srcPoint, Point destPoint) {
    requireNonNull(srcPoint, "srcPoint is null");
    requireNonNull(destPoint, "destPoint is null");

    return getCosts(srcPoint.getReference(), destPoint.getReference());
  }

  /**
   * Returns the costs for travelling the shortest route from one point to
   * another.
   *
   * @param srcPointRef The starting point reference.
   * @param destPointRef The destination point reference.
   * @return The costs for travelling the shortest route from the starting point
   * to the destination point. If no route exists,
   * {@link #INFINITE_COSTS INFINITE_COSTS} will be
   * returned.
   */
  public long getCosts(TCSObjectReference<Point> srcPointRef,
                       TCSObjectReference<Point> destPointRef) {
    requireNonNull(srcPointRef, "srcPointRef is null");
    requireNonNull(destPointRef, "destPointRef is null");

    Entry entry = routingTable.get(srcPointRef, destPointRef);
    return entry == null ? INFINITE_COSTS : entry.getCosts();
  }

  /**
   * An entry in the routing table.
   */
  static class Entry {

    /**
     * The source point.
     */
    private final TCSObjectReference<Point> source;
    /**
     * The destination point.
     */
    private final TCSObjectReference<Point> destination;
    /**
     * The route steps to be travelled from source to destination.
     */
    private final List<Route.Step> steps;
    /**
     * The costs for travelling from source to destination.
     */
    private final long costs;

    /**
     * Creates a new instance.
     *
     * @param source The source point.
     * @param destination The destination point.
     * @param steps The route steps to be travelled from source to destination.
     * @param costs The costs for travelling from source to destination.
     */
    public Entry(TCSObjectReference<Point> source,
                 TCSObjectReference<Point> destination,
                 List<Route.Step> steps,
                 long costs) {
      this.source = requireNonNull(source, "source");
      this.destination = requireNonNull(destination, "destination");
      this.steps = Collections.unmodifiableList(requireNonNull(steps, "steps"));
      this.costs = costs;
    }

    /**
     * Returns the route steps to be travelled from source to destination.
     *
     * @return The route steps to be travelled from source to destination.
     */
    public List<Route.Step> getSteps() {
      return steps;
    }

    /**
     * Returns the costs for travelling from source to destination.
     *
     * @return the costs for travelling from source to destination.
     */
    public long getCosts() {
      return costs;
    }

    @Override
    public String toString() {
      return "RoutingTable.Entry[source=" + source + ",destination=" + destination
          + ",steps=" + steps + ",costs=" + costs + "]";
    }
  }
}
