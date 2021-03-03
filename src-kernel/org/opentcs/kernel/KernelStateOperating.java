/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.TravelCosts;
import org.opentcs.access.queries.Query;
import org.opentcs.access.queries.QueryAvailableScriptFiles;
import org.opentcs.access.queries.QueryRecoveryStatus;
import org.opentcs.access.queries.QueryRoutingInfo;
import org.opentcs.access.queries.QuerySchedulerAllocations;
import org.opentcs.algorithms.Dispatcher;
import org.opentcs.algorithms.KernelExtension;
import org.opentcs.algorithms.RecoveryEvaluator;
import org.opentcs.algorithms.Router;
import org.opentcs.algorithms.Scheduler;
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
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.CommunicationAdapterRegistry;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.drivers.VehicleController;
import org.opentcs.drivers.VehicleControllerPool;
import org.opentcs.drivers.VehicleManagerPool;
import org.opentcs.kernel.vehicles.StandardVehicleManagerPool;
import org.opentcs.kernel.workingset.MessageBuffer;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.opentcs.kernel.xmlorders.ScriptFileManager;

/**
 * This class implements the standard openTCS kernel in normal operation.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class KernelStateOperating
    extends KernelState {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(KernelStateOperating.class.getName());
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
  private final RecoveryEvaluator recoveryEvaluator;
  /**
   * A pool of vehicle controllers.
   */
  private final StandardVehicleManagerPool controllerPool;
  /**
   * A registry for communication adapters.
   */
  private final CommunicationAdapterRegistry commAdapterRegistry;
  /**
   * A script file manager.
   */
  private final ScriptFileManager scriptFileManager;
  /**
   * A task for periodically getting rid of old orders.
   */
  private final OrderCleanerTask orderCleanerTask;
  /**
   * This kernel state's local extensions.
   */
  private final Set<KernelExtension> extensions;
  /**
   * This instance's <em>initialized</em> flag.
   */
  private boolean initialized;

  /**
   * Creates a new KernelStateOperating.
   *
   * @param kernel The kernel.
   * @param objectPool The object pool to be used.
   * @param messageBuffer The message buffer to be used.
   * @param recoveryEvaluator The recovery evaluator to be used.
   */
  @Inject
  KernelStateOperating(StandardKernel kernel,
                       @GlobalKernelSync Object globalSyncObject,
                       TCSObjectPool objectPool,
                       Model model,
                       TransportOrderPool orderPool,
                       MessageBuffer messageBuffer,
                       RecoveryEvaluator recoveryEvaluator,
                       Router router,
                       Scheduler scheduler,
                       Dispatcher dispatcher,
                       StandardVehicleManagerPool controllerPool,
                       CommunicationAdapterRegistry commAdapterRegistry,
                       ScriptFileManager scriptFileManager,
                       OrderCleanerTask orderCleanerTask,
                       @KernelExtension.Operating Set<KernelExtension> extensions) {
    super(kernel, globalSyncObject, objectPool, model, messageBuffer);
    this.orderPool = requireNonNull(orderPool, "orderPool");
    this.recoveryEvaluator = requireNonNull(recoveryEvaluator,
                                            "recoveryEvaluator");
    this.router = requireNonNull(router, "router");
    this.scheduler = requireNonNull(scheduler, "scheduler");
    this.dispatcher = requireNonNull(dispatcher, "dispatcher");
    this.scriptFileManager = requireNonNull(scriptFileManager,
                                            "scriptFileManager");
    this.controllerPool = requireNonNull(controllerPool, "controllerPool");
    this.commAdapterRegistry = requireNonNull(commAdapterRegistry,
                                              "commAdapterRegistry");
    this.orderCleanerTask = requireNonNull(orderCleanerTask, "orderCleanerTask");
    this.extensions = requireNonNull(extensions, "extensions");
  }

  // Implementation of interface Kernel starts here.
  @Override
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    log.fine("Initializing operating state...");
    // Reset vehicle states to ensure vehicles are not dispatchable initially.
    for (Vehicle curVehicle : getTCSObjects(Vehicle.class)) {
      setVehicleProcState(curVehicle.getReference(),
                          Vehicle.ProcState.UNAVAILABLE);
      setVehicleState(curVehicle.getReference(), Vehicle.State.UNKNOWN);
      setVehicleTransportOrder(curVehicle.getReference(), null);
      setVehicleOrderSequence(curVehicle.getReference(), null);
    }
    // Start kernel extensions.
    for (KernelExtension extension : extensions) {
      extension.plugIn();
    }
    // (Re-)initialize the router.
    router.updateRoutingTables();
    // Initialize the dispatcher.
    dispatcher.initialize();

    // Start a task for cleaning up orders regularly.
    Thread cleanerThread = new Thread(orderCleanerTask, "orderCleaner");
    cleanerThread.start();

    initialized = true;

    log.fine("Operating state initialized.");
  }

  @Override
  public void terminate() {
    if (!initialized) {
      throw new IllegalStateException("Not initialized, cannot terminate");
    }
    log.fine("Terminating operating state...");

    // Terminate everything that may still use resources.
    for (KernelExtension extension : extensions) {
      extension.plugOut();
    }

    // No need to clean up any more - it's all going to be cleaned up very soon.
    orderCleanerTask.terminate();
    // Terminate the dispatcher so vehicles aren't assigned orders any more.
    dispatcher.terminate();
    // Terminate communication adapters, too.
    controllerPool.terminate();
    // Grant communication adapters etc. some time to settle things.
    try {
      Thread.sleep(500);
    }
    catch (InterruptedException exc) {
      log.log(Level.WARNING, "Unexpectedly interrupted while sleeping", exc);
    }

    // Ensure that vehicles do not reference orders any more.
    for (Vehicle curVehicle : getTCSObjects(Vehicle.class)) {
      setVehicleProcState(curVehicle.getReference(),
                          Vehicle.ProcState.UNAVAILABLE);
      setVehicleState(curVehicle.getReference(), Vehicle.State.UNKNOWN);
      setVehicleTransportOrder(curVehicle.getReference(), null);
      setVehicleOrderSequence(curVehicle.getReference(), null);
    }

    // Remove all orders and order sequences from the pool.
    orderPool.clear();

    initialized = false;

    log.fine("Operating state terminated.");
  }

  @Override
  public Kernel.State getState() {
    return Kernel.State.OPERATING;
  }

  @Override
  public void saveModel(String modelName, boolean overwrite)
      throws IOException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      if (modelName != null) {
        verifyModelNameCaseMatch(modelName);
        model.setName(modelName);
      }
      kernel.modelPersister.saveModel(model,
                                      model.getName(),
                                      overwrite);
    }
  }

  @Override
  public void removeTCSObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      TCSObject<?> object = globalObjectPool.getObject(ref);
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
        if (order.hasState(TransportOrder.State.BEING_PROCESSED)
            && vRef != null) {
          log.warning("Transport order " + order.getName()
              + " being processed by " + vRef.getName() + ", not removing it");
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
        model.removeGroup(((Group) object).getReference());
      }
      else {
        super.removeTCSObject(ref);
      }
    }
  }

  @Override
  public void setPathLocked(TCSObjectReference<Path> ref,
                            boolean locked)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setPathLocked(ref, locked);
      router.updateRoutingTables();
      // XXX Check if we need to re-route any vehicles?
    }
  }

  @Override
  public void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                    int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      Vehicle vehicle = model.setVehicleEnergyLevel(ref,
                                                    energyLevel);
      // If the vehicle is idle, dispatch it - maybe the dispatcher has an order
      // for it.
      if (vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
        dispatcher.dispatch(vehicle);
      }
    }
  }

  @Override
  public void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                          String rechargeOperation)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleRechargeOperation(ref, rechargeOperation);
    }
  }

  @Override
  public void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                            List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleLoadHandlingDevices(ref, devices);
    }
  }

  @Override
  public void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleMaxVelocity(ref, velocity);
    }
  }

  @Override
  public void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                           int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleMaxReverseVelocity(ref, velocity);
    }
  }

  @Override
  public void setVehicleState(TCSObjectReference<Vehicle> ref,
                              Vehicle.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleState(ref, newState);
    }
  }

  @Override
  public void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                  Vehicle.ProcState newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      Vehicle vehicle = model.setVehicleProcState(ref, newState);
      switch (newState) {
        case IDLE:
        case AWAITING_ORDER:
          // The vehicle is waiting for an order - let the dispatcher handle it.
          dispatcher.dispatch(vehicle);
          break;
        default:
      }
    }
  }

  @Override
  public void setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                     CommunicationAdapter.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleAdapterState(ref, newState);
    }
  }

  @Override
  public void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      final String pointName = pointRef == null ? "<null>" : pointRef.getName();
      log.fine("Vehicle " + vehicleRef.getName() + " has reached point "
          + pointName);
      Vehicle vehicle = model.getVehicle(vehicleRef);
      TCSObjectReference<Point> oldPointRef = vehicle.getCurrentPosition();
      model.setVehiclePosition(vehicleRef, pointRef);
