// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.components.kernel.services.PeripheralDispatcherService;
import org.opentcs.components.kernel.services.PeripheralJobService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Handles requests related to peripheral job dispatching.
 */
public class PeripheralJobDispatcherHandler {

  private final PeripheralJobService jobService;
  private final PeripheralDispatcherService jobDispatcherService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param jobService Used to create peripheral jobs.
   * @param jobDispatcherService Used to dispatch peripheral jobs.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public PeripheralJobDispatcherHandler(
      PeripheralJobService jobService,
      PeripheralDispatcherService jobDispatcherService,
      KernelExecutorWrapper executorWrapper
  ) {
    this.jobService = requireNonNull(jobService, "jobService");
    this.jobDispatcherService = requireNonNull(jobDispatcherService, "jobDispatcherService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  public void triggerJobDispatcher() {
    executorWrapper.callAndWait(() -> jobDispatcherService.dispatch());
  }

  public void withdrawPeripheralJobByLocation(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      Location location = jobService.fetch(Location.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown location: " + name));

      jobDispatcherService.withdrawByLocation(location.getReference());
    });
  }

  public void withdrawPeripheralJob(String name)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      PeripheralJob job = jobService.fetch(PeripheralJob.class, name)
          .orElseThrow(() -> new ObjectUnknownException("Unknown peripheral job: " + name));

      jobDispatcherService.withdrawByPeripheralJob(job.getReference());
    });
  }
}
