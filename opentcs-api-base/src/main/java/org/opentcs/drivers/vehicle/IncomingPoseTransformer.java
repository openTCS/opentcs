// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import java.util.function.UnaryOperator;
import org.opentcs.data.model.Pose;

/**
 * Transforms a {@link Pose} received by a vehicle to one in the plant model coordinate system.
 */
public interface IncomingPoseTransformer
    extends
      UnaryOperator<Pose> {

  @Override
  Pose apply(Pose pose);
}
