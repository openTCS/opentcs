/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleOrderSelectionStrategy {

  @Nullable
  VehicleOrderSelection selectOrder(@Nonnull Vehicle vehicle);
}
