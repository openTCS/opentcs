/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;

/**
 * Transforms {@link Pose}s by subtracting offsets in a given
 * {@link CoordinateSystemTransformation}.
 */
public class CoordinateSystemIncomingPoseTransformer
    implements
      IncomingPoseTransformer {

  private final CoordinateSystemTransformation transformation;

  public CoordinateSystemIncomingPoseTransformer(
      @Nonnull
      CoordinateSystemTransformation transformation
  ) {
    this.transformation = requireNonNull(transformation, "transformation");
  }

  @Override
  public Pose apply(
      @Nonnull
      Pose pose
  ) {
    requireNonNull(pose, "pose");

    return pose
        .withPosition(
            new Triple(
                pose.getPosition().getX() - transformation.getOffsetX(),
                pose.getPosition().getY() - transformation.getOffsetY(),
                pose.getPosition().getZ() - transformation.getOffsetZ()
            )
        )
        .withOrientationAngle(
            (pose.getOrientationAngle() - transformation.getOffsetOrientation()) % 360.0
        );
  }

}
