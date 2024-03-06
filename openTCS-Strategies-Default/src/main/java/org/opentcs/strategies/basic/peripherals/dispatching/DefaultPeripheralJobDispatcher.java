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
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.components.kernel.services.InternalPeripheralJobService;
import org.opentcs.components.kernel.services.InternalPeripheralService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.PeripheralControllerPool;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches peripheral jobs and peripheral devices represented by locations.
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
   * The controller pool.
   */
  private final PeripheralControllerPool controllerPool;
  /**
   * Where we register for application events.
   */
  private final EventSource eventSource;
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
   * A provider for an event handler to trigger the job dispatcher on certain events.
   */
  private final Provider<ImplicitDispatchTrigger> implicitDispatchTriggerProvider;
  /**
   * The peripheral job dispatcher's configuration.
   */
  private final DefaultPeripheralJobDispatcherConfiguration configuration;
  /**
   * The future for the periodic dispatch task.
   */
  private ScheduledFuture<?> periodicDispatchTaskFuture;
  /**
   * An event handler to trigger the job dispatcher on certain events.
   */
  private ImplicitDispatchTrigger implicitDispatchTrigger;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param peripheralService The peripheral service to use.
   * @param peripheralJobService The peripheral job service to use.
   * @param controllerPool The controller pool.
   * @param eventSource Where this instance registers for application events.
   * @param kernelExecutor Executes dispatching tasks.
   * @param fullDispatchTask Performs a full dispatch run.
   * @param periodicDispatchTaskProvider A task to periodically trigger the job dispatcher.
   * @param implicitDispatchTriggerProvider A provider for an event handler to trigger the job
   * dispatcher on certain events.
   * @param configuration The peripheral job dispatcher's configuration.
   */
  @Inject
  public DefaultPeripheralJobDispatcher(
      InternalPeripheralService peripheralService,
      InternalPeripheralJobService peripheralJobService,
      PeripheralControllerPool controllerPool,
      @ApplicationEventBus EventSource eventSource,
      @KernelExecutor ScheduledExecutorService kernelExecutor,
      FullDispatchTask fullDispatchTask,
      Provider<PeriodicPeripheralRedispatchingTask> periodicDispatchTaskProvider,
      Provider<ImplicitDispatchTrigger> implicitDispatchTriggerProvider,
      DefaultPeripheralJobDispatcherConfiguration configuration) {
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
    this.peripheralJobService = requireNonNull(peripheralJobService, "peripheralJobService");
    this.controllerPool = requireNonNull(controllerPool, "controllerPool");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.fullDispatchTask = requireNonNull(fullDispatchTask, "fullDispatchTask");
    this.periodicDispatchTaskProvider = requireNonNull(periodicDispatchTaskProvider,
                                                       "periodicDispatchTaskProvider");
    this.implicitDispatchTriggerProvider = requireNonNull(implicitDispatchTriggerProvider,
                                                          "implicitDispatchTriggerProvider");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    LOG.debug("Initializing...");
    fullDispatchTask.initialize();

    implicitDispatchTrigger = implicitDispatchTriggerProvider.get();
    eventSource.subscribe(implicitDispatchTrigger);

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

    eventSource.unsubscribe(implicitDispatchTrigger);
    implicitDispatchTrigger = null;

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
    fullDispatchTask.run();
  }

  @Override
  public void withdrawJob(Location location) {
    requireNonNull(location, "location");
    checkState(isInitialized(), "Not initialized");

    LOG.debug("Withdrawing peripheral job for location '{}' ({})...",
              location.getName(),
              location.getPeripheralInformation().getPeripheralJob());
    if (location.getPeripheralInformation().getPeripheralJob() == null) {
      return;
    }

    withdrawJob(peripheralService.fetchObject(
        PeripheralJob.class,
        location.getPeripheralInformation().getPeripheralJob())
    );
  }

  @Override
  public void withdrawJob(PeripheralJob job) {
    requireNonNull(job, "job");
    checkState(isInitialized(), "Not initialized");
    if (job.getState().isFinalState()) {
      LOG.info("Peripheral job '{}' already in final state '{}', skipping withdrawal.",
               job.getName(),
               job.getState());
      return;
    }
    checkArgument(
        !isRelatedToNonFinalTransportOrder(job),
        "Cannot withdraw job because it is related to transport order in non-final state: %s",
        job.getName()
    );

    LOG.debug("Withdrawing peripheral job '{}'...", job.getName());

    if (job.getState() == PeripheralJob.State.BEING_PROCESSED) {
      controllerPool
          .getPeripheralController(job.getPeripheralOperation().getLocation())
          .abortJob();
    }

    finalizeJob(job, PeripheralJob.State.FAILED);
  }

  private boolean isRelatedToNonFinalTransportOrder(PeripheralJob job) {
    return job.getRelatedTransportOrder() != null
        && !peripheralService.fetchObject(TransportOrder.class, job.getRelatedTransportOrder())
            .getState().isFinalState();
  }

  @Override
  @Deprecated
  public void peripheralJobFinished(@Nonnull PeripheralJob job) {
    requireNonNull(job, "job");

    peripheralJobFinished(job.getReference());
  }

  @Override
  @Deprecated
  public void peripheralJobFailed(@Nonnull PeripheralJob job) {
    requireNonNull(job, "job");

    peripheralJobFailed(job.getReference());
  }

  @Override
  public void peripheralJobFinished(@Nonnull TCSObjectReference<PeripheralJob> ref) {
    requireNonNull(ref, "ref");

    PeripheralJob job = peripheralJobService.fetchObject(PeripheralJob.class, ref);
    if (job.getState() != PeripheralJob.State.BEING_PROCESSED) {
      LOG.info("Peripheral job not in state BEING_PROCESSED, ignoring: {} ({})",
               job.getName(),
               job.getState());
      return;
    }

    finalizeJob(job, PeripheralJob.State.FINISHED);
    dispatch();
  }

  @Override
  public void peripheralJobFailed(@Nonnull TCSObjectReference<PeripheralJob> ref) {
    requireNonNull(ref, "ref");

    PeripheralJob job = peripheralJobService.fetchObject(PeripheralJob.class, ref);
    if (job.getState() != PeripheralJob.State.BEING_PROCESSED) {
      LOG.info("Peripheral job not in state BEING_PROCESSED, ignoring: {} ({})",
               job.getName(),
               job.getState());
      return;
    }

    finalizeJob(job, PeripheralJob.State.FAILED);
    dispatch();
  }

  private void finalizeJob(PeripheralJob job, PeripheralJob.State state) {
    if (job.getState() == PeripheralJob.State.BEING_PROCESSED) {
      peripheralService.updatePeripheralProcState(job.getPeripheralOperation().getLocation(),
                                                  PeripheralInformation.ProcState.IDLE);
      peripheralService.updatePeripheralJob(job.getPeripheralOperation().getLocation(), null);
    }

    peripheralJobService.updatePeripheralJobState(job.getReference(), state);
  }
}
