/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import com.google.inject.assistedinject.Assisted;
import java.time.Duration;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.drivers.peripherals.BasicPeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralJobCallback;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralProcessModelEvent;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.ExplainedBoolean;
import org.opentcs.util.event.EventHandler;

/**
 * A {@link PeripheralCommAdapter} implementation that is doing nothing.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackPeripheralCommAdapter
    extends BasicPeripheralCommAdapter {

  /**
   * The time it takes for loopback peripherals to process a job.
   */
  private static final Duration JOB_PROCESSING_DURATION = Duration.ofSeconds(5);
  /**
   * The kernel's executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * The queue of tasks to be executed to simulate the processing of jobs.
   * This queue may contain at most one item at any time.
   */
  private final Queue<Runnable> jobTaskQueue = new LinkedList<>();
  /**
   * A future for the current job task's execution.
   * Indicates whehter the current job task has been executed or whether it is still to be executed.
   */
  private Future<?> currentJobFuture;
  /**
   * Whether the execution of jobs should fail.
   */
  private boolean failJobs;

  /**
   * Creates a new instance.
   *
   * @param location The reference to the location this adapter is attached to.
   * @param eventHandler The handler used to send events to.
   * @param kernelExecutor The kernel's executor.
   */
  @Inject
  public LoopbackPeripheralCommAdapter(@Assisted TCSResourceReference<Location> location,
                                       @ApplicationEventBus EventHandler eventHandler,
                                       @KernelExecutor ScheduledExecutorService kernelExecutor) {
    super(new LoopbackPeripheralProcessModel(location), eventHandler);
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    super.initialize();

    setProcessModel(getProcessModel().withState(PeripheralInformation.State.IDLE));
    sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);
  }

  @Override
  public LoopbackPeripheralProcessModel getProcessModel() {
    return (LoopbackPeripheralProcessModel) super.getProcessModel();
  }

  @Override
  public ExplainedBoolean canProcess(PeripheralJob job) {
    if (!isEnabled()) {
      return new ExplainedBoolean(false, "Comm adapter not enabled.");
    }
    else if (hasJobWaitingToBeProcessed()) {
      return new ExplainedBoolean(false, "Busy processing another job.");
    }

    return new ExplainedBoolean(true, "");
  }

  @Override
  public void process(PeripheralJob job, PeripheralJobCallback callback) {
    ExplainedBoolean canProcess = canProcess(job);
    checkState(canProcess.getValue(),
               "%s: Can't process job: %s",
               getProcessModel().getLocation().getName(),
               canProcess.getReason());

    jobTaskQueue.add(() -> {
      if (failJobs) {
        callback.peripheralJobFailed(job);
      }
      else {
        callback.peripheralJobFinished(job);
      }
      setProcessModel(getProcessModel().withState(PeripheralInformation.State.IDLE));
      sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);
    });

    setProcessModel(getProcessModel().withState(PeripheralInformation.State.EXECUTING));
    sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);

    if (!getProcessModel().isManualModeEnabled()) {
      currentJobFuture = kernelExecutor.schedule(jobTaskQueue.poll(),
                                                 JOB_PROCESSING_DURATION.getSeconds(),
                                                 TimeUnit.SECONDS);
    }
  }

  @Override
  public void execute(PeripheralAdapterCommand command) {
    command.execute(this);
  }

  @Override
  protected void connectPeripheral() {
  }

  @Override
  protected void disconnectPeripheral() {
  }

  public void enableManualMode(boolean enabled) {
    kernelExecutor.submit(() -> {
      LoopbackPeripheralProcessModel oldProcessModel = getProcessModel();
      setProcessModel(getProcessModel().withManualModeEnabled(enabled));

      if (oldProcessModel.isManualModeEnabled() && !getProcessModel().isManualModeEnabled()) {
        // Job processing mode changed to "automatic". If there's a task that has not yet been 
        // processed while the processing mode was set to "manual", make sure the task is executed 
        // and schedule it for execution on the kernel executor.
        if (!jobTaskQueue.isEmpty()) {
          currentJobFuture = kernelExecutor.submit(jobTaskQueue.poll());
        }
      }

      sendProcessModelChangedEvent(LoopbackPeripheralProcessModel.Attribute.MANUAL_MODE_ENABLED);
    });
  }

  public void updateState(PeripheralInformation.State state) {
    kernelExecutor.submit(() -> {
      setProcessModel(getProcessModel().withState(state));
      sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);
    });
  }

  public void triggerJobProcessing(boolean failJob) {
    if (!getProcessModel().isManualModeEnabled()) {
      return;
    }

    if (jobTaskQueue.isEmpty() || !isCurrentJobFutureDone()) {
      // There's no job to be processed or we are not yet done processing the current one.
      return;
    }

    currentJobFuture = kernelExecutor.submit(() -> {
      failJobs = failJob;
      jobTaskQueue.poll().run();
      failJobs = false;
    });
  }

  private boolean hasJobWaitingToBeProcessed() {
    return !jobTaskQueue.isEmpty() || !isCurrentJobFutureDone();
  }

  private boolean isCurrentJobFutureDone() {
    if (currentJobFuture == null) {
      return true;
    }

    return currentJobFuture.isDone();
  }

  private void sendProcessModelChangedEvent(
      LoopbackPeripheralProcessModel.Attribute attributeChanged) {
    getEventHandler().onEvent(new PeripheralProcessModelEvent(getProcessModel().getLocation(),
                                                              attributeChanged.name(),
                                                              getProcessModel()));
  }
}
