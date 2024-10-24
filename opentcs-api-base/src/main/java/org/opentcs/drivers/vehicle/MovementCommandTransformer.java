/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
