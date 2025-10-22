// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.lang.Math.toDegrees;
import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;

/**
 * Resolves the vehicle precise position to an openTCS point.
 */
public class VehiclePositionResolver {
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * This class' configuration.
   */
  private final VehiclePositionResolverConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param objectService Object service.
   * @param configuration The configuration.
   */
  @Inject
  public VehiclePositionResolver(
      @Nonnull
      TCSObjectService objectService,
      @Nonnull
      VehiclePositionResolverConfiguration configuration
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  /**
   * Resolves the given precise position to the name of a point in the plant model.
   * <p>
   * If provided, the last known position is first checked to see if it already matches the
   * given precise position. If this check succeeds, the last known position is returned. If
   * the last known position is not provided or does not match the provided precise position,
   * the name of the first point in the plant model that matches the given precise position is
   * returned.
   * </p>
   *
   * @param lastKnownPosition The name of the vehicle's last known position. May be {@code null}
   * if the last position is not known.
   * @param precisePosition The vehicle's current precise position.
   * @return The name of the resolved point or the last known position if no point could be
   * found.
   */
  @Nullable
  public String resolveVehiclePosition(
      @Nonnull
      Pose precisePosition,
      @Nullable
      String lastKnownPosition
  ) {
    requireNonNull(precisePosition, "precisePosition");

    if (isCurrentLogicalPositionCorrect(precisePosition, lastKnownPosition)) {
      return lastKnownPosition;
    }

    for (Point p : objectService.fetch(Point.class)) {
      if (isPointAtPrecisePosition(p, precisePosition)) {
        return p.getName();
      }
    }
    return lastKnownPosition;
  }

  private boolean isCurrentLogicalPositionCorrect(
      @Nonnull
      Pose precisePosition,
      @Nullable
      String lastKnownPosition
  ) {
    if (lastKnownPosition == null) {
      return false;
    }

    Point point = objectService.fetch(Point.class, lastKnownPosition).orElse(null);
    if (point == null) {
      return false;
    }

    return isPointAtPrecisePosition(point, precisePosition);
  }

  private boolean isPointAtPrecisePosition(Point p, Pose precisePosition) {
    return isWithinDeviationXY(p, precisePosition)
        && isWithinDeviationTheta(p, precisePosition);
  }

  private boolean isWithinDeviationXY(Point p, Pose precisePosition) {
    double deviationX = Math.abs(
        p.getPose().getPosition().getX()
            - precisePosition.getPosition().getX()
    );
    double deviationY = Math.abs(
        p.getPose().getPosition().getY()
            - precisePosition.getPosition().getY()
    );

    return Math.sqrt((deviationX * deviationX) + (deviationY * deviationY))
        <= configuration.deviationXY();
  }

  private boolean isWithinDeviationTheta(Point p, Pose precisePosition) {
    if (Double.isNaN(p.getPose().getOrientationAngle())) {
      return true;
    }
    return angleBetween(
        p.getPose().getOrientationAngle(), toDegrees(precisePosition.getOrientationAngle())
    ) <= configuration.deviationTheta();
  }

  private double angleBetween(double angle1, double angle2) {
    double difference = Math.abs(angle1 - angle2) % 360;
    if (difference > 180) {
      return Math.abs(difference - 360);
    }
    return difference;
  }
}
