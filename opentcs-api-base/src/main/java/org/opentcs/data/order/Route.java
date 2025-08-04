// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A route for a {@link Vehicle}, consisting of a sequence of steps (pairs of {@link Path}s and
 * {@link Point}s) that need to be processed in their given order.
 */
public class Route
    implements
      Serializable {

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
   * @deprecated Use {@link #Route(java.util.List)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public Route(
      @Nonnull
      List<Step> routeSteps,
      long routeCosts
  ) {
    requireNonNull(routeSteps, "routeSteps");
    checkArgument(!routeSteps.isEmpty(), "routeSteps may not be empty");
    steps = List.copyOf(routeSteps);
    costs = routeCosts;
  }

  /**
   * Creates a new Route.
   *
   * @param routeSteps The sequence of steps this route consists of.
   */
  public Route(
      @Nonnull
      List<Step> routeSteps
  ) {
    requireNonNull(routeSteps, "routeSteps");
    checkArgument(!routeSteps.isEmpty(), "routeSteps may not be empty");
    steps = List.copyOf(routeSteps);
    costs = routeSteps.stream().mapToLong(Step::getCosts).sum();
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
    return steps.getLast().getDestinationPoint();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Route other)) {
      return false;
    }
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
      implements
        Serializable {

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
     * The costs for travelling the path.
     */
    private final long costs;
    /**
     * Whether execution of this step is allowed.
     */
    private final boolean executionAllowed;
    /**
     * Marks this {@link Step} as the origin of a recalculated route and indicates which
     * {@link ReroutingType} was used to determine the (new) route.
     * <p>
     * Might be {@code null}, if this {@link Step} is not the origin of a recalculated route.
     */
    private final ReroutingType reroutingType;

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param srcPoint The point that the vehicle is starting from.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     * @param executionAllowed Whether execution of this step is allowed.
     * @param reroutingType Marks this step as the origin of a recalculated route.
     * @deprecated Use {@link #Step(Path, Point, Point, Vehicle.Orientation, int, long)} in
     * combination with {@link #withExecutionAllowed(boolean)} and
     * {@link #withReroutingType(ReroutingType)} instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed.")
    public Step(
        @Nullable
        Path path,
        @Nullable
        Point srcPoint,
        @Nonnull
        Point destPoint,
        @Nonnull
        Vehicle.Orientation orientation,
        int routeIndex,
        boolean executionAllowed,
        @Nullable
        ReroutingType reroutingType
    ) {
      this(
          path, srcPoint, destPoint, orientation, routeIndex, 0, executionAllowed,
          reroutingType
      );
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param srcPoint The point that the vehicle is starting from.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     * @param executionAllowed Whether execution of this step is allowed.
     * @deprecated Use {@link #Step(Path, Point, Point, Vehicle.Orientation, int, long)} in
     * combination with {@link #withExecutionAllowed(boolean)} instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed.")
    public Step(
        @Nullable
        Path path,
        @Nullable
        Point srcPoint,
        @Nonnull
        Point destPoint,
        @Nonnull
        Vehicle.Orientation orientation,
        int routeIndex,
        boolean executionAllowed
    ) {
      this(
          path, srcPoint, destPoint, orientation, routeIndex, 0, executionAllowed, null
      );
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param srcPoint The point that the vehicle is starting from.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     * @deprecated Use {@link #Step(Path, Point, Point, Vehicle.Orientation, int, long)} instead.
     */
    @Deprecated
    @ScheduledApiChange(when = "7.0", details = "Will be removed.")
    public Step(
        @Nullable
        Path path,
        @Nullable
        Point srcPoint,
        @Nonnull
        Point destPoint,
        @Nonnull
        Vehicle.Orientation orientation,
        int routeIndex
    ) {
      this(path, srcPoint, destPoint, orientation, routeIndex, 0, true, null);
    }

    /**
     * Creates a new instance.
     * <p>
     * The created step will have its {@code executionAllowed} flag set to {@code true} and its
     * {@code reroutingType} set to {@code null}.
     * </p>
     *
     * @param path The path to travel.
     * @param srcPoint The point that the vehicle is starting from.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     * @param costs The costs for travelling the path.
     */
    public Step(
        @Nullable
        Path path,
        @Nullable
        Point srcPoint,
        @Nonnull
        Point destPoint,
        @Nonnull
        Vehicle.Orientation orientation,
        int routeIndex,
        long costs
    ) {
      this(path, srcPoint, destPoint, orientation, routeIndex, costs, true, null);
    }

    /**
     * Creates a new instance.
     *
     * @param path The path to travel.
     * @param srcPoint The point that the vehicle is starting from.
     * @param destPoint The point that is reached by travelling the path.
     * @param orientation The vehicle's orientation on this step.
     * @param routeIndex This step's index in the vehicle's route.
     * @param costs The costs for travelling the path.
     * @param executionAllowed Whether execution of this step is allowed.
     * @param reroutingType Marks this step as the origin of a recalculated route.
     */
    private Step(
        @Nullable
        Path path,
        @Nullable
        Point srcPoint,
        @Nonnull
        Point destPoint,
        @Nonnull
        Vehicle.Orientation orientation,
        int routeIndex,
        long costs,
        boolean executionAllowed,
        @Nullable
        ReroutingType reroutingType
    ) {
      this.path = path;
      this.sourcePoint = srcPoint;
      this.destinationPoint = requireNonNull(destPoint, "destPoint");
      this.vehicleOrientation = requireNonNull(orientation, "orientation");
      this.routeIndex = routeIndex;
      this.costs = costs;
      this.executionAllowed = executionAllowed;
      this.reroutingType = reroutingType;
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
     * Creates a copy of this object, with the given path.
     *
     * @param path The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withPath(
        @Nullable
        Path path
    ) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
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
     * Creates a copy of this object, with the given source point.
     *
     * @param sourcePoint The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withSourcePoint(
        @Nullable
        Point sourcePoint
    ) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
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
     * Creates a copy of this object, with the given destination point.
     *
     * @param destinationPoint The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withDestinationPoint(
        @Nonnull
        Point destinationPoint
    ) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
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
     * Creates a copy of this object, with the given vehicle orientation.
     *
     * @param vehicleOrientation The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withVehicleOrientation(
        @Nonnull
        Vehicle.Orientation vehicleOrientation
    ) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
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
     * Creates a copy of this object, with the given route index.
     *
     * @param routeIndex The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withRouteIndex(int routeIndex) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
    }

    /**
     * Returns the costs for travelling the path.
     *
     * @return The costs for travelling the path.
     */
    public long getCosts() {
      return costs;
    }

    /**
     * Creates a copy of this object, with the given costs.
     *
     * @param costs The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withCosts(long costs) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
    }

    /**
     * Returns whether execution of this step is allowed.
     *
     * @return {@code true}, if execution of this step is allowed, otherwise {@code false}.
     */
    public boolean isExecutionAllowed() {
      return executionAllowed;
    }

    /**
     * Creates a copy of this object, with the given execution allowed flag.
     *
     * @param executionAllowed The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withExecutionAllowed(boolean executionAllowed) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
    }

    /**
     * Returns the {@link ReroutingType} of this step.
     * <p>
     * Indicates whether this step is the origin of a recalculated route, and if so, which
     * {@link ReroutingType} was used to determine the (new) route.
     * <p>
     * Might return {@code null}, if this step is not the origin of a recalculated route.
     *
     * @return The {@link ReroutingType} of this step.
     */
    @Nullable
    public ReroutingType getReroutingType() {
      return reroutingType;
    }

    /**
     * Creates a copy of this object, with the given rerouting type.
     *
     * @param reroutingType The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Route.Step withReroutingType(
        @Nullable
        ReroutingType reroutingType
    ) {
      return new Route.Step(
          path,
          sourcePoint,
          destinationPoint,
          vehicleOrientation,
          routeIndex,
          costs,
          executionAllowed,
          reroutingType
      );
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Step other)) {
        return false;
      }
      return Objects.equals(path, other.path)
          && Objects.equals(sourcePoint, other.sourcePoint)
          && Objects.equals(destinationPoint, other.destinationPoint)
          && costs == other.getCosts()
          && Objects.equals(vehicleOrientation, other.vehicleOrientation)
          && routeIndex == other.routeIndex
          && executionAllowed == other.executionAllowed
          && reroutingType == other.reroutingType;
    }

    /**
     * Compares the given step to this step, ignoring rerouting-related properties.
     *
     * @param step The step to compare to.
     * @return {@code true}, if the given step is equal to this step (ignoring rerouting-related
     * properties), otherwise {@code false}.
     */
    public boolean equalsInMovement(Step step) {
      if (step == null) {
        return false;
      }
      return Objects.equals(this.getSourcePoint(), step.getSourcePoint())
          && Objects.equals(this.getDestinationPoint(), step.getDestinationPoint())
          && Objects.equals(this.getPath(), step.getPath())
          && Objects.equals(this.getVehicleOrientation(), step.getVehicleOrientation())
          && Objects.equals(this.getRouteIndex(), step.getRouteIndex());
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          path, sourcePoint, destinationPoint, vehicleOrientation, routeIndex, costs,
          executionAllowed, reroutingType
      );
    }

    @Override
    public String toString() {
      return destinationPoint.getName();
    }
  }
}