//      if (oldPointRef == null && pointRef != null) {
//        dispatcher.dispatch(vehicle);
//      }
    }
  }

  @Override
  public void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleNextPosition(vehicleRef, pointRef);
    }
  }

  @Override
  public void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                        Triple newPosition)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehiclePrecisePosition(vehicleRef, newPosition);
    }
  }

  @Override
  public void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                         double angle)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleOrientationAngle(vehicleRef, angle);
    }
  }

  @Override
  public void setVehicleTransportOrder(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleTransportOrder(vehicleRef, orderRef);
    }
  }

  @Override
  public void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleOrderSequence(vehicleRef, seqRef);
    }
  }

  @Override
  public void setVehicleRouteProgressIndex(
      TCSObjectReference<Vehicle> vehicleRef,
      int index)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleRouteProgressIndex(vehicleRef, index);
    }
  }

  @Override
  public TransportOrder createTransportOrder(List<Destination> destinations) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return orderPool.createTransportOrder(destinations).clone();
    }
  }

  @Override
  public void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderDeadline(ref, deadline);
    }
  }

  @Override
  public void activateTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      TransportOrder order = orderPool.getTransportOrder(ref);
      // Check if the transport order hasn't been activated before.
      if (!order.hasState(TransportOrder.State.RAW)) {
        throw new IllegalArgumentException("Transport order not in state RAW");
      }
      // Check if the transport order is routable.
      Set<Vehicle> routableVehicles = router.checkRoutability(order);
      if (routableVehicles.isEmpty()) {
        orderPool.setTransportOrderState(order.getReference(),
                                         TransportOrder.State.UNROUTABLE);
        return;
      }
      // Set the transport order's state.
      order = orderPool.setTransportOrderState(order.getReference(),
                                               TransportOrder.State.ACTIVE);
      // The transport order has been activated - dispatch it.
      // Check if it has unfinished dependencies.
      if (!orderPool.hasUnfinishedDependencies(order.getReference())) {
        orderPool.setTransportOrderState(order.getReference(),
                                         TransportOrder.State.DISPATCHABLE);
        log.fine("Dispatching activated transport order " + order.getName());
        dispatcher.dispatch(order);
      }
    }
  }

  @Override
  public void setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                     TransportOrder.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      switch (newState) {
        case ACTIVE:
          throw new IllegalArgumentException(
              "Activating orders should happen via activateTransportOrder()");
        case FINISHED:
          setTOStateFinished(ref);
          break;
        case FAILED:
          setTOStateFailed(ref);
          break;
        default:
          // Set the transport order's state.
          orderPool.setTransportOrderState(ref, newState);
      }
    }
  }

  @Override
  public void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderIntendedVehicle(orderRef, vehicleRef);
    }
  }

  @Override
  public void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderProcessingVehicle(orderRef, vehicleRef);
    }
  }

  @Override
  public void setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderFutureDriveOrders(orderRef, newOrders);
    }
  }

  @Override
  public void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderInitialDriveOrder(ref);
    }
  }

  @Override
  public void setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderNextDriveOrder(ref);
    }
  }

  @Override
  public void addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.addTransportOrderDependency(orderRef, newDepRef);
    }
  }

  @Override
  public void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.addTransportOrderDependency(orderRef, rmDepRef);
    }
  }

  @Override
  public void addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.addTransportOrderRejection(orderRef, newRejection);
    }
  }

  @Override
  public void setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderDispensable(orderRef, dispensable);
    }
  }

  @Override
  public OrderSequence createOrderSequence() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return orderPool.createOrderSequence().clone();
    }
  }

  @Override
  public void addOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.addOrderSequenceOrder(seqRef, orderRef);
    }
  }

  @Override
  public void removeOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.removeOrderSequenceOrder(seqRef, orderRef);
    }
  }

  @Override
  public void setOrderSequenceFinishedIndex(
      TCSObjectReference<OrderSequence> ref,
      int index) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceFinishedIndex(ref, index);
    }
  }

  @Override
  public void setOrderSequenceComplete(TCSObjectReference<OrderSequence> ref) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      OrderSequence seq = orderPool.getOrderSequence(ref);
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
          Vehicle vehicle = model.getVehicle(seq.getProcessingVehicle());
          model.setVehicleOrderSequence(vehicle.getReference(), null);
          dispatcher.dispatch(vehicle);
        }
      }
    }
  }

  @Override
  public void setOrderSequenceFinished(TCSObjectReference<OrderSequence> ref) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      OrderSequence seq = orderPool.getOrderSequence(ref);
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
        Vehicle vehicle = model.getVehicle(seq.getProcessingVehicle());
        model.setVehicleOrderSequence(vehicle.getReference(), null);
        dispatcher.dispatch(vehicle);
      }
    }
  }

  @Override
  public void setOrderSequenceFailureFatal(
      TCSObjectReference<OrderSequence> ref,
      boolean fatal) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceFailureFatal(ref, fatal);
    }
  }

  @Override
  public void setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceIntendedVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  public void setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceProcessingVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  public void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                                     boolean disableVehicle)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      TransportOrder order = orderPool.getTransportOrder(ref);
      if (order.getState().isFinalState()) {
        return;
      }
      // Check if the order is currently being processed by a vehicle.
      TCSObjectReference<Vehicle> vehicleRef = order.getProcessingVehicle();
      if (vehicleRef == null) {
        setTransportOrderState(ref, TransportOrder.State.FAILED);
        return;
      }
      Vehicle vehicle = model.getVehicle(vehicleRef);
      dispatcher.withdrawOrder(vehicle, disableVehicle);
    }
  }

  @Override
  public void withdrawTransportOrderByVehicle(
      TCSObjectReference<Vehicle> vehicleRef, boolean disableVehicle)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      Vehicle vehicle
          = model.getVehicle(vehicleRef);
      dispatcher.withdrawOrder(vehicle, disableVehicle);
    }
  }

  @Override
  public void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      Vehicle vehicle = model.getVehicle(vehicleRef);
      // If the vehicle's processing state is currently UNAVAILABLE and we're
      // supposed to change that to IDLE implicitly, do so.
      if (vehicle.hasProcState(Vehicle.ProcState.UNAVAILABLE)
          && setIdleIfUnavailable) {
        // Note: Setting the vehicle's processing state to IDLE implicitly
        // triggers the dispatcher, so that doesn't have to be done here again.
        setVehicleProcState(vehicleRef, Vehicle.ProcState.IDLE);
      }
      else if (vehicle.hasProcState(Vehicle.ProcState.IDLE)) {
        dispatcher.dispatch(vehicle);
      }
      else {
        log.warning(vehicle.getName()
            + ": Vehicle's processing state is not IDLE but "
            + vehicle.getProcState().name());
      }
    }
  }

  @Override
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef,
                                     Object message) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      Vehicle vehicle = globalObjectPool.getObject(Vehicle.class,
                                                   vehicleRef);
      VehicleController controller
          = controllerPool.getVehicleController(vehicle.getName());
      if (controller != null) {
        controller.sendCommAdapterMessage(message);
      }
    }
  }

  @Override
  public List<TransportOrder> createTransportOrdersFromScript(String fileName)
      throws ObjectUnknownException, IOException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
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
  public List<TravelCosts> getTravelCosts(
      TCSObjectReference<Vehicle> vRef,
      TCSObjectReference<Location> srcRef,
      Set<TCSObjectReference<Location>> destRefs)
      throws ObjectUnknownException {

    log.finer("method entry");
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
    List<TravelCosts> travelCosts = new ArrayList<>();

    //Get cheapest costs for every destination
    for (TCSObjectReference<Location> currentLoc : destRefs) {
      long costs = router.getCosts(vehicle, srcRef, currentLoc);
      travelCosts.add(new TravelCosts(currentLoc, costs));
    }

    Collections.sort(travelCosts);
    return travelCosts;
  }

  @Override
  public <T extends Query<T>> T query(Class<T> clazz) {
    if (QueryAvailableScriptFiles.class.equals(clazz)) {
      return clazz.cast(new QueryAvailableScriptFiles(
          scriptFileManager.listScriptFileNames()));
    }
    else if (QueryRecoveryStatus.class.equals(clazz)) {
      return clazz.cast(recoveryEvaluator.evaluateRecovery());
    }
    else if (QueryRoutingInfo.class.equals(clazz)) {
      return clazz.cast(new QueryRoutingInfo(router.getInfo()));
    }
    else if (QuerySchedulerAllocations.class.equals(clazz)) {
      return clazz.cast(
          new QuerySchedulerAllocations(scheduler.getAllocations()));
    }
    else {
      return super.query(clazz);
    }
  }

  @Override
  public VehicleManagerPool getVehicleManagerPool() {
    return controllerPool;
  }

  @Override
  public VehicleControllerPool getVehicleControllerPool() {
    return controllerPool;
  }

  @Override
  public CommunicationAdapterRegistry getCommAdapterRegistry() {
    return commAdapterRegistry;
  }

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  @Override
  public double getSimulationTimeFactor() {
    return controllerPool.getSimulationTimeFactor();
  }

  @Override
  public void setSimulationTimeFactor(double angle) {
    controllerPool.setSimulationTimeFactor(angle);
  }

  /**
   * Properly sets a transport order to a finished state, setting related
   * properties.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced order could not be found.
   */
  private void setTOStateFinished(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    assert ref != null;
    // Set the transport order's state.
    TransportOrder order
        = orderPool.setTransportOrderState(ref, TransportOrder.State.FINISHED);
    // If it is part of an order sequence, we should proceed to its next order.
    if (order.getWrappingSequence() != null) {
      OrderSequence seq
          = orderPool.getOrderSequence(order.getWrappingSequence());
      // Sanity check: The finished order must be the next one in the sequence;
      // if it is not, something has already gone wrong.
      assert ref.equals(seq.getNextUnfinishedOrder());
      orderPool.setOrderSequenceFinishedIndex(seq.getReference(),
                                              seq.getFinishedIndex() + 1);
      // If the sequence is complete and this was its last order, the sequence
      // is also finished.
      if (seq.isComplete() && seq.getNextUnfinishedOrder() == null) {
        orderPool.setOrderSequenceFinished(seq.getReference());
        // Reset the processing vehicle's back reference on the sequence.
        model.setVehicleOrderSequence(seq.getProcessingVehicle(), null);
      }
    }
    // A transport order has been finished - look for others for which all
    // dependencies have been finished now and mark them as dispatchable.
    Set<TransportOrder> activeOrders
        = orderPool.getTransportOrders(TransportOrder.State.ACTIVE);
    for (TransportOrder curOrder : activeOrders) {
      TCSObjectReference<TransportOrder> curRef = curOrder.getReference();
      // If there aren't any unfinished dependencies for this transport
      // order any more, adjust its state.
      if (!orderPool.hasUnfinishedDependencies(curRef)) {
        orderPool.setTransportOrderState(curRef,
                                         TransportOrder.State.DISPATCHABLE);
        log.fine("Dispatching order " + curOrder.getName()
            + " which has just become dispatchable");
        dispatcher.dispatch(curOrder);
      }
    }
  }

  /**
   * Properly sets a transport order to a failed state, setting related
   * properties.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced order could not be found.
   */
  private void setTOStateFailed(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    assert ref != null;
    TransportOrder failedOrder = orderPool.getTransportOrder(ref);
    // A transport order has failed - check if it's part of an order
    // sequence that we need to take care of.
    if (failedOrder.getWrappingSequence() != null) {
      OrderSequence sequence
          = orderPool.getOrderSequence(failedOrder.getWrappingSequence());

      if (sequence.isFailureFatal()) {
        // Mark the sequence as complete to make sure no further orders are
        // added.
        orderPool.setOrderSequenceComplete(sequence.getReference());
        // Mark all orders of the sequence that are not in a final state as
        // FAILED.
        for (TCSObjectReference<TransportOrder> curRef : sequence.getOrders()) {
          TransportOrder curOrder = orderPool.getTransportOrder(curRef);
          if (!curOrder.getState().isFinalState()) {
            orderPool.setTransportOrderState(curRef,
                                             TransportOrder.State.FAILED);
          }
        }
        // Move the finished index of the sequence to its end.
        orderPool.setOrderSequenceFinishedIndex(sequence.getReference(),
                                                sequence.getOrders().size() - 1);
      }
      else {
        // Since failure of an order in the sequence is not fatal, increment the
        // finished index of the sequence by one and mark only that one order
        // as FAILED.
        orderPool.setOrderSequenceFinishedIndex(
            sequence.getReference(), sequence.getFinishedIndex() + 1);
        orderPool.setTransportOrderState(failedOrder.getReference(),
                                         TransportOrder.State.FAILED);
      }
      // Mark the sequence as finished if there's nothing more to do in it.
      if (sequence.isComplete() && sequence.getNextUnfinishedOrder() == null) {
        orderPool.setOrderSequenceFinished(sequence.getReference());
        // Reset the processing vehicle's back reference on the sequence.
        model.setVehicleOrderSequence(sequence.getProcessingVehicle(), null);
      }
    }
    else {
      // If it's not part of an order sequence, just set the order's state.
      orderPool.setTransportOrderState(failedOrder.getReference(),
                                       TransportOrder.State.FAILED);
    }
  }
}
