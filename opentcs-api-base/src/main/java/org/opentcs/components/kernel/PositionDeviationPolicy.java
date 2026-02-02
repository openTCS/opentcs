// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.kernel;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.Point;

/**
 * Provides allowed position deviation data for a single vehicle on given points.
 * <p>
 * Which vehicle this policy provides deviation data for is determined when the policy is created by
 * a {@link PositionDeviationPolicyFactory}.
 * </p>
 */
public interface PositionDeviationPolicy {

  /**
   * Returns the allowed deviation distance for the given point, in millimeters.
   *
   * @param point The point.
   * @return The allowed deviation distance.
   */
  long allowedDeviationDistance(
      @Nonnull
      Point point
  );

  /**
   * Returns the allowed deviation angle for the given point, in degrees.
   *
   * @param point The point.
   * @return The allowed deviation angle.
   */
  long allowedDeviationAngle(
      @Nonnull
      Point point
  );
}
