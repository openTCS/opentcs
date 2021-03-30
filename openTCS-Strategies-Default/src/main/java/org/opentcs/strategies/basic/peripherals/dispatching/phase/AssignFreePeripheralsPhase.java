/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching.phase;

import java.util.Collection;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralControllerPool;
import org.opentcs.strategies.basic.peripherals.dispatching.JobSelectionStrategy;
import org.opentcs.strategies.basic.peripherals.dispatching.PeripheralDispatcherPhase;
import org.opentcs.strategies.basic.peripherals.dispatching.PeripheralJobUtil;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assigns peripheral jobs to peripheral devices that are currently not processing any and are
 * not reserved for any reservation token.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AssignFreePeripheralsPhase
    implements PeripheralDispatcherPhase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AssignFreePeripheralsPhase.class);
  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * The strategy to use for selecting jobs for peripheral devices.
   */
  private final JobSelectionStrategy jobSelectionStrategy;
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
  public AssignFreePeripheralsPhase(TCSObjectService objectService,
                                    JobSelectionStrategy jobSelectionStrategy,
                                    PeripheralControllerPool peripheralControllerPool,
                                    PeripheralJobUtil peripheralJobUtil) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.jobSelectionStrategy = requireNonNull(jobSelectionStrategy, "jobSelectionStrategy");
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
    Set<Location> availablePeripherals = objectService.fetchObjects(Location.class,
                                                                    this::availableForAnyJob);
    if (availablePeripherals.isEmpty()) {
      LOG.debug("No peripherals available, skipping potentially expensive fetching of jobs.");
      return;
    }
    Set<PeripheralJob> jobsToBeProcessed = objectService.fetchObjects(PeripheralJob.class,
                                                                      this::toBeProcessed);
    LOG.debug("Available for dispatching: {} peripheral jobs and {} peripheral devices.",
              jobsToBeProcessed.size(),
              availablePeripherals.size());

    for (Location location : availablePeripherals) {
      tryAssignJob(location, jobsToBeProcessed);
    }
  }

  private boolean availableForAnyJob(Location location) {
    return processesNoJob(location) && !hasReservationToken(location);
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

  private boolean toBeProcessed(PeripheralJob job) {
    return job.getState() == PeripheralJob.State.TO_BE_PROCESSED;
  }

  private void tryAssignJob(Location location, Collection<PeripheralJob> availableJobs) {
    LOG.debug("Trying to find job for peripheral '{}'...", location.getName());
    Optional<PeripheralJob> selectedJob = jobSelectionStrategy.select(
        availableJobs.stream()
            .filter(job -> matchesLocation(job, location))
            .collect(Collectors.toList()),
        location
    );
    if (selectedJob.isEmpty()) {
      return;
    }

    ExplainedBoolean canProcess = canProcess(location, selectedJob.get());
    if (!canProcess.getValue()) {
      LOG.debug("{} cannot process peripheral job {}: {}",
                location.getName(),
                selectedJob.get().getName(),
                canProcess.getReason());
      return;
    }

    assignJob(selectedJob.get(), location);
  }

  private boolean matchesLocation(PeripheralJob job, Location location) {
    return Objects.equals(job.getPeripheralOperation().getLocation(),
                          location.getReference());
  }

  private ExplainedBoolean canProcess(Location location, PeripheralJob job) {
    return peripheralControllerPool
        .getPeripheralController(location.getReference()).canProcess(job);
  }

  private void assignJob(PeripheralJob job, Location location) {
    LOG.debug("Assigning job '{}' to peripheral '{}'...", job.getName(), location.getName());
    peripheralJobUtil.assignPeripheralJob(location, job);
  }
}
