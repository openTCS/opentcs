// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import jakarta.annotation.Nonnull;
import org.opentcs.components.kernel.PositionDeviationPolicy;
import org.opentcs.data.model.Point;

/**
 * A position deviation policy with a fixed given deviation.
 *
 * @param distance The allowed deviation distance in millimeters.
 * @param angle The allowed deviation angle in degrees.
 */
public record FixedPositionDeviationPolicy(long distance, long angle)
    implements
      PositionDeviationPolicy {

  @Override
  public long allowedDeviationDistance(
      @Nonnull
      Point point
  ) {
    return distance;
  }

  @Override
  public long allowedDeviationAngle(
      @Nonnull
      Point point
  ) {
    return angle;
  }
}
