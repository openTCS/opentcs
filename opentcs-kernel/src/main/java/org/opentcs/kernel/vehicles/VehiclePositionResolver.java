// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;

import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.Optional;
import org.opentcs.components.kernel.PositionDeviationPolicy;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;

/**
 * Resolves the vehicle precise position to an openTCS point.
 */
public class VehiclePositionResolver {
  /**
   * The object service.
   */
  private final InternalTCSObjectService objectService;
  /**
   * The position deviation policy to be used.
   */
  private final PositionDeviationPolicy positionDeviationPolicy;

  /**
   * Creates a new instance.
   *
   * @param objectService Object service.
   * @param positionDeviationPolicy The position deviation policy to be used.
   */
  @Inject
  public VehiclePositionResolver(
      @Nonnull
      InternalTCSObjectService objectService,
      @Assisted
      @Nonnull
      PositionDeviationPolicy positionDeviationPolicy
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.positionDeviationPolicy
        = requireNonNull(positionDeviationPolicy, "positionDeviationPolicy");
  }

  /**
   * Resolves the given precise position to the name of a point in the plant model.
   * <p>
   * If provided, the last known position is first checked to see if it already matches the
   * given precise position. If this check succeeds, the last known position is returned. If
   * the last known position is not provided or does not match the provided precise position,
   * the name of the closest point in the plant model that matches the given precise position is
   * returned.
   * </p>
   *
   * @param precisePosition The vehicle's current precise position.
   * @param lastKnownPosition The name of the vehicle's last known position. May be {@code null}
   * if the last position is not known.
   * @return The name of the resolved point or the last known position if no point could be
   * found.
   */
  @Nonnull
  public Optional<String> resolveVehiclePosition(
      @Nonnull
      Pose precisePosition,
      @Nullable
      String lastKnownPosition
  ) {
    requireNonNull(precisePosition, "precisePosition");

    if (isCurrentLogicalPositionCorrect(precisePosition, lastKnownPosition)) {
      return Optional.of(lastKnownPosition);
    }

    // From all points with acceptable deviation, select the closest one.
    return objectService.stream(Point.class)
        .filter(point -> isPointAtPrecisePosition(point, precisePosition))
        .min(Comparator.comparingDouble(p -> euclideanDistance2D(p.getPose(), precisePosition)))
        .map(Point::getName)
        .or(() -> Optional.ofNullable(lastKnownPosition));
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
    return euclideanDistance2D(p.getPose(), precisePosition)
        <= positionDeviationPolicy.allowedDeviationDistance(p);
  }

  private boolean isWithinDeviationTheta(Point p, Pose precisePosition) {
    if (Double.isNaN(p.getPose().getOrientationAngle())) {
      return true;
    }
    return angleBetween(
        p.getPose().getOrientationAngle(), precisePosition.getOrientationAngle()
    ) <= positionDeviationPolicy.allowedDeviationAngle(p);
  }

  private static double euclideanDistance2D(Pose pose1, Pose pose2) {
    double distanceX = pose1.getPosition().getX() - pose2.getPosition().getX();
    double distanceY = pose1.getPosition().getY() - pose2.getPosition().getY();
    return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
  }

  private static double angleBetween(double angle1, double angle2) {
    double difference = Math.abs(angle1 - angle2) % 360;
    if (difference > 180) {
      return Math.abs(difference - 360);
    }
    return difference;
  }
}
