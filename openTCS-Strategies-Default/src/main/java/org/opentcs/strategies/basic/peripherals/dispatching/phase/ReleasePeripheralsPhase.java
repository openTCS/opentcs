/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching.phase;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.data.model.Location;
import org.opentcs.strategies.basic.peripherals.dispatching.PeripheralDispatcherPhase;
import org.opentcs.strategies.basic.peripherals.dispatching.PeripheralReleaseStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Releases the reservations of peripherals.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ReleasePeripheralsPhase
    implements PeripheralDispatcherPhase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReleasePeripheralsPhase.class);
  /**
   * The peripheral service.
   */
  private final InternalPeripheralService peripheralService;
  /**
   * The release strategy to use.
   */
  private final PeripheralReleaseStrategy releaseStrategy;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public ReleasePeripheralsPhase(InternalPeripheralService peripheralService,
                                 PeripheralReleaseStrategy releaseStrategy) {
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
    this.releaseStrategy = requireNonNull(releaseStrategy, "releaseStrategy");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  @Override
  public void run() {
    Collection<Location> peripheralsToBeRelease
        = releaseStrategy.selectPeripheralsToRelease(peripheralService.fetchObjects(Location.class));
    for (Location location : peripheralsToBeRelease) {
      releasePeripheral(location);
    }
  }

  private void releasePeripheral(Location location) {
    LOG.debug("Releasing peripheral '{}'...", location.getName());
    peripheralService.updatePeripheralReservationToken(location.getReference(), null);
  }
}
