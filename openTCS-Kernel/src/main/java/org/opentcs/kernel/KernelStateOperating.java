/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.ActiveInOperatingMode;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.kernel.extensions.controlcenter.vehicles.AttachmentManager;
import org.opentcs.kernel.extensions.xmlhost.orders.ScriptFileManager;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.vehicles.LocalVehicleControllerPool;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.opentcs.util.Comparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the standard openTCS kernel in normal operation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
class KernelStateOperating
    extends KernelStateOnline {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelStateOperating.class);
  /**
   * The kernel application's configuration.
   */
  private final KernelApplicationConfiguration configuration;
  /**
   * The order facade to the object pool.
   */
  private final TransportOrderPool orderPool;
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
   * The recovery evaluator to be used.
   */
  @SuppressWarnings("deprecation")
  private final org.opentcs.components.kernel.RecoveryEvaluator recoveryEvaluator;
  /**
   * A pool of vehicle controllers.
   */
  private final LocalVehicleControllerPool vehicleControllerPool;
  /**
   * A script file manager.
   */
  private final ScriptFileManager scriptFileManager;
  /**
   * The kernel's executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * A task for periodically getting rid of old orders.
   */
  private final OrderCleanerTask orderCleanerTask;
  /**
   * This kernel state's local extensions.
   */
  private final Set<KernelExtension> extensions;
  /**
   * The kernel's attachment manager.
   */
  private final AttachmentManager attachmentManager;
  /**
   * The vehicle service.
   */
  private final VehicleService vehicleService;
  /**
   * A handle for the cleaner task.
   */
  private ScheduledFuture<?> cleanerTaskFuture;
  /**
   * This instance's <em>initialized</em> flag.
   */
  private boolean initialized;

  /**
   * Creates a new KernelStateOperating.
   *
   * @param objectPool The object pool to be used.
   * @param messageBuffer The message buffer to be used.
   * @param modelPersister The model persister to be used.
   * @param configuration This class's configuration.
   * @param recoveryEvaluator The recovery evaluator to be used.
   */
  @Inject
  @SuppressWarnings("deprecation")
  KernelStateOperating(@GlobalKernelSync Object globalSyncObject,
                       TCSObjectPool objectPool,
                       Model model,
                       TransportOrderPool orderPool,
                       NotificationBuffer messageBuffer,
                       ModelPersister modelPersister,
                       KernelApplicationConfiguration configuration,
                       org.opentcs.components.kernel.RecoveryEvaluator recoveryEvaluator,
                       Router router,
                       Scheduler scheduler,
                       Dispatcher dispatcher,
                       LocalVehicleControllerPool controllerPool,
                       ScriptFileManager scriptFileManager,
                       @KernelExecutor ScheduledExecutorService kernelExecutor,
                       OrderCleanerTask orderCleanerTask,
                       @ActiveInOperatingMode Set<KernelExtension> extensions,
                       AttachmentManager attachmentManager,
                       VehicleService vehicleService) {
    super(globalSyncObject,
          objectPool,
          model,
          messageBuffer,
          modelPersister,
          configuration.saveModelOnTerminateOperating());
    this.orderPool = requireNonNull(orderPool, "orderPool");
    this.configuration = requireNonNull(configuration, "configuration");
    this.recoveryEvaluator = requireNonNull(recoveryEvaluator, "recoveryEvaluator");
    this.router = requireNonNull(router, "router");
    this.scheduler = requireNonNull(scheduler, "scheduler");
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
    this.scriptFileManager = requireNonNull(scriptFileManager, "scriptFileManager");
    this.vehicleControllerPool = requireNonNull(controllerPool, "controllerPool");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.orderCleanerTask = requireNonNull(orderCleanerTask, "orderCleanerTask");
    this.extensions = requireNonNull(extensions, "extensions");
    this.attachmentManager = requireNonNull(attachmentManager, "attachmentManager");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
  }

  // Implementation of interface Kernel starts here.
  @Override
  @SuppressWarnings("deprecation")
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }
    LOG.debug("Initializing operating state...");

    // Reset vehicle states to ensure vehicles are not dispatchable initially.
    for (Vehicle curVehicle : getTCSObjects(Vehicle.class)) {
      setVehicleProcState(curVehicle.getReference(), Vehicle.ProcState.UNAVAILABLE);
      vehicleService.updateVehicleIntegrationLevel(curVehicle.getReference(),
                                                   Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      setVehicleState(curVehicle.getReference(), Vehicle.State.UNKNOWN);
      setVehicleTransportOrder(curVehicle.getReference(), null);
      setVehicleOrderSequence(curVehicle.getReference(), null);
    }

    LOG.debug("Initializing scheduler '{}'...", scheduler);
    scheduler.initialize();
    LOG.debug("Initializing router '{}'...", router);
    router.initialize();
    LOG.debug("Initializing dispatcher '{}'...", dispatcher);
    dispatcher.initialize();
    LOG.debug("Initializing recovery evaluator '{}'...", recoveryEvaluator);
    recoveryEvaluator.initialize();
    LOG.debug("Initializing vehicle controller pool '{}'...", vehicleControllerPool);
    vehicleControllerPool.initialize();
    LOG.debug("Initializing attachment manager '{}'...", attachmentManager);
    attachmentManager.initialize();
    LOG.debug("Initializing script file manager '{}'...", scriptFileManager);
    scriptFileManager.initialize();

    // Start a task for cleaning up old orders periodically.
    cleanerTaskFuture = kernelExecutor.scheduleAtFixedRate(orderCleanerTask,
                                                           orderCleanerTask.getSweepInterval(),
                                                           orderCleanerTask.getSweepInterval(),
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
  @SuppressWarnings("deprecation")
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
    recoveryEvaluator.terminate();
    LOG.debug("Terminating dispatcher '{}'...", dispatcher);
    dispatcher.terminate();
    LOG.debug("Terminating router '{}'...", router);
    router.terminate();
    LOG.debug("Terminating scheduler '{}'...", scheduler);
    scheduler.terminate();
    LOG.debug("Terminating vehicle controller pool '{}'...", vehicleControllerPool);
    vehicleControllerPool.terminate();
    LOG.debug("Terminating attachment manager '{}'...", attachmentManager);
    attachmentManager.terminate();
    LOG.debug("Terminating script file manager '{}'...", scriptFileManager);
    scriptFileManager.terminate();
    // Grant communication adapters etc. some time to settle things.
    Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

    // Ensure that vehicles do not reference orders any more.
    for (Vehicle curVehicle : getTCSObjects(Vehicle.class)) {
      setVehicleProcState(curVehicle.getReference(), Vehicle.ProcState.UNAVAILABLE);
      vehicleService.updateVehicleIntegrationLevel(curVehicle.getReference(),
                                                   Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      setVehicleState(curVehicle.getReference(), Vehicle.State.UNKNOWN);
      setVehicleTransportOrder(curVehicle.getReference(), null);
      setVehicleOrderSequence(curVehicle.getReference(), null);
    }

    // Remove all orders and order sequences from the pool.
    orderPool.clear();

    initialized = false;

    LOG.debug("Operating state terminated.");
  }

  @Override
  public Kernel.State getState() {
    return Kernel.State.OPERATING;
  }

  @Override
  @Deprecated
  public void removeTCSObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      TCSObject<?> object = getGlobalObjectPool().getObjectOrNull(ref);
      if (object == null) {
        throw new ObjectUnknownException(ref);
      }
      // In normal operation mode, we only allow removal of transport orders and
      // order sequences.
      if (object instanceof TransportOrder) {
        TransportOrder order = (TransportOrder) object;
        // Check if the transport order is currently being processed by a
        // vehicle. If so, withdraw the order first.
        TCSObjectReference<Vehicle> vRef = order.getProcessingVehicle();
        // If the order is currently being processed by a vehicle, tell the
        // dispatcher to withdraw and implicitly remove it afterwards (by
        // calling this method again).
        if (order.hasState(TransportOrder.State.BEING_PROCESSED) && vRef != null) {
          LOG.warn("Transport order {} being processed by {}, not removing it",
                   order.getName(),
                   vRef.getName());
        }
        else {
          // Check if the transport order is part of an order sequence. If so,
          // remove the reference on it from the sequence.
          // XXX We might want to make sure the sequence's finishedIndex is
          // correct after removing the order...
          if (order.getWrappingSequence() != null) {
            orderPool.removeOrderSequenceOrder(order.getWrappingSequence(),
                                               order.getReference());
          }
          orderPool.removeTransportOrder(order.getReference());
        }
      }
      else if (object instanceof OrderSequence) {
        OrderSequence seq = (OrderSequence) object;
        // Clear the back references in all orders of the sequence.
        for (TCSObjectReference<TransportOrder> orderRef : seq.getOrders()) {
          orderPool.setTransportOrderWrappingSequence(orderRef, null);
        }
        // Finally remove the sequence.
        orderPool.removeOrderSequence(seq.getReference());
      }
      else if (object instanceof Group) {
        getModel().removeGroup(((Group) object).getReference());
      }
      else {
        super.removeTCSObject(ref);
      }
    }
  }

  @Override
  @Deprecated
  public void setPathLocked(TCSObjectReference<Path> ref,
                            boolean locked)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setPathLocked(ref, locked);
      if (configuration.updateRoutingTopologyOnPathLockChange()) {
        updateRoutingTopology();
      }
    }
  }

  @Override
  @Deprecated
  public void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                    int energyLevel)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleEnergyLevel(ref, energyLevel);
    }
  }

  @Override
  @Deprecated
  public void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                          String rechargeOperation)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleRechargeOperation(ref, rechargeOperation);
    }
  }

  @Override
  @Deprecated
  public void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                            List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleLoadHandlingDevices(ref, devices);
    }
  }

  @Override
  @Deprecated
  public void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleMaxVelocity(ref, velocity);
    }
  }

  @Override
  @Deprecated
  public void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                           int velocity)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleMaxReverseVelocity(ref, velocity);
    }
  }

  @Override
  @Deprecated
  public void setVehicleState(TCSObjectReference<Vehicle> ref,
                              Vehicle.State newState)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleState(ref, newState);
    }
  }

  @Override
  @Deprecated
  public void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                  Vehicle.ProcState newState)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      LOG.debug("Updating procState of vehicle {} to {}...", ref.getName(), newState);
      getModel().setVehicleProcState(ref, newState);
    }
  }

  @Override
  @Deprecated
  public void setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                     VehicleCommAdapter.State newState)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleAdapterState(ref, newState);
    }
  }

  @Override
  @Deprecated
  public void setVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                              Set<String> processableCategories) {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleProcessableCategories(ref, processableCategories);
    }
  }

  @Override
  @Deprecated
  public void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      LOG.debug("Vehicle {} has reached point {}.", vehicleRef, pointRef);
      getModel().setVehiclePosition(vehicleRef, pointRef);
    }
  }

  @Override
  @Deprecated
  public void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleNextPosition(vehicleRef, pointRef);
    }
  }

  @Override
  @Deprecated
  public void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                        Triple newPosition)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehiclePrecisePosition(vehicleRef, newPosition);
    }
  }

  @Override
  @Deprecated
  public void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                         double angle)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleOrientationAngle(vehicleRef, angle);
    }
  }

  @Override
  @Deprecated
  public void setVehicleTransportOrder(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleTransportOrder(vehicleRef, orderRef);
    }
  }

  @Override
  @Deprecated
  public void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleOrderSequence(vehicleRef, seqRef);
    }
  }

  @Override
  @Deprecated
  public void setVehicleRouteProgressIndex(
      TCSObjectReference<Vehicle> vehicleRef,
      int index)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleRouteProgressIndex(vehicleRef, index);
    }
  }

  @Override
  @Deprecated
  public TransportOrder createTransportOrder(List<Destination> destinations) {
    synchronized (getGlobalSyncObject()) {
      return orderPool.createTransportOrder(destinations).clone();
    }
  }

  @Override
  @Deprecated
  public TransportOrder createTransportOrder(TransportOrderCreationTO to) {
    synchronized (getGlobalSyncObject()) {
      return orderPool.createTransportOrder(to).clone();
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderDeadline(ref, deadline);
    }
  }

  @Override
  @Deprecated
  public void activateTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      TransportOrder order = getGlobalObjectPool().getObject(TransportOrder.class, ref);
      // Check if the transport order hasn't been activated before.
      checkArgument(order.hasState(TransportOrder.State.RAW),
                    "Transport order %s not in state RAW",
                    order);
      dispatcher.dispatch(order);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                     TransportOrder.State newState)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderState(ref, newState);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderIntendedVehicle(orderRef, vehicleRef);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderProcessingVehicle(TCSObjectReference<TransportOrder> orderRef,
                                                 TCSObjectReference<Vehicle> vehicleRef,
                                                 List<DriveOrder> driveOrders)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderProcessingVehicle(orderRef, vehicleRef, driveOrders);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderProcessingVehicle(orderRef, vehicleRef);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderFutureDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                                 List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderDriveOrders(orderRef, newOrders);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                           List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderDriveOrders(orderRef, newOrders);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderInitialDriveOrder(ref);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderNextDriveOrder(ref);
    }
  }

  @Override
  @Deprecated
  public void addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.addTransportOrderDependency(orderRef, newDepRef);
    }
  }

  @Override
  @Deprecated
  public void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.addTransportOrderDependency(orderRef, rmDepRef);
    }
  }

  @Override
  @Deprecated
  public void addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.addTransportOrderRejection(orderRef, newRejection);
    }
  }

  @Override
  @Deprecated
  public void setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      orderPool.setTransportOrderDispensable(orderRef, dispensable);
    }
  }

  @Override
  @Deprecated
  public OrderSequence createOrderSequence() {
    synchronized (getGlobalSyncObject()) {
      return orderPool.createOrderSequence().clone();
    }
  }

  @Override
  @Deprecated
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to) {
    synchronized (getGlobalSyncObject()) {
      return orderPool.createOrderSequence(to).clone();
    }
  }

  @Override
  @Deprecated
  public void addOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    synchronized (getGlobalSyncObject()) {
      orderPool.addOrderSequenceOrder(seqRef, orderRef);
    }
  }

  @Override
  @Deprecated
  public void removeOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    synchronized (getGlobalSyncObject()) {
      orderPool.removeOrderSequenceOrder(seqRef, orderRef);
    }
  }

  @Override
  @Deprecated
  public void setOrderSequenceFinishedIndex(
      TCSObjectReference<OrderSequence> ref,
      int index) {
    synchronized (getGlobalSyncObject()) {
      orderPool.setOrderSequenceFinishedIndex(ref, index);
    }
  }

  @Override
  @Deprecated
  public void setOrderSequenceComplete(TCSObjectReference<OrderSequence> ref) {
    synchronized (getGlobalSyncObject()) {
      OrderSequence seq = getGlobalObjectPool().getObject(OrderSequence.class, ref);
      // Make sure we don't execute this if the sequence is already marked as
      // finished, as that would make it possible to trigger disposition of a
      // vehicle at any given moment.
      if (seq.isComplete()) {
        return;
      }
      orderPool.setOrderSequenceComplete(ref);
      // If there aren't any transport orders left to be processed as part of
      // the sequence, mark it as finished, too.
      if (seq.getNextUnfinishedOrder() == null) {
        orderPool.setOrderSequenceFinished(ref);
        // If the sequence was being processed by a vehicle, clear its back
        // reference to the sequence to make it available again and dispatch it.
        if (seq.getProcessingVehicle() != null) {
          Vehicle vehicle = getGlobalObjectPool().getObject(Vehicle.class,
                                                            seq.getProcessingVehicle());
          getModel().setVehicleOrderSequence(vehicle.getReference(), null);
        }
      }
    }
  }

  @Override
  @Deprecated
  public void setOrderSequenceFinished(TCSObjectReference<OrderSequence> ref) {
    synchronized (getGlobalSyncObject()) {
      OrderSequence seq = getGlobalObjectPool().getObject(OrderSequence.class, ref);
      // Make sure we don't execute this if the sequence is already marked as
      // finished, as that would make it possible to trigger disposition of a
      // vehicle at any given moment.
      if (seq.isFinished()) {
        return;
      }
      orderPool.setOrderSequenceFinished(ref);
      // If the sequence was being processed by a vehicle, clear its back
      // reference to the sequence to make it available again and dispatch it.
      if (seq.getProcessingVehicle() != null) {
        Vehicle vehicle = getGlobalObjectPool().getObject(Vehicle.class,
                                                          seq.getProcessingVehicle());
        getModel().setVehicleOrderSequence(vehicle.getReference(), null);
      }
    }
  }

  @Override
  @Deprecated
  public void setOrderSequenceFailureFatal(
      TCSObjectReference<OrderSequence> ref,
      boolean fatal) {
    synchronized (getGlobalSyncObject()) {
      orderPool.setOrderSequenceFailureFatal(ref, fatal);
    }
  }

  @Override
  @Deprecated
  public void setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    synchronized (getGlobalSyncObject()) {
      orderPool.setOrderSequenceIntendedVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  @Deprecated
  public void setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    synchronized (getGlobalSyncObject()) {
      orderPool.setOrderSequenceProcessingVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  @Deprecated
  public void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                                     boolean immediateAbort,
                                     boolean disableVehicle)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      dispatcher.withdrawOrder(getGlobalObjectPool().getObject(TransportOrder.class, ref),
                               immediateAbort,
                               disableVehicle);
    }
  }

  @Override
  @Deprecated
  public void withdrawTransportOrderByVehicle(TCSObjectReference<Vehicle> vehicleRef,
                                              boolean immediateAbort,
                                              boolean disableVehicle)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      dispatcher.withdrawOrder(getGlobalObjectPool().getObject(Vehicle.class, vehicleRef),
                               immediateAbort,
                               disableVehicle);
    }
  }

  @Override
  @Deprecated
  public void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable) {
    synchronized (getGlobalSyncObject()) {
      Vehicle vehicle = getGlobalObjectPool().getObject(Vehicle.class, vehicleRef);
      // If the vehicle's processing state is currently UNAVAILABLE and we're
      // supposed to change that to IDLE implicitly, do so.
      if (vehicle.hasProcState(Vehicle.ProcState.UNAVAILABLE)
          && setIdleIfUnavailable) {
        // Note: Setting the vehicle's processing state to IDLE implicitly
        // triggers the dispatcher, so that doesn't have to be done here again.
        setVehicleProcState(vehicleRef, Vehicle.ProcState.IDLE);
        vehicleService.updateVehicleIntegrationLevel(vehicleRef,
                                                     Vehicle.IntegrationLevel.TO_BE_UTILIZED);
      }
      else if (vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
        dispatcher.dispatch(vehicle);
      }
      else {
        LOG.warn("{}: Vehicle's processing state is not IDLE but {}",
                 vehicle.getName(),
                 vehicle.getProcState());
      }
    }
  }

  @Override
  @Deprecated
  public void releaseVehicle(TCSObjectReference<Vehicle> vehicleRef) {
    synchronized (getGlobalSyncObject()) {
      dispatcher.releaseVehicle(getGlobalObjectPool().getObject(Vehicle.class, vehicleRef));
    }
  }

  @Override
  @Deprecated
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef, Object message) {
    synchronized (getGlobalSyncObject()) {
      vehicleControllerPool
          .getVehicleController(vehicleRef.getName())
          .sendCommAdapterMessage(message);
    }
  }

  @Override
  @Deprecated
  public List<TransportOrder> createTransportOrdersFromScript(String fileName)
      throws ObjectUnknownException, IOException {
    synchronized (getGlobalSyncObject()) {
      List<TransportOrder> orders
          = scriptFileManager.createTransportOrdersFromScript(fileName);
      // Return a deep copy.
      List<TransportOrder> result = new LinkedList<>();
      for (TransportOrder curOrder : orders) {
        result.add(curOrder.clone());
      }
      return result;
    }
  }

  @Override
  @Deprecated
  public void updateRoutingTopology() {
    synchronized (getGlobalSyncObject()) {
      router.topologyChanged();
      // XXX Check if we need to re-route any vehicles?
    }
  }

  @Override
  @Deprecated
  public List<org.opentcs.access.TravelCosts> getTravelCosts(
      TCSObjectReference<Vehicle> vRef,
      TCSObjectReference<Location> srcRef,
      Set<TCSObjectReference<Location>> destRefs)
      throws ObjectUnknownException {
    // If vTypeRef is null get any vehicleType instead
    Vehicle vehicle;
    if (vRef == null) {
      Set<Vehicle> vehicles = getTCSObjects(Vehicle.class);
      if (vehicles.isEmpty()) {
        throw new ObjectUnknownException("no vehicles available");
      }
      vehicle = vehicles.iterator().next();
    }
    else {
      vehicle = getTCSObject(Vehicle.class, vRef);
      if (vehicle == null) {
        throw new ObjectUnknownException("Vehicle unknown");
      }
    }

    //List containing the costs for every destination
    List<org.opentcs.access.TravelCosts> travelCosts = new ArrayList<>();

    //Get cheapest costs for every destination
    for (TCSObjectReference<Location> currentLoc : destRefs) {
      long costs = router.getCosts(vehicle, srcRef, currentLoc);
      travelCosts.add(new org.opentcs.access.TravelCosts(currentLoc, costs));
    }

    Collections.sort(travelCosts, Comparators.travelCostsByCosts());
    return travelCosts;
  }

  @Override
  @Deprecated
  public <T extends org.opentcs.access.queries.Query<T>> T query(Class<T> clazz) {
    if (org.opentcs.access.queries.QueryAvailableScriptFiles.class.equals(clazz)) {
      return clazz.cast(new org.opentcs.access.queries.QueryAvailableScriptFiles(
          scriptFileManager.listScriptFileNames()));
    }
    else if (org.opentcs.access.queries.QueryRecoveryStatus.class.equals(clazz)) {
      return clazz.cast(recoveryEvaluator.evaluateRecovery());
    }
    else if (org.opentcs.access.queries.QueryRoutingInfo.class.equals(clazz)) {
      return clazz.cast(new org.opentcs.access.queries.QueryRoutingInfo(router.getInfo()));
    }
    else if (org.opentcs.access.queries.QuerySchedulerAllocations.class.equals(clazz)) {
      return clazz.cast(
          new org.opentcs.access.queries.QuerySchedulerAllocations(scheduler.getAllocations()));
    }
    else {
      return super.query(clazz);
    }
  }

  @Override
  @Deprecated
  public double getSimulationTimeFactor() {
    return vehicleControllerPool.getSimulationTimeFactor();
  }

  @Override
  @Deprecated
  public void setSimulationTimeFactor(double angle) {
    vehicleControllerPool.setSimulationTimeFactor(angle);
  }
}
