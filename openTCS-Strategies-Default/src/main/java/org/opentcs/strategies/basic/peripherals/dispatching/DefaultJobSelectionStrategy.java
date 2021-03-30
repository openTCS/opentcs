/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.opentcs.data.model.Location;
import org.opentcs.data.peripherals.PeripheralJob;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.Comparators;

/**
 * The default implementation of {@link JobSelectionStrategy}.
 * Selects a job by applying the following rules:
 * <ul>
 * <li>The location of a job's operation has to match the given location.</li>
 * <li>If this applies to multiple jobs, the oldest one is selected.</li>
 * </ul>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultJobSelectionStrategy
    implements JobSelectionStrategy {

  @Override
  public Optional<PeripheralJob> select(Collection<PeripheralJob> jobs, Location location) {
    checkArgument(jobs.stream().allMatch(job -> matchesLocation(job, location)),
                  "All jobs are expected to match the given location: %s", location.getName());

    return jobs.stream()
        .sorted(Comparators.jobsByAge())
        .findFirst();
  }

  private boolean matchesLocation(PeripheralJob job, Location location) {
    return Objects.equals(job.getPeripheralOperation().getLocation(), location.getReference());
  }
}
