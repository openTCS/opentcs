/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.inject.BindingAnnotation;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
import org.opentcs.util.annotations.ScheduledApiChange;

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
   * @param saveModelOnTerminate Whether to save the model when this state is
   * terminated.
   * @param recoveryEvaluator The recovery evaluator to be used.
   */
  @Inject
  KernelStateOperating(StandardKernel kernel,
                       @GlobalKernelSync Object globalSyncObject,
                       TCSObjectPool objectPool,
                       Model model,
                       TransportOrderPool orderPool,
                       MessageBuffer messageBuffer,
                       @SaveModelOnTerminate boolean saveModelOnTerminate,
                       RecoveryEvaluator recoveryEvaluator,
                       Router router,
                       Scheduler scheduler,
                       Dispatcher dispatcher,
                       StandardVehicleManagerPool controllerPool,
                       CommunicationAdapterRegistry commAdapterRegistry,
                       ScriptFileManager scriptFileManager,
                       OrderCleanerTask orderCleanerTask,
                       @KernelExtension.Operating Set<KernelExtension> extensions) {
    super(kernel, globalSyncObject, objectPool, model, messageBuffer,
          saveModelOnTerminate);
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
    super.terminate();

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
  public void removeTCSObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
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
    synchronized (globalSyncObject) {
      model.setVehicleRechargeOperation(ref, rechargeOperation);
    }
  }

  @Override
  public void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                            List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleLoadHandlingDevices(ref, devices);
    }
  }

  @Override
  public void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleMaxVelocity(ref, velocity);
    }
  }

  @Override
  public void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                           int velocity)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleMaxReverseVelocity(ref, velocity);
    }
  }

  @Override
  public void setVehicleState(TCSObjectReference<Vehicle> ref,
                              Vehicle.State newState)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleState(ref, newState);
    }
  }

  @Override
  public void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                  Vehicle.ProcState newState)
      throws ObjectUnknownException {
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
    synchronized (globalSyncObject) {
      model.setVehicleAdapterState(ref, newState);
    }
  }

  @Override
  public void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
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
    synchronized (globalSyncObject) {
      model.setVehicleNextPosition(vehicleRef, pointRef);
    }
  }

  @Override
  public void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                        Triple newPosition)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehiclePrecisePosition(vehicleRef, newPosition);
    }
  }

  @Override
  public void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                         double angle)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleOrientationAngle(vehicleRef, angle);
    }
  }

  @Override
  public void setVehicleTransportOrder(
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleTransportOrder(vehicleRef, orderRef);
    }
  }

  @Override
  public void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleOrderSequence(vehicleRef, seqRef);
    }
  }

  @Override
  public void setVehicleRouteProgressIndex(
      TCSObjectReference<Vehicle> vehicleRef,
      int index)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVehicleRouteProgressIndex(vehicleRef, index);
    }
  }

  @Override
  public TransportOrder createTransportOrder(List<Destination> destinations) {
    synchronized (globalSyncObject) {
      return orderPool.createTransportOrder(destinations).clone();
    }
  }

  @Override
  public void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderDeadline(ref, deadline);
    }
  }

  @Override
  public void activateTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      TransportOrder order = orderPool.getTransportOrder(ref);
      // Check if the transport order hasn't been activated before.
      checkArgument(order.hasState(TransportOrder.State.RAW),
                    "Transport order %s not in state RAW",
                    order);
      dispatcher.dispatch(order);
    }
  }

  @Override
  public void setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                     TransportOrder.State newState)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderState(ref, newState);
    }
  }

  @Override
  public void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderIntendedVehicle(orderRef, vehicleRef);
    }
  }

  @Override
  public void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderProcessingVehicle(orderRef, vehicleRef);
    }
  }

  @Override
  public void setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderFutureDriveOrders(orderRef, newOrders);
    }
  }

  @Override
  public void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderInitialDriveOrder(ref);
    }
  }

  @Override
  public void setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderNextDriveOrder(ref);
    }
  }

  @Override
  public void addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.addTransportOrderDependency(orderRef, newDepRef);
    }
  }

  @Override
  public void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.addTransportOrderDependency(orderRef, rmDepRef);
    }
  }

  @Override
  public void addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.addTransportOrderRejection(orderRef, newRejection);
    }
  }

  @Override
  public void setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      orderPool.setTransportOrderDispensable(orderRef, dispensable);
    }
  }

  @Override
  public OrderSequence createOrderSequence() {
    synchronized (globalSyncObject) {
      return orderPool.createOrderSequence().clone();
    }
  }

  @Override
  public void addOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    synchronized (globalSyncObject) {
      orderPool.addOrderSequenceOrder(seqRef, orderRef);
    }
  }

  @Override
  public void removeOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    synchronized (globalSyncObject) {
      orderPool.removeOrderSequenceOrder(seqRef, orderRef);
    }
  }

  @Override
  public void setOrderSequenceFinishedIndex(
      TCSObjectReference<OrderSequence> ref,
      int index) {
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceFinishedIndex(ref, index);
    }
  }

  @Override
  public void setOrderSequenceComplete(TCSObjectReference<OrderSequence> ref) {
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
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceFailureFatal(ref, fatal);
    }
  }

  @Override
  public void setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceIntendedVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  public void setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    synchronized (globalSyncObject) {
      orderPool.setOrderSequenceProcessingVehicle(seqRef, vehicleRef);
    }
  }

  @Override
  public void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                                     boolean immediateAbort,
                                     boolean disableVehicle)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.withdrawOrder(orderPool.getTransportOrder(ref),
                               immediateAbort,
                               disableVehicle);
    }
  }

  @Override
  public void withdrawTransportOrderByVehicle(
      TCSObjectReference<Vehicle> vehicleRef,
      boolean immediateAbort,
      boolean disableVehicle)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      dispatcher.withdrawOrder(model.getVehicle(vehicleRef),
                               immediateAbort,
                               disableVehicle);
    }
  }

  @Override
  public void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable) {
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
  @Deprecated
  @ScheduledApiChange(when = "4.0.0")
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
   * Annotation type for marking/binding the "save on terminate" parameter.
   */
  @BindingAnnotation
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface SaveModelOnTerminate {
    // Nothing here.
  }
}
