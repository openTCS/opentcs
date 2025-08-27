// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Optional;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.drivers.vehicle.IncomingPoseTransformer;

/**
 * Transforms coordinates in incoming {@link Pose}s (i.e. coordinates in the vehicle's coordinate
 * system) to coordinates in the plant model's coordinates system as described by the provided
 * {@link CoordinateSystemMapping}
 */
public class CsmIncomingPoseTransformer
    implements
      IncomingPoseTransformer {

  private final CoordinateSystemMapping mapping;
  private final AffineTransform affineTransform;

  public CsmIncomingPoseTransformer(
      @Nonnull
      CoordinateSystemMapping mapping
  ) {
    this.mapping = requireNonNull(mapping, "mapping");
    affineTransform = new AffineTransform();
    affineTransform.translate(mapping.getTranslationX(), mapping.getTranslationY());
    affineTransform.rotate(Math.toRadians(mapping.getRotationZ()));
  }

  @Override
  public Pose apply(
      @Nonnull
      Pose pose
  ) {
    requireNonNull(pose, "pose");

    return transformPose(pose);
  }

  private Pose transformPose(Pose pose) {
    return new Pose(
        transformTriple(pose.getPosition()),
        (pose.getOrientationAngle() + mapping.getRotationZ()) % 360
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
                  trpl.getZ() + mapping.getTranslationZ()
              );
            }
        )
        .orElse(null);
  }
}
