// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.MovementCommandTransformer;

/**
 * Transforms coordinates in {@link MovementCommand}s by adding offsets in a given
 * {@link CoordinateSystemTransformation}.
 */
public class CoordinateSystemMovementCommandTransformer
    implements
      MovementCommandTransformer {

  private final CoordinateSystemTransformation transformation;

  public CoordinateSystemMovementCommandTransformer(
      @Nonnull
      CoordinateSystemTransformation transformation
  ) {
    this.transformation = requireNonNull(transformation, "transformation");
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
                    .collect(Collectors.toList()),
                originalRoute.getCosts()
            )
        )
        .orElse(null);
  }

  private Step transformStep(Step oldStep) {
    return new Step(
        oldStep.getPath(),
        transformPoint(oldStep.getSourcePoint()),
        transformPoint(oldStep.getDestinationPoint()),
        oldStep.getVehicleOrientation(),
        oldStep.getRouteIndex(),
        oldStep.isExecutionAllowed(),
        oldStep.getReroutingType()
    );
  }

  private Point transformPoint(Point point) {
    return Optional.ofNullable(point)
        .map(
            originalPoint -> originalPoint.withPose(
                new Pose(
                    transformTriple(originalPoint.getPose().getPosition()),
                    (originalPoint.getPose().getOrientationAngle() + transformation
                        .getOffsetOrientation()) % 360.0
                )
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

  private Triple transformTriple(Triple triple) {
    return Optional.ofNullable(triple)
        .map(
            originalTriple -> new Triple(
                originalTriple.getX() + transformation.getOffsetX(),
                originalTriple.getY() + transformation.getOffsetY(),
                originalTriple.getZ() + transformation.getOffsetZ()
            )
        )
        .orElse(null);
  }
}
