/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * A route for a vehicle, consisting of a sequence of steps (pairs of paths and
 * points) that need to be processed in their given order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Route
    implements Serializable {

  /**
   * The sequence of steps this route consists of, in the order they are to be
   * processed.
   */
  private final List<Step> steps;
  /**
   * The costs for travelling this route.
   */
  private final long costs;

  /**
   * Creates a new Route.
   *
   * @param routeSteps The sequence of steps this route consists of.
   * @param routeCosts The costs for travelling this route.
   */
  public Route(List<Step> routeSteps, long routeCosts) {
    if (routeSteps == null) {
      throw new NullPointerException("routeSteps is null");
    }
    if (routeSteps.isEmpty()) {
      steps = Collections.unmodifiableList(new ArrayList<Step>());
    }
    else {
      steps = Collections.unmodifiableList(new ArrayList<>(routeSteps));
    }
    costs = routeCosts;
  }

  /**
   * Returns the sequence of steps this route consists of.
   *
   * @return The sequence of steps this route consists of. The returned
   * <code>List</code> is unmodifiable.
   */
  public List<Step> getSteps() {
    return steps;
  }

  /**
   * Returns the costs for travelling this route.
   *
   * @return The costs for travelling this route.
   */
  public long getCosts() {
    return costs;
  }

  /**
   * Returns the final destination point that is reached by travelling this
   * route (i.e. the destination point of this route's last step).
   *
   * @return The final destination point that is reached by travelling this
   * route.
   */
  public Point getFinalDestinationPoint() {
    return steps.get(steps.size() - 1).getDestinationPoint();
  }

  @Override
  public String toString() {
    return steps.toString();
  }

  /**
   * A single step in a route, consisting of a path to travel and a point that
   * is reached by travelling the path.
   */
  public static class Step
      implements Serializable {

    /**
     * The path to travel.
     */
    private final Path path;
    /**
     * The point that is reached by travelling the path.
     */
    private final Point destinationPoint;
    /**
     * The direction into which the vehicle is supposed to travel.
     */
    private final Vehicle.Orientation vehicleOrientation;
    /**
     * This step's index in the vehicle's route.
     */
    private final int routeIndex;

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     */
    public Step(Path path,
                Point destPoint,
                Vehicle.Orientation orientation,
                int routeIndex) {
      this.path = path;
      this.destinationPoint = requireNonNull(destPoint, "destPoint");
      vehicleOrientation = requireNonNull(orientation, "orientation");
      this.routeIndex = routeIndex;
    }

    /**
     * Returns the path to travel.
     *
     * @return The path to travel. May be <code>null</code> if the vehicle does
     * not really have to move.
     */
    public Path getPath() {
      return path;
    }

    /**
     * Returns the point that is reached by travelling the path.
     *
     * @return The point that is reached by travelling the path.
     */
    public Point getDestinationPoint() {
      return destinationPoint;
    }

    /**
     * Returns the direction into which the vehicle is supposed to travel.
     *
     * @return The direction into which the vehicle is supposed to travel.
     */
    public Vehicle.Orientation getVehicleOrientation() {
      return vehicleOrientation;
    }

    /**
     * Returns this step's index in the vehicle's route.
     *
     * @return This step's index in the vehicle's route.
     */
    public int getRouteIndex() {
      return routeIndex;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Step)) {
        return false;
      }
      Step other = (Step) o;
      return Objects.equals(path, other.path)
          && Objects.equals(destinationPoint, other.destinationPoint)
          && Objects.equals(vehicleOrientation, other.vehicleOrientation)
          && routeIndex == other.routeIndex;
    }

    @Override
    public int hashCode() {
      return Objects.hash(path, destinationPoint, vehicleOrientation);
    }

    @Override
    public String toString() {
      return destinationPoint.getName();
    }
  }
}
