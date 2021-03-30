/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching.phase;

import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralControllerPool;
import org.opentcs.strategies.basic.peripherals.dispatching.PeripheralDispatcherPhase;
import org.opentcs.strategies.basic.peripherals.dispatching.PeripheralJobUtil;
import org.opentcs.util.Comparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assigns the next peripheral job that matches a peripheral's reservation token to peripherals that
 * are currently not processing any.
 * Peripherals with no reservation token set are not cosidered in this phase.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AssignReservedPeripheralsPhase
    implements PeripheralDispatcherPhase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AssignReservedPeripheralsPhase.class);
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * The peripheral controller pool.
   */
  private final PeripheralControllerPool peripheralControllerPool;
  /**
   * Provides service functions for working with peripheral jobs and their states.
   */
  private final PeripheralJobUtil peripheralJobUtil;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignReservedPeripheralsPhase(TCSObjectService objectService,
                                        PeripheralControllerPool peripheralControllerPool,
                                        PeripheralJobUtil peripheralJobUtil) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.peripheralControllerPool = requireNonNull(peripheralControllerPool,
                                                   "peripheralControllerPool");
    this.peripheralJobUtil = requireNonNull(peripheralJobUtil, "peripheralJobUtil");
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
    for (Location location : objectService.fetchObjects(Location.class,
                                                        this::reservedAndAvailable)) {
      checkForReservedJobs(location);
    }
  }

  private boolean reservedAndAvailable(Location location) {
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

  private void checkForReservedJobs(Location location) {
    objectService.fetchObjects(PeripheralJob.class, this::toBeProcessed).stream()
        .filter(job -> matchesReservationToken(job, location))
        .filter(job -> matchesLocation(job, location))
        .filter(job -> canProcess(location, job))
        .sorted(Comparators.jobsByAge())
        .findFirst()
        .ifPresent(job -> assignJob(job, location));
  }

  private boolean toBeProcessed(PeripheralJob job) {
    return job.getState() == PeripheralJob.State.TO_BE_PROCESSED;
  }

  private boolean matchesReservationToken(PeripheralJob job, Location location) {
    return Objects.equals(job.getReservationToken(),
                          location.getPeripheralInformation().getReservationToken());
  }

  private boolean matchesLocation(PeripheralJob job, Location location) {
    return Objects.equals(job.getPeripheralOperation().getLocation(),
                          location.getReference());
  }

  private boolean canProcess(Location location, PeripheralJob job) {
    return peripheralControllerPool.getPeripheralController(location.getReference())
        .canProcess(job).getValue();
  }

  private void assignJob(PeripheralJob job, Location location) {
    LOG.debug("Assigning job '{}' to peripheral '{}'...", job.getName(), location.getName());
    peripheralJobUtil.assignPeripheralJob(location, job);
  }
}
