/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes a sequence of {@link Point}s that can be used as an alternative to dynamically computed
 * routes for a {@link Vehicle}.
 *
 * @see Router
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Static routes are an undesirable exception to routes computed by a {@link Router}
 * implementation and will not be supported in the future.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class StaticRoute
    extends TCSObject<StaticRoute>
    implements Serializable,
               Cloneable {

  /**
   * The sequence of points this route consists of.
   */
  private final List<TCSObjectReference<Point>> hops;

  /**
   * Creates a new StaticRoute with the given name and ID.
   *
   * @param objectID The route's object ID.
   * @param name The route's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public StaticRoute(int objectID, String name) {
    super(objectID, name);
    this.hops = new ArrayList<>();
  }

  /**
   * Creates a new StaticRoute with the given name and ID.
   *
   * @param name The route's name.
   */
  public StaticRoute(String name) {
    super(name);
    this.hops = new ArrayList<>();
  }

  /**
   * Creates a new StaticRoute with the given name and ID.
   *
   * @param objectID The route's object ID.
   * @param name The route's name.
   * @param hops The sequence of points this route consists of.
   */
  @SuppressWarnings("deprecation")
  private StaticRoute(int objectID,
                      String name,
                      Map<String, String> properties,
                      List<TCSObjectReference<Point>> hops) {
    super(objectID, name, properties);
    this.hops = listWithoutNullValues(requireNonNull(hops, "hops"));
  }

  @Override
  public StaticRoute withProperty(String key, String value) {
    return new StaticRoute(getIdWithoutDeprecationWarning(),
                           getName(),
                           propertiesWith(key, value),
                           hops);
  }

  @Override
  public StaticRoute withProperties(Map<String, String> properties) {
    return new StaticRoute(getIdWithoutDeprecationWarning(), getName(), properties, hops);
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
    return Collections.unmodifiableList(hops);
  }

  /**
   * Adds a hop to the end of this route.
   *
   * @param newHop The hop to be added.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void addHop(TCSObjectReference<Point> newHop) {
    requireNonNull(newHop, "newHop");
    hops.add(newHop);
  }

  /**
   * Creates a copy of this object, with the given hops.
   *
   * @param hops The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public StaticRoute withHops(List<TCSObjectReference<Point>> hops) {
    return new StaticRoute(getIdWithoutDeprecationWarning(), getName(), getProperties(), hops);
  }

  /**
   * Removes all hops from this route.
   *
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
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

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public StaticRoute clone() {
    return new StaticRoute(getIdWithoutDeprecationWarning(), getName(), getProperties(), hops);
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
  }

}
