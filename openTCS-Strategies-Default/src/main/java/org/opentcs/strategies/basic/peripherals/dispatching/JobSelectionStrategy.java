/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import java.util.Collection;
import java.util.Optional;
import org.opentcs.data.model.Location;
import org.opentcs.data.peripherals.PeripheralJob;

/**
 * A strategy for selecting a peripheral job to be processed next.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface JobSelectionStrategy {

  /**
   * Selects a peripheral job to be processed next by the given location out of the given
   * collection of peripheral jobs.
   *
   * @param jobs The peripheral jobs to select from.
   * @param location The location to select a peripheral job for.
   * @return The selected peripheral job.
   */
  Optional<PeripheralJob> select(Collection<PeripheralJob> jobs, Location location);
}
