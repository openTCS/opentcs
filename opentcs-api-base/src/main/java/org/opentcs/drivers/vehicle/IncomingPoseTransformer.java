/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
