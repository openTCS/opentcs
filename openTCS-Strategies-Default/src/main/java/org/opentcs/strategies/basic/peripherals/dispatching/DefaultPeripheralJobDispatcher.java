/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.peripherals.dispatching;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches peripheral jobs and peripheral devices represented by locations.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DefaultPeripheralJobDispatcher
    implements PeripheralJobDispatcher,
               PeripheralJobCallback {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultPeripheralJobDispatcher.class);
  /**
   * The peripheral service to use.
   */
  private final InternalPeripheralService peripheralService;
  /**
   * The peripheral job service to use.
   */
  private final InternalPeripheralJobService peripheralJobService;
  /**
   * The kernel's executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * Performs a full dispatch run.
   */
  private final FullDispatchTask fullDispatchTask;
  /**
   * A task to periodically trigger the job dispatcher.
   */
  private final Provider<PeriodicPeripheralRedispatchingTask> periodicDispatchTaskProvider;
  /**
   * The peripheral job dispatcher's configuration.
   */
  private final DefaultPeripheralJobDispatcherConfiguration configuration;
  /**
   * The future for the periodic dispatch task.
   */
  private ScheduledFuture<?> periodicDispatchTaskFuture;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param peripheralService The peripheral service to use.
   * @param peripheralJobService The peripheral job service to use.
   * @param kernelExecutor Executes dispatching tasks.
   * @param fullDispatchTask Performs a full dispatch run.
   * @param periodicDispatchTaskProvider A task to periodically trigger the job dispatcher.
   * @param configuration The peripheral job dispatcher's configuration.
   */
  @Inject
  public DefaultPeripheralJobDispatcher(
      InternalPeripheralService peripheralService,
      InternalPeripheralJobService peripheralJobService,
      @KernelExecutor ScheduledExecutorService kernelExecutor,
      FullDispatchTask fullDispatchTask,
      Provider<PeriodicPeripheralRedispatchingTask> periodicDispatchTaskProvider,
      DefaultPeripheralJobDispatcherConfiguration configuration) {
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
    this.peripheralJobService = requireNonNull(peripheralJobService, "peripheralJobService");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.fullDispatchTask = requireNonNull(fullDispatchTask, "fullDispatchTask");
    this.periodicDispatchTaskProvider = requireNonNull(periodicDispatchTaskProvider,
                                                       "periodicDispatchTaskProvider");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    LOG.debug("Initializing...");
    fullDispatchTask.initialize();

    LOG.debug("Scheduling periodic peripheral job dispatch task with interval of {} ms...",
              configuration.idlePeripheralRedispatchingInterval());
    periodicDispatchTaskFuture = kernelExecutor.scheduleAtFixedRate(
        periodicDispatchTaskProvider.get(),
        configuration.idlePeripheralRedispatchingInterval(),
        configuration.idlePeripheralRedispatchingInterval(),
        TimeUnit.MILLISECONDS
    );

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    LOG.debug("Terminating...");

    periodicDispatchTaskFuture.cancel(false);
    periodicDispatchTaskFuture = null;

    fullDispatchTask.terminate();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void dispatch() {
    LOG.debug("Scheduling dispatch task...");
    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(fullDispatchTask);
  }

  @Override
  public void withdrawJob(Location location) {
    requireNonNull(location, "location");
    checkState(isInitialized(), "Not initialized");

    // Schedule this to be executed by the kernel executor.
    kernelExecutor.submit(() -> {
      LOG.debug("Scheduling withdrawal for location '{}'...", location.getName());

      // TODO Abort peripheral job
    });
  }

  @Override
  public void peripheralJobFinished(PeripheralJob job) {
    kernelExecutor.submit(() -> {
      peripheralJobService.updatePeripheralJobState(job.getReference(),
                                                    PeripheralJob.State.FINISHED);
      peripheralService.updatePeripheralProcState(job.getPeripheralOperation().getLocation(),
                                                  PeripheralInformation.ProcState.IDLE);
      peripheralService.updatePeripheralJob(job.getPeripheralOperation().getLocation(),
                                            null);
      dispatch();
    });
  }

  @Override
  public void peripheralJobFailed(PeripheralJob job) {
    kernelExecutor.submit(() -> {
      peripheralJobService.updatePeripheralJobState(job.getReference(),
                                                    PeripheralJob.State.FAILED);
      peripheralService.updatePeripheralProcState(job.getPeripheralOperation().getLocation(),
                                                  PeripheralInformation.ProcState.IDLE);
      peripheralService.updatePeripheralJob(job.getPeripheralOperation().getLocation(),
                                            null);
      dispatch();
    });
  }
}
