/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralControllerPool;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides service functions for working with peripheral jobs and their states.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralJobUtil {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralJobUtil.class);
  /**
   * The peripheral service to use.
   */
  private final InternalPeripheralService peripheralService;
  /**
   * The peripheral job service to use.
   */
  private final InternalPeripheralJobService peripheralJobService;
  /**
   * The peripheral controller pool.
   */
  private final PeripheralControllerPool peripheralControllerPool;
  /**
   * The peripheral job callback to use.
   */
  private final PeripheralJobCallback peripheralJobCallback;

  @Inject
  public PeripheralJobUtil(InternalPeripheralService peripheralService,
                           InternalPeripheralJobService peripheralJobService,
                           PeripheralControllerPool peripheralControllerPool,
                           PeripheralJobCallback peripheralJobCallback) {
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
    this.peripheralJobService = requireNonNull(peripheralJobService, "peripheralJobService");
    this.peripheralControllerPool = requireNonNull(peripheralControllerPool,
                                                   "peripheralControllerPool");
    this.peripheralJobCallback = requireNonNull(peripheralJobCallback, "peripheralJobCallback");
  }

  public void assignPeripheralJob(Location location, PeripheralJob peripheralJob) {
    requireNonNull(location, "location");
    requireNonNull(peripheralJob, "peripheralJob");

    LOG.debug("Assigning location {} to job {}.", location.getName(), peripheralJob.getName());
    final TCSResourceReference<Location> locationRef = location.getReference();
    final TCSObjectReference<PeripheralJob> jobRef = peripheralJob.getReference();
    // Set the locations's and peripheral job's state.
    peripheralService.updatePeripheralProcState(locationRef,
                                                PeripheralInformation.ProcState.PROCESSING_JOB);
    peripheralService.updatePeripheralReservationToken(locationRef,
                                                       peripheralJob.getReservationToken());
    peripheralService.updatePeripheralJob(locationRef, jobRef);
    peripheralJobService.updatePeripheralJobState(jobRef, PeripheralJob.State.BEING_PROCESSED);

    peripheralControllerPool.getPeripheralController(locationRef).process(peripheralJob,
                                                                          peripheralJobCallback);
  }
}
