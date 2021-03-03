/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.awt.Color;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.opentcs.access.ConfigurationItemTO;
import org.opentcs.access.Kernel.State;
import org.opentcs.access.TravelCosts;
import org.opentcs.access.UnsupportedKernelOpException;
import org.opentcs.access.queries.Queries;
import org.opentcs.access.queries.Query;
import org.opentcs.access.queries.QueryTopologyInfo;
import org.opentcs.algorithms.Scheduler;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.message.Message;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Layout;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.CommunicationAdapterRegistry;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.drivers.VehicleControllerPool;
import org.opentcs.drivers.VehicleManagerPool;
import org.opentcs.kernel.workingset.MessageBuffer;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.opentcs.util.configuration.Configuration;
import org.opentcs.util.configuration.ConfigurationItem;

/**
 * The abstract base class for classes that implement state specific kernel
 * behaviour.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class KernelState {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(KernelState.class.getName());
  /**
   * The kernel we're working for.
   */
  protected final StandardKernel kernel;
  /**
   * A global object to be used within the kernel.
   */
  protected final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  protected final TCSObjectPool globalObjectPool;
  /**
   * The model facade to the object pool.
   */
  protected final Model model;
  /**
   * The buffer for all messages published.
   */
  protected final MessageBuffer messageBuffer;

  /**
   * Creates a new state.
   *
   * @param kernel The kernel.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param objectPool The object pool to be used.
   * @param model The model to be used.
   * @param messageBuffer The message buffer to be used.
   */
  KernelState(StandardKernel kernel,
              @GlobalKernelSync Object globalSyncObject,
              TCSObjectPool objectPool,
              Model model,
              MessageBuffer messageBuffer) {
    log.finer("method entry");
    this.kernel = Objects.requireNonNull(kernel, "kernel is null");
    this.globalSyncObject = Objects.requireNonNull(globalSyncObject,
                                                   "globalSyncObject is null");
    this.globalObjectPool = Objects.requireNonNull(objectPool,
                                                   "objectPool is null");
    this.model = Objects.requireNonNull(model, "model is null");
    this.messageBuffer = Objects.requireNonNull(messageBuffer,
                                                "messageBuffer is null");
  }

  /**
   * Initializes this kernel state.
   * (Allocates resources, starts kernel extensions etc.)
   */
  public abstract void initialize();

  /**
   * Terminates this kernel state.
   * (Frees resources, stops kernel extensions etc.)
   */
  public abstract void terminate();

  public abstract State getState();

  public final Set<String> getModelNames() {
    log.finer("method entry");
    return kernel.modelPersister.getModelNames();
  }

  public final String getCurrentModelName() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return model.getName();
    }
  }

  public void createModel(String modelName) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void loadModel(String modelName)
      throws IOException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void saveModel(String modelName,
                        boolean overwrite)
      throws IOException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void removeModel(String rmName)
      throws IOException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public final <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                       TCSObjectReference<T> ref) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      T result = globalObjectPool.getObject(clazz, ref);
      return result == null ? null : clazz.cast(result.clone());
    }
  }

  public final <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                       String name) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      T result = globalObjectPool.getObject(clazz, name);
      return result == null ? null : clazz.cast(result.clone());
    }
  }

  public final <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      Set<T> objects = globalObjectPool.getObjects(clazz);
      Set<T> copies = new HashSet<>();
      for (T object : objects) {
        copies.add(clazz.cast(object.clone()));
      }
      return copies;
    }
  }

  public final <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz,
                                                             Pattern regexp) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      Set<T> objects = globalObjectPool.getObjects(clazz, regexp);
      Set<T> copies = new HashSet<>();
      for (T object : objects) {
        copies.add(clazz.cast(object.clone()));
      }
      return copies;
    }
  }

  public final void renameTCSObject(TCSObjectReference<?> ref,
                                    String newName)
      throws ObjectUnknownException, ObjectExistsException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      globalObjectPool.renameObject(ref, newName);
    }
  }

  public final void setTCSObjectProperty(TCSObjectReference<?> ref,
                                         String key,
                                         String value)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      globalObjectPool.setObjectProperty(ref, key, value);
    }
  }

  public final void clearTCSObjectProperties(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      globalObjectPool.clearObjectProperties(ref);
    }
  }

  public void removeTCSObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public Message publishMessage(String message, Message.Type type) {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      return messageBuffer.createMessage(message, type);
    }
  }

  public Layout createLayout(byte[] layoutData) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setLayoutData(TCSObjectReference<Layout> ref,
                            byte[] newData)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public VisualLayout createVisualLayout() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                                    double scaleX)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                                    double scaleY)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                                    Map<String, Color> colors)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                                      Set<LayoutElement> elements)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public final void setVisualLayoutViewBookmarks(TCSObjectReference<VisualLayout> ref,
                                                 List<ViewBookmark> bookmarks)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVisualLayoutViewBookmarks(ref, bookmarks);
    }
  }

  public Point createPoint() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPointPosition(TCSObjectReference<Point> ref,
                               Triple position)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                              double angle)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPathLength(TCSObjectReference<Path> ref,
                            long length)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPathRoutingCost(TCSObjectReference<Path> ref,
                                 long cost)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPathMaxVelocity(TCSObjectReference<Path> ref,
                                 int velocity)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int velocity)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setPathLocked(TCSObjectReference<Path> ref,
                            boolean locked)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public Vehicle createVehicle() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                    int energyLevel)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public final void setVehicleEnergyLevelCritical(TCSObjectReference<Vehicle> ref,
                                                  int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleEnergyLevelCritical(ref, energyLevel);
    }
  }

  public final void setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                              int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.setVehicleEnergyLevelGood(ref, energyLevel);
    }
  }

  public void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                          String rechargeAction)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                            List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                           int velocity)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleState(TCSObjectReference<Vehicle> ref,
                              Vehicle.State newState)
      throws ObjectUnknownException {
    // Do nada.
    // This method does not throw an exception because, when switching kernel
    // states, vehicle drivers are shut down and reset their vehicles' states
    // via this method; when done too late, calling this method leads to an
    // undesired exception.
    // XXX Maybe there's a cleaner way to handle this...
  }

  public void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                  Vehicle.ProcState newState)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                     CommunicationAdapter.State newState)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleLength(TCSObjectReference<Vehicle> ref,
                               int length)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                        Triple newPosition)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                         double angle)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                       TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setVehicleRouteProgressIndex(TCSObjectReference<Vehicle> vehicleRef,
                                           int index)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public LocationType createLocationType() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void addLocationTypeAllowedOperation(TCSObjectReference<LocationType> ref,
                                              String operation)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void removeLocationTypeAllowedOperation(TCSObjectReference<LocationType> ref,
                                                 String operation)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setLocationPosition(TCSObjectReference<Location> ref,
                                  Triple position)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setLocationType(TCSObjectReference<Location> ref,
                              TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void connectLocationToPoint(TCSObjectReference<Location> locRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void addLocationLinkAllowedOperation(TCSObjectReference<Location> locRef,
                                              TCSObjectReference<Point> pointRef,
                                              String operation)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void removeLocationLinkAllowedOperation(TCSObjectReference<Location> locRef,
                                                 TCSObjectReference<Point> pointRef,
                                                 String operation)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void clearLocationLinkAllowedOperations(TCSObjectReference<Location> locRef,
                                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public Block createBlock() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void addBlockMember(TCSObjectReference<Block> ref,
                             TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void removeBlockMember(TCSObjectReference<Block> ref,
                                TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public Group createGroup() {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      // Return a copy of the point
      return model.createGroup(null).clone();
    }
  }

  public void addGroupMember(TCSObjectReference<Group> ref,
                             TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.addGroupMember(ref, newMemberRef);
    }
  }

  public void removeGroupMember(TCSObjectReference<Group> ref,
                                TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    synchronized (globalSyncObject) {
      model.removeGroupMember(ref, rmMemberRef);
    }
  }

  public StaticRoute createStaticRoute() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void addStaticRouteHop(TCSObjectReference<StaticRoute> ref,
                                TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void clearStaticRouteHops(TCSObjectReference<StaticRoute> ref)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void attachResource(TCSResourceReference<?> resource,
                             TCSResourceReference<?> newResource)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void detachResource(TCSResourceReference<?> resource,
                             TCSResourceReference<?> rmResource)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public TransportOrder createTransportOrder(List<Destination> destinations) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void activateTransportOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderState(
      TCSObjectReference<TransportOrder> ref,
      TransportOrder.State newState)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderWrappingSequence(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public OrderSequence createOrderSequence() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void addOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void removeOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setOrderSequenceFinishedIndex(
      TCSObjectReference<OrderSequence> ref,
      int index) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setOrderSequenceComplete(TCSObjectReference<OrderSequence> ref) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setOrderSequenceFinished(
      TCSObjectReference<OrderSequence> ref) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setOrderSequenceFailureFatal(
      TCSObjectReference<OrderSequence> ref,
      boolean fatal) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                                     boolean disableVehicle) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void withdrawTransportOrderByVehicle(
      TCSObjectReference<Vehicle> vehicleRef, boolean disableVehicle)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef,
                                     Object message) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public List<TransportOrder> createTransportOrdersFromScript(
      String fileName)
      throws ObjectUnknownException, IOException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public final Set<TCSResource> expandResources(
      Set<TCSResourceReference> resources)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      return model.expandResources(resources);
    }
  }

  public List<TravelCosts> getTravelCosts(
      TCSObjectReference<Vehicle> vTypeRef,
      TCSObjectReference<Location> srcRef,
      Set<TCSObjectReference<Location>> destRefs)
      throws ObjectUnknownException {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public <T extends Query<T>> T query(Class<T> clazz) {
    // If the given query isn't available in this state, return null.
    if (!Queries.availableInState(clazz, getState())) {
      return null;
    }
    if (QueryTopologyInfo.class.equals(clazz)) {
      return clazz.cast(new QueryTopologyInfo(model.getInfo()));
    }
    else {
      // The given query should be available in this state, but isn't - throw an
      // exception.
      throw new IllegalStateException("Query " + clazz.getName()
          + " should be available in kernel state " + getState().name()
          + ", but noone processed it.");
    }
  }

  public VehicleManagerPool getVehicleManagerPool() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public VehicleControllerPool getVehicleControllerPool() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public CommunicationAdapterRegistry getCommAdapterRegistry() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public Scheduler getScheduler() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public double getSimulationTimeFactor() {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public void setSimulationTimeFactor(double angle) {
    throw new UnsupportedKernelOpException(unsupportedMsg());
  }

  public final Set<ConfigurationItemTO> getConfigurationItems() {
    log.finer("method entry");
    Set<ConfigurationItemTO> result = new HashSet<>();
    Set<ConfigurationItem> allItems
        = Configuration.getInstance().getConfigurationItems();
    for (ConfigurationItem item : allItems) {
      ConfigurationItemTO itemTO = new ConfigurationItemTO();
      itemTO.setNamespace(item.getNamespace());
      itemTO.setKey(item.getKey());
      itemTO.setValue(item.getValue());
      itemTO.setConstraint(item.getConstraint());
      itemTO.setDescription(item.getDescription());
      result.add(itemTO);
    }
    return result;
  }

  public final void setConfigurationItem(ConfigurationItemTO itemTO) {
    log.finer("method entry");
    if (itemTO == null) {
      throw new NullPointerException("itemTO is null");
    }
    ConfigurationItem item = new ConfigurationItem(itemTO.getNamespace(),
                                                   itemTO.getKey(),
                                                   itemTO.getDescription(),
                                                   itemTO.getConstraint(),
                                                   itemTO.getValue());
    Configuration.getInstance().setConfigurationItem(item);
  }

  /**
   * Verifies that the given model name either does not exist, yet, or does not
   * differ from an existing model name in spelling case only.
   *
   * @param modelName The model name to check.
   * @throws IOException If a model exists with a name that differs from the
   * given one only in spelling case.
   */
  protected void verifyModelNameCaseMatch(String modelName) throws IOException {
    requireNonNull(modelName, "modelName");
    for (String existingName : getModelNames()) {
      if (existingName.equalsIgnoreCase(modelName)
          && !existingName.equals(modelName)) {
        throw new IOException("Model name " + modelName
            + " differs in case from " + existingName);
      }
    }
  }

  private String unsupportedMsg() {
    return "Called operation not supported in this kernel mode ("
        + getState().name() + ").";
  }
}
