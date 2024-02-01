/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.kernel.workingset.WorkingSetCleanupTask;
import com.google.common.util.concurrent.Uninterruptibles;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.PeripheralJobDispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.customizations.kernel.ActiveInOperatingMode;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.peripherals.LocalPeripheralControllerPool;
import org.opentcs.kernel.peripherals.PeripheralAttachmentManager;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.workingset.PlantModelManager;
import org.opentcs.kernel.workingset.PeripheralJobPoolManager;
import org.opentcs.kernel.workingset.TransportOrderPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the standard openTCS kernel in normal operation.
 */
public class KernelStateOperating
    extends KernelStateOnline {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelStateOperating.class);
  /**
   * The order pool manager.
   */
  private final TransportOrderPoolManager orderPoolManager;
  /**
   * The job pool manager.
   */
  private final PeripheralJobPoolManager jobPoolManager;
  /**
   * This kernel's router.
   */
  private final Router router;
  /**
   * This kernel's scheduler.
   */
  private final Scheduler scheduler;
  /**
   * This kernel's dispatcher.
   */
  private final Dispatcher dispatcher;
  /**
   * This kernel's peripheral job dispatcher.
   */
  private final PeripheralJobDispatcher peripheralJobDispatcher;
  /**
   * A pool of vehicle controllers.
   */
  private final LocalVehicleControllerPool vehicleControllerPool;
  /**
   * A pool of peripheral controllers.
   */
  private final LocalPeripheralControllerPool peripheralControllerPool;
  /**
   * The kernel's executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * A task for periodically getting rid of old orders, order sequences and peripheral jobs.
   */
  private final WorkingSetCleanupTask workingSetCleanupTask;
  /**
   * This kernel state's local extensions.
   */
  private final Set<KernelExtension> extensions;
  /**
   * The kernel's attachment manager.
   */
  private final AttachmentManager attachmentManager;
  /**
   * The kernel's peripheral attachment manager.
   */
  private final PeripheralAttachmentManager peripheralAttachmentManager;
  /**
   * The vehicle service.
   */
  private final InternalVehicleService vehicleService;
  /**
   * Listens to path lock events and updates the routing topology.
   */
  private final PathLockEventListener pathLockListener;
  /**
   * Triggers dispatching of vehicles and transport orders on certain events.
   */
  private final VehicleDispatchTrigger vehicleDispatchTrigger;
  /**
   * A handle for the cleaner task.
   */
  private ScheduledFuture<?> cleanerTaskFuture;
  /**
   * This instance's <em>initialized</em> flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject kernel threads' global synchronization object.
   * @param plantModelManager The plant model manager to be used.
   * @param orderPoolManager The order pool manager to be used.
   * @param jobPoolManager The job pool manager to be used.
   * @param modelPersister The model persister to be used.
   * @param configuration This class's configuration.
   * @param router The router to be used.
   * @param scheduler The scheduler to be used.
   * @param dispatcher The dispatcher to be used.
   * @param peripheralJobDispatcher The peripheral job dispatcher to be used.
   * @param controllerPool The vehicle controller pool to be used.
   * @param peripheralControllerPool The peripheral controller pool to be used.
   * @param kernelExecutor The kernel executer to be used.
   * @param workingSetCleanupTask The workingset cleanup task to be used.
   * @param extensions The kernel extensions to load.
   * @param attachmentManager The attachment manager to be used.
   * @param peripheralAttachmentManager The peripheral attachment manager to be used.
   * @param vehicleService The vehicle service to be used.
   * @param pathLockListener Listens to path lock events and updates the routing topology.
   * @param vehicleDispatchTrigger Triggers dispatching of vehicles and transport orders on certain
   * events.
   */
  @Inject
  public KernelStateOperating(@GlobalSyncObject Object globalSyncObject,
                              PlantModelManager plantModelManager,
                              TransportOrderPoolManager orderPoolManager,
                              PeripheralJobPoolManager jobPoolManager,
                              ModelPersister modelPersister,
                              KernelApplicationConfiguration configuration,
                              Router router,
                              Scheduler scheduler,
                              Dispatcher dispatcher,
                              PeripheralJobDispatcher peripheralJobDispatcher,
                              LocalVehicleControllerPool controllerPool,
                              LocalPeripheralControllerPool peripheralControllerPool,
                              @KernelExecutor ScheduledExecutorService kernelExecutor,
                              WorkingSetCleanupTask workingSetCleanupTask,
                              @ActiveInOperatingMode Set<KernelExtension> extensions,
                              AttachmentManager attachmentManager,
                              PeripheralAttachmentManager peripheralAttachmentManager,
                              InternalVehicleService vehicleService,
                              PathLockEventListener pathLockListener,
                              VehicleDispatchTrigger vehicleDispatchTrigger) {
    super(globalSyncObject,
          plantModelManager,
          modelPersister,
          configuration.saveModelOnTerminateOperating());
    this.orderPoolManager = requireNonNull(orderPoolManager, "orderPoolManager");
    this.jobPoolManager = requireNonNull(jobPoolManager, "jobPoolManager");
    this.router = requireNonNull(router, "router");
    this.scheduler = requireNonNull(scheduler, "scheduler");
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
    this.peripheralJobDispatcher = requireNonNull(peripheralJobDispatcher,
                                                  "peripheralJobDispatcher");
    this.vehicleControllerPool = requireNonNull(controllerPool, "controllerPool");
    this.peripheralControllerPool = requireNonNull(peripheralControllerPool,
                                                   "peripheralControllerPool");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.workingSetCleanupTask = requireNonNull(workingSetCleanupTask, "workingSetCleanupTask");
    this.extensions = requireNonNull(extensions, "extensions");
    this.attachmentManager = requireNonNull(attachmentManager, "attachmentManager");
    this.peripheralAttachmentManager = requireNonNull(peripheralAttachmentManager,
                                                      "peripheralAttachmentManager");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.pathLockListener = requireNonNull(pathLockListener, "pathLockListener");
    this.vehicleDispatchTrigger = requireNonNull(vehicleDispatchTrigger, "vehicleDispatchTrigger");
  }

  // Implementation of interface Kernel starts here.
  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }
    LOG.debug("Initializing operating state...");

    // Reset vehicle states to ensure vehicles are not dispatchable initially.
    for (Vehicle curVehicle : vehicleService.fetchObjects(Vehicle.class)) {
      vehicleService.updateVehicleProcState(curVehicle.getReference(), Vehicle.ProcState.IDLE);
      vehicleService.updateVehicleIntegrationLevel(curVehicle.getReference(),
                                                   Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      vehicleService.updateVehicleState(curVehicle.getReference(), Vehicle.State.UNKNOWN);
      vehicleService.updateVehicleTransportOrder(curVehicle.getReference(), null);
      vehicleService.updateVehicleOrderSequence(curVehicle.getReference(), null);
    }

    LOG.debug("Initializing scheduler '{}'...", scheduler);
    scheduler.initialize();
    LOG.debug("Initializing router '{}'...", router);
    router.initialize();
    LOG.debug("Initializing dispatcher '{}'...", dispatcher);
    dispatcher.initialize();
    LOG.debug("Initializing peripheral job dispatcher '{}'...", peripheralJobDispatcher);
    peripheralJobDispatcher.initialize();
    LOG.debug("Initializing vehicle controller pool '{}'...", vehicleControllerPool);
    vehicleControllerPool.initialize();
    LOG.debug("Initializing peripheral controller pool '{}'...", peripheralControllerPool);
    peripheralControllerPool.initialize();
    LOG.debug("Initializing attachment manager '{}'...", attachmentManager);
    attachmentManager.initialize();
    LOG.debug("Initializing peripheral attachment manager '{}'...", peripheralAttachmentManager);
    peripheralAttachmentManager.initialize();

    pathLockListener.initialize();
    vehicleDispatchTrigger.initialize();

    // Start a task for cleaning up old orders periodically.
    cleanerTaskFuture = kernelExecutor.scheduleAtFixedRate(workingSetCleanupTask,
                                                           workingSetCleanupTask.getSweepInterval(),
                                                           workingSetCleanupTask.getSweepInterval(),
                                                           TimeUnit.MILLISECONDS);

    // Start kernel extensions.
    for (KernelExtension extension : extensions) {
      LOG.debug("Initializing kernel extension '{}'...", extension);
      extension.initialize();
    }
    LOG.debug("Finished initializing kernel extensions.");

    initialized = true;

    LOG.debug("Operating state initialized.");
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized.");
      return;
    }
    LOG.debug("Terminating operating state...");
    super.terminate();

    // Terminate everything that may still use resources.
    for (KernelExtension extension : extensions) {
      LOG.debug("Terminating kernel extension '{}'...", extension);
      extension.terminate();
    }
    LOG.debug("Terminated kernel extensions.");

    // No need to clean up any more - it's all going to be cleaned up very soon.
    cleanerTaskFuture.cancel(false);
    cleanerTaskFuture = null;

    // Terminate strategies.
    LOG.debug("Terminating peripheral job dispatcher '{}'...", peripheralJobDispatcher);
    peripheralJobDispatcher.terminate();
    LOG.debug("Terminating dispatcher '{}'...", dispatcher);
    dispatcher.terminate();
    LOG.debug("Terminating router '{}'...", router);
    router.terminate();
    LOG.debug("Terminating scheduler '{}'...", scheduler);
    scheduler.terminate();
    LOG.debug("Terminating peripheral controller pool '{}'...", peripheralControllerPool);
    peripheralControllerPool.terminate();
    LOG.debug("Terminating vehicle controller pool '{}'...", vehicleControllerPool);
    vehicleControllerPool.terminate();
    LOG.debug("Terminating attachment manager '{}'...", attachmentManager);
    attachmentManager.terminate();
    LOG.debug("Terminating peripheral attachment manager '{}'...", peripheralAttachmentManager);
    peripheralAttachmentManager.terminate();

    pathLockListener.terminate();
    vehicleDispatchTrigger.terminate();

    // Grant communication adapters etc. some time to settle things.
    Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    // Ensure that vehicles do not reference orders any more.
    for (Vehicle curVehicle : vehicleService.fetchObjects(Vehicle.class)) {
      vehicleService.updateVehicleProcState(curVehicle.getReference(), Vehicle.ProcState.IDLE);
      vehicleService.updateVehicleIntegrationLevel(curVehicle.getReference(),
                                                   Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      vehicleService.updateVehicleState(curVehicle.getReference(), Vehicle.State.UNKNOWN);
      vehicleService.updateVehicleTransportOrder(curVehicle.getReference(), null);
      vehicleService.updateVehicleOrderSequence(curVehicle.getReference(), null);
    }

    // Remove all orders and order sequences from the pool.
    orderPoolManager.clear();
    // Remove all peripheral jobs from the pool.
    jobPoolManager.clear();

    initialized = false;

    LOG.debug("Operating state terminated.");
  }

  @Override
  public Kernel.State getState() {
    return Kernel.State.OPERATING;
  }
}
