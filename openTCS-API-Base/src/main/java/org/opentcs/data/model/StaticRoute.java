/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * A named static route that can be used as an alternative to dynamically
 * computing a route for a vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StaticRoute
    extends TCSObject<StaticRoute>
    implements Serializable, Cloneable {

  /**
   * The sequence of points this route consists of.
   */
  private List<TCSObjectReference<Point>> hops = new LinkedList<>();

  /**
   * Creates a new StaticRoute with the given name and ID.
   *
   * @param objectID The route's object ID.
   * @param name The route's name.
   */
  public StaticRoute(int objectID, String name) {
    super(objectID, name);
  }

  /**
   * Returns the first element of the list of hops in this route, or
   * <code>null</code>, if the list of hops is empty.
   *
   * @return The first element of the list of hops in this route, or
   * <code>null</code>, if the list of hops is empty.
   */
  public TCSObjectReference<Point> getSourcePoint() {
    if (hops.isEmpty()) {
      return null;
    }
    else {
      return hops.get(0);
    }
  }

  /**
   * Returns the final element of the list of hops in this route, or
   * <code>null</code>, if the list of hops is empty.
   *
   * @return The final element of the list of hops in this route, or
   * <code>null</code>, if the list of hops is empty.
   */
  public TCSObjectReference<Point> getDestinationPoint() {
    if (hops.isEmpty()) {
      return null;
    }
    else {
      return hops.get(hops.size() - 1);
    }
  }

  /**
   * Returns the sequence of points this route consists of.
   *
   * @return The sequence of points this route consists of.
   */
  public List<TCSObjectReference<Point>> getHops() {
    return new LinkedList<>(hops);
  }

  /**
   * Adds a hop to the end of this route.
   *
   * @param newHop The hop to be added.
   */
  public void addHop(TCSObjectReference<Point> newHop) {
    requireNonNull(newHop, "newHop");
    hops.add(newHop);
  }

  /**
   * Removes all hops from this route.
   */
  public void clearHops() {
    hops.clear();
  }
  
  /**
   * Checks whether this static route is valid or not.
   * A static route is valid if it has at least two hops.
   *
   * @return <code>true</code> if, and only if, this static route is valid.
   */
  public boolean isValid() {
    return hops.size() >= 2;
  }

  @Override
  public StaticRoute clone() {
    StaticRoute clone = (StaticRoute) super.clone();
    clone.hops = new LinkedList<>();
    for (TCSObjectReference<Point> curRef : hops) {
      clone.hops.add(curRef);
    }
    return clone;
  }
}
