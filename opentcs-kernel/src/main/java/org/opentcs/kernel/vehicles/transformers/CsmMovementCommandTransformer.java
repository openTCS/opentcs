// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.MovementCommandTransformer;

/**
 * Transforms coordinates in {@link MovementCommand}s (i.e. coordinates in the plant model's
 * coordinate system) to coordinates in the vehicle's coordinate system as described by the provided
 * {@link CoordinateSystemMapping}.
 */
public class CsmMovementCommandTransformer
    implements
      MovementCommandTransformer {

  private final CoordinateSystemMapping mapping;
  private final AffineTransform affineTransform;

  public CsmMovementCommandTransformer(
      @Nonnull
      CoordinateSystemMapping mapping
  ) {
    this.mapping = requireNonNull(mapping, "mapping");
    affineTransform = new AffineTransform();
    affineTransform.rotate(Math.toRadians(-mapping.getRotationZ()));
    affineTransform.translate(-mapping.getTranslationX(), -mapping.getTranslationY());
  }

  @Override
  public MovementCommand apply(MovementCommand command) {
    return command
        .withTransportOrder(transformTransportOrder(command.getTransportOrder()))
        .withDriveOrder(transformDriveOrder(command.getDriveOrder()))
        .withFinalDestination(transformPoint(command.getFinalDestination()))
        .withFinalDestinationLocation(transformLocation(command.getFinalDestinationLocation()))
        .withOpLocation(transformLocation(command.getOpLocation()))
        .withStep(transformStep(command.getStep()));
  }

  private TransportOrder transformTransportOrder(TransportOrder oldTransportOrder) {
    return oldTransportOrder.withDriveOrders(
        transformDriveOrders(oldTransportOrder.getAllDriveOrders())
    );
  }

  private List<DriveOrder> transformDriveOrders(List<DriveOrder> oldDriveOrders) {
    return oldDriveOrders.stream()
        .map(this::transformDriveOrder)
        .toList();
  }

  private DriveOrder transformDriveOrder(DriveOrder oldOrder) {
    return oldOrder.withRoute(transformRoute(oldOrder.getRoute()));
  }

  private Route transformRoute(Route route) {
    return Optional.ofNullable(route)
        .map(
            originalRoute -> new Route(
                originalRoute.getSteps().stream()
                    .map(step -> transformStep(step))
                    .collect(Collectors.toList())
            )
        )
        .orElse(null);
  }

  private Route.Step transformStep(Route.Step oldStep) {
    return oldStep
        .withSourcePoint(transformPoint(oldStep.getSourcePoint()))
        .withDestinationPoint(transformPoint(oldStep.getDestinationPoint()));
  }

  private Point transformPoint(Point point) {
    return Optional.ofNullable(point)
        .map(
            originalPoint -> originalPoint.withPose(
                transformPose(originalPoint.getPose())
            )
        )
        .orElse(null);
  }

  private Location transformLocation(Location location) {
    return Optional.ofNullable(location)
        .map(
            originalLocation -> originalLocation.withPosition(
                transformTriple(originalLocation.getPosition())
            )
        )
        .orElse(null);
  }

  private Pose transformPose(Pose pose) {
    return new Pose(
        transformTriple(pose.getPosition()),
        (pose.getOrientationAngle() - mapping.getRotationZ()) % 360
    );
  }

  private Triple transformTriple(Triple triple) {
    return Optional.ofNullable(triple)
        .map(
            trpl -> {
              Point2D srcPoint = new Point2D.Double(trpl.getX(), trpl.getY());
              Point2D destPoint = new Point2D.Double();
              affineTransform.transform(srcPoint, destPoint);
              return new Triple(
                  Math.round(destPoint.getX()),
                  Math.round(destPoint.getY()),
                  trpl.getZ() - mapping.getTranslationZ()
              );
            }
        )
        .orElse(null);
  }
}
