/**
 * Copyright (c) The openTCS Authors.
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
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opentcs.access.Kernel.State;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.Rejection;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.TCSObjectPool;

/**
 * The abstract base class for classes that implement state specific kernel
 * behaviour.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class KernelState
    implements Lifecycle {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectPool globalObjectPool;
  /**
   * The model facade to the object pool.
   */
  private final Model model;
  /**
   * The buffer for all messages published.
   */
  private final NotificationBuffer notificationBuffer;
  /**
   * The persister loading and storing model data.
   */
  private final ModelPersister modelPersister;

  /**
   * Creates a new state.
   *
   * @param kernel The kernel.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param objectPool The object pool to be used.
   * @param model The model to be used.
   * @param notificationBuffer The notification buffer to be used.
   */
  KernelState(Object globalSyncObject,
              TCSObjectPool objectPool,
              Model model,
              NotificationBuffer notificationBuffer,
              ModelPersister modelPersister) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(objectPool, "objectPool");
    this.model = requireNonNull(model, "model");
    this.notificationBuffer = requireNonNull(notificationBuffer, "notificationBuffer");
    this.modelPersister = requireNonNull(modelPersister, "modelPersister");
  }

  /**
   * Returns the current state.
   *
   * @return The current state.
   */
  public abstract State getState();

  /**
   * Returns the name of the currently saved model.
   *
   * @return The name of the model which is not present when there is no model.
   * @throws IllegalStateException If reading the model name from the model file failed.
   */
  @Deprecated
  public final Optional<String> getPersistentModelName()
      throws IllegalStateException {
    return modelPersister.getPersistentModelName();
  }

  @Deprecated
  public final String getLoadedModelName() {
    synchronized (getGlobalSyncObject()) {
      return getModel().getName();
    }
  }

  @Deprecated
  public void createPlantModel(PlantModelCreationTO to) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void createModel(String modelName) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void loadPlantModel()
      throws IllegalStateException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void loadModel()
      throws IOException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void savePlantModel()
      throws IllegalStateException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void saveModel(String modelName)
      throws IOException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void removeModel()
      throws IOException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public final <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                       TCSObjectReference<T> ref) {
    synchronized (getGlobalSyncObject()) {
      T result = getGlobalObjectPool().getObjectOrNull(clazz, ref);
      return result == null ? null : clazz.cast(result.clone());
    }
  }

  @Deprecated
  public final <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                       String name) {
    synchronized (getGlobalSyncObject()) {
      T result = getGlobalObjectPool().getObjectOrNull(clazz, name);
      return result == null ? null : clazz.cast(result.clone());
    }
  }

  @Deprecated
  public final <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz) {
    synchronized (getGlobalSyncObject()) {
      Set<T> objects = getGlobalObjectPool().getObjects(clazz);
      Set<T> copies = new HashSet<>();
      for (T object : objects) {
        copies.add(clazz.cast(object.clone()));
      }
      return copies;
    }
  }

  @Deprecated
  public final <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz,
                                                             Pattern regexp) {
    synchronized (getGlobalSyncObject()) {
      Set<T> objects = getGlobalObjectPool().getObjects(clazz, regexp);
      Set<T> copies = new HashSet<>();
      for (T object : objects) {
        copies.add(clazz.cast(object.clone()));
      }
      return copies;
    }
  }

  @SuppressWarnings("deprecation")
  public <T extends TCSObject<T>> Set<T> getTCSObjects(@Nonnull Class<T> clazz,
                                                       @Nonnull Predicate<? super T> predicate) {
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjects(clazz, predicate).stream()
          .map(obj -> clazz.cast(obj.clone()))
          .collect(Collectors.toSet());
    }
  }

  @Deprecated
  public final <T extends TCSObject<T>> T getTCSObjectOriginal(
      Class<T> clazz,
      TCSObjectReference<T> ref) {
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjectOrNull(clazz, ref);
    }
  }

  @Deprecated
  public final <T extends TCSObject<T>> T getTCSObjectOriginal(Class<T> clazz,
                                                               String name) {
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjectOrNull(clazz, name);
    }
  }

  @Deprecated
  public final <T extends TCSObject<T>> Set<T> getTCSObjectsOriginal(
      Class<T> clazz) {
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjects(clazz);
    }
  }

  @Deprecated
  public final <T extends TCSObject<T>> Set<T> getTCSObjectsOriginal(
      Class<T> clazz,
      Pattern regexp) {
    synchronized (getGlobalSyncObject()) {
      return getGlobalObjectPool().getObjects(clazz, regexp);
    }
  }

  @Deprecated
  public final void renameTCSObject(TCSObjectReference<?> ref,
                                    String newName)
      throws ObjectUnknownException, ObjectExistsException {
    synchronized (getGlobalSyncObject()) {
      getGlobalObjectPool().renameObject(ref, newName);
    }
  }

  @Deprecated
  public final void setTCSObjectProperty(TCSObjectReference<?> ref,
                                         String key,
                                         String value)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getGlobalObjectPool().setObjectProperty(ref, key, value);
    }
  }

  @Deprecated
  public final void clearTCSObjectProperties(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getGlobalObjectPool().clearObjectProperties(ref);
    }
  }

  @Deprecated
  public void removeTCSObject(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void publishUserNotification(UserNotification notification) {
    synchronized (getGlobalSyncObject()) {
      notificationBuffer.addNotification(notification);
    }
  }

  @Deprecated
  public List<UserNotification> getUserNotifications(Predicate<UserNotification> predicate) {
    synchronized (getGlobalSyncObject()) {
      return notificationBuffer.getNotifications(predicate);
    }
  }

  @Deprecated
  public VisualLayout createVisualLayout() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                                    double scaleX)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                                    double scaleY)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                                    Map<String, Color> colors)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                                      Set<LayoutElement> elements)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVisualLayoutViewBookmarks(
      TCSObjectReference<VisualLayout> ref,
      List<org.opentcs.data.model.visualization.ViewBookmark> bookmarks)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public Point createPoint()
      throws ObjectExistsException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPointPosition(TCSObjectReference<Point> ref,
                               Triple position)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                              double angle)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPathLength(TCSObjectReference<Path> ref,
                            long length)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPathRoutingCost(TCSObjectReference<Path> ref,
                                 long cost)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPathMaxVelocity(TCSObjectReference<Path> ref,
                                 int velocity)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int velocity)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setPathLocked(TCSObjectReference<Path> ref,
                            boolean locked)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public Vehicle createVehicle() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                    int energyLevel)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public final void setVehicleEnergyLevelCritical(
      TCSObjectReference<Vehicle> ref,
      int energyLevel)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleEnergyLevelCritical(ref, energyLevel);
    }
  }

  @Deprecated
  public final void setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                              int energyLevel)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVehicleEnergyLevelGood(ref, energyLevel);
    }
  }

  @Deprecated
  public void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                          String rechargeAction)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                            List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                           int velocity)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
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

  @Deprecated
  public void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                  Vehicle.ProcState newState)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                     VehicleCommAdapter.State newState)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleLength(TCSObjectReference<Vehicle> ref,
                               int length)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                              Set<String> pocessableCategories) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                        Triple newPosition)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                         double angle)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                       TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setVehicleRouteProgressIndex(
      TCSObjectReference<Vehicle> vehicleRef,
      int index)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public LocationType createLocationType() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void addLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref,
      String operation)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void removeLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref,
      String operation)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setLocationPosition(TCSObjectReference<Location> ref,
                                  Triple position)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setLocationType(TCSObjectReference<Location> ref,
                              TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void connectLocationToPoint(TCSObjectReference<Location> locRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void addLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef,
      TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void removeLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef,
      TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void clearLocationLinkAllowedOperations(
      TCSObjectReference<Location> locRef,
      TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public Block createBlock() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void addBlockMember(TCSObjectReference<Block> ref,
                             TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void removeBlockMember(TCSObjectReference<Block> ref,
                                TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public Group createGroup() {
    synchronized (getGlobalSyncObject()) {
      // Return a copy of the point
      return getModel().createGroup((Integer) null).clone();
    }
  }

  @Deprecated
  public void addGroupMember(TCSObjectReference<Group> ref,
                             TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().addGroupMember(ref, newMemberRef);
    }
  }

  @Deprecated
  public void removeGroupMember(TCSObjectReference<Group> ref,
                                TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().removeGroupMember(ref, rmMemberRef);
    }
  }

  @Deprecated
  public org.opentcs.data.model.StaticRoute createStaticRoute() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void addStaticRouteHop(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref,
                                TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void clearStaticRouteHops(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public TransportOrder createTransportOrder(List<Destination> destinations) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public TransportOrder createTransportOrder(TransportOrderCreationTO to) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void activateTransportOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderState(
      TCSObjectReference<TransportOrder> ref,
      TransportOrder.State newState)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderProcessingVehicle(TCSObjectReference<TransportOrder> orderRef,
                                                 TCSObjectReference<Vehicle> vehicleRef,
                                                 List<DriveOrder> driveOrders)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                           List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderWrappingSequence(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public OrderSequence createOrderSequence() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void addOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void removeOrderSequenceOrder(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<TransportOrder> orderRef) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setOrderSequenceFinishedIndex(
      TCSObjectReference<OrderSequence> ref,
      int index) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setOrderSequenceComplete(TCSObjectReference<OrderSequence> ref) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setOrderSequenceFinished(
      TCSObjectReference<OrderSequence> ref) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setOrderSequenceFailureFatal(
      TCSObjectReference<OrderSequence> ref,
      boolean fatal) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                                     boolean immediateAbort,
                                     boolean disableVehicle) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void withdrawTransportOrderByVehicle(
      TCSObjectReference<Vehicle> vehicleRef,
      boolean immediateAbort,
      boolean disableVehicle)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void releaseVehicle(TCSObjectReference<Vehicle> vehicleRef) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef,
                                     Object message) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public List<TransportOrder> createTransportOrdersFromScript(
      String fileName)
      throws ObjectUnknownException, IOException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public final Set<TCSResource<?>> expandResources(Set<TCSResourceReference<?>> resources)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      return getModel().expandResources(resources);
    }
  }

  @Deprecated
  public void updateRoutingTopology() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public List<org.opentcs.access.TravelCosts> getTravelCosts(
      TCSObjectReference<Vehicle> vTypeRef,
      TCSObjectReference<Location> srcRef,
      Set<TCSObjectReference<Location>> destRefs)
      throws ObjectUnknownException {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public <T extends org.opentcs.access.queries.Query<T>> T query(Class<T> clazz) {
    // If the given query isn't available in this state, return null.
    if (!Queries.availableInState(clazz, getState())) {
      return null;
    }
    if (org.opentcs.access.queries.QueryTopologyInfo.class.equals(clazz)) {
      return clazz.cast(new org.opentcs.access.queries.QueryTopologyInfo(getModel().getInfo()));
    }
    else {
      // The given query should be available in this state, but isn't - throw an
      // exception.
      throw new IllegalStateException("Query " + clazz.getName()
          + " should be available in kernel state " + getState().name()
          + ", but noone processed it.");
    }
  }

  @Deprecated
  public double getSimulationTimeFactor() {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public void setSimulationTimeFactor(double angle) {
    throw new org.opentcs.access.UnsupportedKernelOpException(unsupportedMsg());
  }

  @Deprecated
  public final Set<org.opentcs.access.ConfigurationItemTO> getConfigurationItems() {
    Set<org.opentcs.access.ConfigurationItemTO> result = new HashSet<>();
    Set<org.opentcs.util.configuration.ConfigurationItem> allItems
        = org.opentcs.util.configuration.Configuration.getInstance().getConfigurationItems();
    for (org.opentcs.util.configuration.ConfigurationItem item : allItems) {
      org.opentcs.access.ConfigurationItemTO itemTO = new org.opentcs.access.ConfigurationItemTO();
      itemTO.setNamespace(item.getNamespace());
      itemTO.setKey(item.getKey());
      itemTO.setValue(item.getValue());
      itemTO.setConstraint(item.getConstraint());
      itemTO.setDescription(item.getDescription());
      result.add(itemTO);
    }
    return result;
  }

  @Deprecated
  public final void setConfigurationItem(org.opentcs.access.ConfigurationItemTO itemTO) {
    requireNonNull(itemTO, "itemTO");
    org.opentcs.util.configuration.ConfigurationItem item
        = new org.opentcs.util.configuration.ConfigurationItem(itemTO.getNamespace(),
                                                               itemTO.getKey(),
                                                               itemTO.getDescription(),
                                                               itemTO.getConstraint(),
                                                               itemTO.getValue());
    org.opentcs.util.configuration.Configuration.getInstance().setConfigurationItem(item);
  }

  protected Object getGlobalSyncObject() {
    return globalSyncObject;
  }

  protected ModelPersister getModelPersister() {
    return modelPersister;
  }

  protected TCSObjectPool getGlobalObjectPool() {
    return globalObjectPool;
  }

  protected Model getModel() {
    return model;
  }

  private String unsupportedMsg() {
    return "Called operation not supported in this kernel mode (" + getState().name() + ").";
  }
}
