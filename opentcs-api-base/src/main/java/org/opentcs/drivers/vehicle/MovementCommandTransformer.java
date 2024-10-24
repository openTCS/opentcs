// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import java.util.function.UnaryOperator;

/**
 * Transforms contents of a {@link MovementCommand} before it is sent to a vehicle, thereby
 * transforming coordinates in the plant model coordinate system to coordinates in the vehicle's
 * coordinate system.
 */
public interface MovementCommandTransformer
    extends
      UnaryOperator<MovementCommand> {

  @Override
  MovementCommand apply(MovementCommand command);

}
