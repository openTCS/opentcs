// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;

/**
 * Transforms {@link Pose}s by subtracting offsets in a given
 * {@link CoordinateSystemTransformation}.
 *
 * @deprecated Use {@link CsmIncomingPoseTransformer} instead.
 */
@Deprecated
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
        .withPosition(transformTriple(pose.getPosition()))
        .withOrientationAngle(
            (pose.getOrientationAngle() - transformation.getOffsetOrientation()) % 360.0
        );
  }

  private Triple transformTriple(Triple triple) {
    return Optional.ofNullable(triple)
        .map(
            originalTriple -> new Triple(
                originalTriple.getX() - transformation.getOffsetX(),
                originalTriple.getY() - transformation.getOffsetY(),
                originalTriple.getZ() - transformation.getOffsetZ()
            )
        )
        .orElse(null);
  }
}
