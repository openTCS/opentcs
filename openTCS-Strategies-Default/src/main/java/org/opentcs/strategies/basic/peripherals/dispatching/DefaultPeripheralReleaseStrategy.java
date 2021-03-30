/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import java.util.Collection;
import java.util.stream.Collectors;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;

/**
 * The default implementation of {@link PeripheralReleaseStrategy}.
 * Selects peripherals to be released by applying the following rules:
 * <ul>
 * <li>A peripheral's state must be {@link PeripheralInformation.State#IDLE}</li>
 * <li>A peripheral's processing state must be {@link PeripheralInformation.ProcState#IDLE}</li>
 * <li>A peripheral's reservation token must be set.</li>
 * </ul>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultPeripheralReleaseStrategy
    implements PeripheralReleaseStrategy {

  @Override
  public Collection<Location> selectPeripheralsToRelease(Collection<Location> locations) {
    return locations.stream()
        .filter(this::idleAndReserved)
        .collect(Collectors.toSet());
  }

  private boolean idleAndReserved(Location location) {
    return processesNoJob(location) && hasReservationToken(location);
  }

  private boolean processesNoJob(Location location) {
    return location.getPeripheralInformation().getProcState()
        == PeripheralInformation.ProcState.IDLE
        && location.getPeripheralInformation().getState()
        == PeripheralInformation.State.IDLE;
  }

  private boolean hasReservationToken(Location location) {
    return location.getPeripheralInformation().getReservationToken() != null;
  }
}
