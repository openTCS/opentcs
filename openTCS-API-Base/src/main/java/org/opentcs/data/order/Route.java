/**
 * Copyright (c) The openTCS Authors.
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A route for a {@link Vehicle}, consisting of a sequence of steps (pairs of {@link Path}s and
 * {@link Point}s) that need to be processed in their given order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Route
    implements Serializable {

  /**
   * The sequence of steps this route consists of, in the order they are to be processed.
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
  public Route(@Nonnull List<Step> routeSteps, long routeCosts) {
    requireNonNull(routeSteps, "routeSteps");
    checkArgument(!routeSteps.isEmpty(), "routeSteps may not be empty");
    steps = Collections.unmodifiableList(new ArrayList<>(routeSteps));
    costs = routeCosts;
  }

  /**
   * Returns the sequence of steps this route consists of.
   *
   * @return The sequence of steps this route consists of.
   * May be empty.
   * The returned <code>List</code> is unmodifiable.
   */
  @Nonnull
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
   * Returns the final destination point that is reached by travelling this route.
   * (I.e. returns the destination point of this route's last step.)
   *
   * @return The final destination point that is reached by travelling this route.
   */
  @Nonnull
  public Point getFinalDestinationPoint() {
    return steps.get(steps.size() - 1).getDestinationPoint();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Route)) {
      return false;
    }
    final Route other = (Route) o;
    return costs == other.costs
        && Objects.equals(steps, other.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(steps, costs);
  }

  @Override
  public String toString() {
    return "Route{" + "steps=" + steps + ", costs=" + costs + '}';
  }

  /**
   * A single step in a route.
   */
  public static class Step
      implements Serializable {

    /**
     * The path to travel.
     */
    private final Path path;
    /**
     * The point that the vehicle is starting from.
     */
    private final Point sourcePoint;
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
     * Whether execution of this step is allowed.
     */
    private final boolean executionAllowed;

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param srcPoint The point that the vehicle is starting from.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     * @param executionAllowed Whether execution of this step is allowed.
     */
    public Step(@Nullable Path path,
                @Nullable Point srcPoint,
                @Nonnull Point destPoint,
                @Nonnull Vehicle.Orientation orientation,
                int routeIndex,
                boolean executionAllowed) {
      this.path = path;
      this.sourcePoint = srcPoint;
      this.destinationPoint = requireNonNull(destPoint, "destPoint");
      this.vehicleOrientation = requireNonNull(orientation, "orientation");
      this.routeIndex = routeIndex;
      this.executionAllowed = executionAllowed;
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param srcPoint The point that the vehicle is starting from.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     */
    public Step(@Nullable Path path,
                @Nullable Point srcPoint,
                @Nonnull Point destPoint,
                @Nonnull Vehicle.Orientation orientation,
                int routeIndex) {
      this(path, srcPoint, destPoint, orientation, routeIndex, true);
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     * @deprecated Use other constructor instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    public Step(@Nullable Path path,
                @Nonnull Point destPoint,
                @Nonnull Vehicle.Orientation orientation,
                int routeIndex) {
      this(path, null, destPoint, orientation, routeIndex);
    }

    /**
     * Returns the path to travel.
     *
     * @return The path to travel. May be <code>null</code> if the vehicle does
     * not really have to move.
     */
    @Nullable
    public Path getPath() {
      return path;
    }

    /**
     * Returns the point that the vehicle is starting from.
     *
     * @return The point that the vehicle is starting from.
     * May be <code>null</code> if the vehicle does not really have to move.
     */
    @Nullable
    public Point getSourcePoint() {
      return sourcePoint;
    }

    /**
     * Returns the point that is reached by travelling the path.
     *
     * @return The point that is reached by travelling the path.
     */
    @Nonnull
    public Point getDestinationPoint() {
      return destinationPoint;
    }

    /**
     * Returns the direction into which the vehicle is supposed to travel.
     *
     * @return The direction into which the vehicle is supposed to travel.
     */
    @Nonnull
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

    /**
     * Returns whether execution of this step is allowed.
     *
     * @return {@code true}, if execution of this step is allowed, otherwise {@code false}.
     */
    public boolean isExecutionAllowed() {
      return executionAllowed;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Step)) {
        return false;
      }
      Step other = (Step) o;
      return Objects.equals(path, other.path)
          && Objects.equals(sourcePoint, other.sourcePoint)
          && Objects.equals(destinationPoint, other.destinationPoint)
          && Objects.equals(vehicleOrientation, other.vehicleOrientation)
          && routeIndex == other.routeIndex;
    }

    @Override
    public int hashCode() {
      return Objects.hash(path, sourcePoint, destinationPoint, vehicleOrientation, routeIndex);
    }

    @Override
    public String toString() {
      return destinationPoint.getName();
    }
  }
}
