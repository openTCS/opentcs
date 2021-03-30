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
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically checks for idle peripheral devices that could process a peripheral job.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeriodicPeripheralRedispatchingTask
    implements Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeriodicPeripheralRedispatchingTask.class);

  private final PeripheralDispatcherService dispatcherService;

  private final TCSObjectService objectService;

  /**
   * Creates a new instance.
   *
   * @param dispatcherService The dispatcher service used to dispatch peripheral devices.
   * @param objectService The object service.
   */
  @Inject
  public PeriodicPeripheralRedispatchingTask(PeripheralDispatcherService dispatcherService,
                                             TCSObjectService objectService) {
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.objectService = requireNonNull(objectService, "objectService");
  }

  @Override
  public void run() {
    // If there are any peripheral devices that could process a peripheral job,
    // trigger the dispatcher once.
    objectService.fetchObjects(Location.class, this::couldProcessJob).stream()
        .findAny()
        .ifPresent(location -> {
          LOG.debug("Peripheral {} could process peripheral job, triggering dispatcher ...",
                    location);
          dispatcherService.dispatch();
        });
  }

  private boolean couldProcessJob(Location location) {
    return location.getPeripheralInformation().getState() != PeripheralInformation.State.NO_PERIPHERAL
        && (processesNoJob(location));
  }

  private boolean processesNoJob(Location location) {
    return location.getPeripheralInformation().getProcState()
        == PeripheralInformation.ProcState.IDLE
        && location.getPeripheralInformation().getState()
        == PeripheralInformation.State.IDLE;
  }
}
