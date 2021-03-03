/*
 * openTCS copyright information:
 * Copyright (c) 2005 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.Provider;
import org.opentcs.util.eventsystem.CentralEventHub;
import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opentcs.access.ConfigurationItemTO;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.Kernel.State;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.TCSKernelStateEvent;
import org.opentcs.access.TCSModelTransitionEvent;
import org.opentcs.access.TravelCosts;
import org.opentcs.access.UnsupportedKernelOpException;
import org.opentcs.access.queries.Query;
import org.opentcs.algorithms.KernelExtension;
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
import org.opentcs.data.user.UserPermission;
import org.opentcs.drivers.CommunicationAdapter;
import org.opentcs.drivers.CommunicationAdapterRegistry;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.drivers.VehicleControllerPool;
import org.opentcs.drivers.VehicleManagerPool;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.persistence.OrderPersister;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.eventsystem.EventFilter;
import org.opentcs.util.eventsystem.EventHub;
import org.opentcs.util.eventsystem.EventListener;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * This class implements the standard openTCS kernel.
 * <hr>
 * <h4>Configuration entries</h4>
 * <dl>
 * <dt><b>messageBufferCapacity:</b></dt>
 * <dd>An integer defining the maximum number of messages to be kept in the
 * kernel's message buffer (default: 500).</dd>
 * <dt><b>defaultModel:</b></dt>
 * <dd>The name of the default model to load if told to do so via command line
 * (default: empty string).</dd>
 * </dl>
 * <hr>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Singleton
final class StandardKernel
    implements LocalKernel, Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(StandardKernel.class.getName());
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(StandardKernel.class.getName());
  /**
   * Message for UnsupportedKernelOpExceptions thrown in user management
   * methods.
   */
  private static final String msgUserManagementUnsupported
      = "No user management in local kernel";
  /**
   * The persister loading and storing model data.
   */
  final ModelPersister modelPersister;
  /**
   * The persister loading and storing transport order data.
   */
  final OrderPersister orderPersister;
  /**
   * A map to state providers used when switching kernel states.
   */
  private final Map<Kernel.State, Provider<KernelState>> stateProviders;
  /**
   * An event hub for synchronous dispatching of events.
   */
  private final EventHub<TCSEvent> eventHub;
  /**
   * This kernel's order receivers.
   */
  private final Set<KernelExtension> kernelExtensions = new HashSet<>();
  /**
   * An object that is used for synchronizing the thread executing this kernel's
   * {@link #run() run()} method.
   */
  private final Object waitObject = new Object();
  /**
   * This kernel's <em>terminated</em> flag.
   */
  private volatile boolean terminated;
  /**
   * Indicates whether the kernel has finished its shutdown sequence.
   */
  private volatile boolean terminationFinished;
  /**
   * The kernel implementing the actual functionality for the current mode.
   */
  private KernelState kernelState;

  /**
   * Creates a new kernel.
   *
   * @param eventHub The central event hub to be used.
   * @param stateProviders The state map to be used.
   * @param modelPersister The model persister to be used.
   * @param orderPersister The order persister to be used.
   */
  @Inject
  StandardKernel(@CentralEventHub EventHub<TCSEvent> eventHub,
                 Map<Kernel.State, Provider<KernelState>> stateProviders,
                 ModelPersister modelPersister,
                 OrderPersister orderPersister) {
    log.finer("method entry");
    this.eventHub = Objects.requireNonNull(eventHub, "eventHub is null");
    this.stateProviders = Objects.requireNonNull(stateProviders,
                                                 "stateProviders is null");
    this.modelPersister = Objects.requireNonNull(modelPersister,
                                                 "modelPersister is null");
    this.orderPersister = Objects.requireNonNull(orderPersister,
                                                 "orderPersister is null");
  }

  @Override
  public void initialize() {
    // First of all, start all kernel extensions that are already registered.
    for (KernelExtension extension : kernelExtensions) {
      extension.plugIn();
    }

    // Initial state is modelling.
    setState(State.MODELLING);

    log.fine("Starting kernel thread");
    Thread kernelThread = new Thread(this, "kernelThread");
    kernelThread.start();
  }

  @Override
  public void waitForTermination() {
    synchronized (waitObject) {
      while (!terminationFinished) {
        try {
          waitObject.wait(100);
        }
        catch (InterruptedException exc) {
          log.log(Level.SEVERE, "Unexpectedly interrupted, ignored.", exc);
        }
      }
    }
  }

  // Implementation of interface Runnable starts here.
  @Override
  public void run() {
    synchronized (waitObject) {
      // Wait until terminated.
      while (!terminated) {
        try {
          waitObject.wait();
        }
        catch (InterruptedException exc) {
          throw new IllegalStateException("Unexpectedly interrupted", exc);
        }
      }
    }
    log.info("Termination flag set, terminating...");
    // Sleep a bit so clients have some time to receive an event for the
    // SHUTDOWN state change and shut down gracefully themselves.
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException exc) {
      log.log(Level.SEVERE, "Unexpectedly interrupted, ignored.", exc);
    }
    // Shut down all kernel extensions.
    log.fine("Shutting down kernel extensions...");
    for (KernelExtension extension : kernelExtensions) {
      extension.plugOut();
    }
    log.info("Kernel thread finished.");
    terminationFinished = true;
  }

  // Implementation of interface Kernel starts here.
  @Override
  public Set<UserPermission> getUserPermissions() {
    log.finer("method entry");
    return EnumSet.allOf(UserPermission.class);
  }

  @Override
  public void createUser(String userName, String userPassword,
                         Set<UserPermission> userPermissions)
      throws UnsupportedKernelOpException {
    log.finer("method entry");
    throw new UnsupportedKernelOpException(msgUserManagementUnsupported);
  }

  @Override
  public void setUserPassword(String userName, String userPassword)
      throws UnsupportedKernelOpException {
    log.finer("method entry");
    throw new UnsupportedKernelOpException(msgUserManagementUnsupported);
  }

  @Override
  public void setUserPermissions(String userName,
                                 Set<UserPermission> userPermissions)
      throws UnsupportedKernelOpException {
    log.finer("method entry");
    throw new UnsupportedKernelOpException(msgUserManagementUnsupported);
  }

  @Override
  public void removeUser(String userName)
      throws UnsupportedKernelOpException {
    log.finer("method entry");
    throw new UnsupportedKernelOpException(msgUserManagementUnsupported);
  }

  @Override
  public State getState() {
    log.finer("method entry");
    return kernelState.getState();
  }

  @Override
  public void setState(State newState)
      throws IllegalArgumentException {
    log.finer("method entry");
    Objects.requireNonNull(newState, "newState is null");
    final Kernel.State oldState;
    if (kernelState != null) {
      oldState = kernelState.getState();
      // Don't do anything if the new state is the same as the current one.
      if (oldState.equals(newState)) {
        log.warning("Already in state " + newState + ", doing nothing.");
        return;
      }
      // Let listeners know we're in transition.
      emitStateEvent(oldState, newState, false);
      // Terminate previous state.
      kernelState.terminate();
    }
    else {
      oldState = null;
    }
    switch (newState) {
      case SHUTDOWN:
        log.info("Switching kernel to state SHUTDOWN...");
        kernelState = stateProviders.get(Kernel.State.SHUTDOWN).get();
        kernelState.initialize();
        terminate();
        break;
      case MODELLING:
        log.info("Switching kernel to state MODELLING...");
        kernelState = stateProviders.get(Kernel.State.MODELLING).get();
        kernelState.initialize();
        break;
      case OPERATING:
        log.info("Switching kernel to state OPERATING...");
        kernelState = stateProviders.get(Kernel.State.OPERATING).get();
        kernelState.initialize();
        break;
      default:
        throw new IllegalArgumentException("Unexpected state: " + newState);
    }
    emitStateEvent(oldState, newState, true);
    publishMessage("Kernel is now in state " + newState, Message.Type.INFO);
  }

  @Override
  public List<TravelCosts> getTravelCosts(
      TCSObjectReference<Vehicle> vRef,
      TCSObjectReference<Location> srcRef,
      Set<TCSObjectReference<Location>> destRefs) {
    log.finer("method entry");
    return kernelState.getTravelCosts(vRef, srcRef, destRefs);
  }

  @Override
  public Set<String> getModelNames() {
    log.finer("method entry");
    return kernelState.getModelNames();
  }

  @Override
  public String getCurrentModelName() {
    log.finer("method entry");
    return kernelState.getCurrentModelName();
  }

  @Override
  public void createModel(String modelName) {
    log.finer("method entry");
    final String oldModelName = kernelState.getCurrentModelName();
    emitModelEvent(oldModelName, modelName, true, false);
    kernelState.createModel(modelName);
    emitModelEvent(oldModelName, modelName, true, true);
    publishMessage("Kernel created model " + modelName, Message.Type.INFO);
  }

  @Override
  public void loadModel(String newModelName)
      throws IOException {
    log.finer("method entry");
    final String oldModelName;
    oldModelName = kernelState.getCurrentModelName();
    // Let listeners know we're in transition.
    emitModelEvent(oldModelName, newModelName, true, false);
    // Load the new model
    kernelState.loadModel(newModelName);
    // If loading the model was successful, remember it as the new default.
    configStore.setString("defaultModel", newModelName);
    // Let listeners know we're done with the transition.
    emitModelEvent(oldModelName, newModelName, true, true);
    publishMessage("Kernel loaded model " + newModelName, Message.Type.INFO);
  }

  @Override
  public void saveModel(String modelName, boolean overwrite)
      throws IOException {
    log.finer("method entry");
    final String oldModelName = kernelState.getCurrentModelName();
    final String newModelName = (modelName == null) ? oldModelName : modelName;
    // Let listeners know we're in transition.
    emitModelEvent(oldModelName, newModelName, false, false);
    kernelState.saveModel(modelName, overwrite);
    // Let listeners know we're done with the transition.
    emitModelEvent(oldModelName, newModelName, false, true);
    publishMessage("Kernel saved model " + newModelName, Message.Type.INFO);
  }

  @Override
  public void removeModel(String rmName)
      throws IOException {
    log.finer("method entry");
    kernelState.removeModel(rmName);
  }

  @Override
  public <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                 TCSObjectReference<T> ref)
      throws CredentialsException {
    log.finer("method entry");
    return kernelState.getTCSObject(clazz, ref);
  }

  @Override
  public <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                 String name)
      throws CredentialsException {
    log.finer("method entry");
    return kernelState.getTCSObject(clazz, name);
  }

  @Override
  public <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz)
      throws CredentialsException {
    log.finer("method entry");
    return kernelState.getTCSObjects(clazz);
  }

  @Override
  public <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz,
                                                       Pattern regexp)
      throws CredentialsException {
    log.finer("method entry");
    return kernelState.getTCSObjects(clazz, regexp);
  }

  @Override
  public void renameTCSObject(TCSObjectReference<?> ref, String newName)
      throws CredentialsException, ObjectUnknownException, ObjectExistsException {
    log.finer("method entry");
    kernelState.renameTCSObject(ref, newName);
  }

  @Override
  public void setTCSObjectProperty(TCSObjectReference<?> ref, String key,
                                   String value)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTCSObjectProperty(ref, key, value);
  }

  @Override
  public void clearTCSObjectProperties(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.clearTCSObjectProperties(ref);
  }

  @Override
  public void removeTCSObject(TCSObjectReference<?> ref) {
    log.finer("method entry");
    kernelState.removeTCSObject(ref);
  }

  @Override
  public Message publishMessage(String message, Message.Type type) {
    log.finer("method entry");
    return kernelState.publishMessage(message, type);
  }

  @Override
  @Deprecated
  public Layout createLayout(byte[] layoutData) {
    log.finer("method entry");
    return kernelState.createLayout(layoutData);
  }

  @Override
  @Deprecated
  public void setLayoutData(TCSObjectReference<Layout> ref, byte[] newData)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setLayoutData(ref, newData);
  }

  @Override
  public VisualLayout createVisualLayout()
      throws CredentialsException {
    log.finer("method entry");
    return kernelState.createVisualLayout();
  }

  @Override
  public void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                                    double scaleX)
      throws ObjectUnknownException, CredentialsException {
    log.finer("method entry");
    kernelState.setVisualLayoutScaleX(ref, scaleX);
  }

  @Override
  public void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                                    double scaleY)
      throws ObjectUnknownException, CredentialsException {
    log.finer("method entry");
    kernelState.setVisualLayoutScaleY(ref, scaleY);
  }

  @Override
  public void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                                    Map<String, Color> colors)
      throws ObjectUnknownException, CredentialsException {
    log.finer("method entry");
    kernelState.setVisualLayoutColors(ref, colors);
  }

  @Override
  public void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                                      Set<LayoutElement> elements)
      throws ObjectUnknownException, CredentialsException {
    log.finer("method entry");
    kernelState.setVisualLayoutElements(ref, elements);
  }

  @Override
  public void setVisualLayoutViewBookmarks(TCSObjectReference<VisualLayout> ref,
                                           List<ViewBookmark> bookmarks)
      throws ObjectUnknownException, CredentialsException {
    log.finer("method entry");
    kernelState.setVisualLayoutViewBookmarks(ref, bookmarks);
  }

  @Override
  public Point createPoint() {
    log.finer("method entry");
    return kernelState.createPoint();
  }

  @Override
  public void setPointPosition(TCSObjectReference<Point> ref, Triple position)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPointPosition(ref, position);
  }

  @Override
  public void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                              double angle)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPointVehicleOrientationAngle(ref, angle);
  }

  @Override
  public void setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPointType(ref, newType);
  }

  @Override
  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    return kernelState.createPath(srcRef, destRef);
  }

  @Override
  public void setPathLength(TCSObjectReference<Path> ref, long length)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPathLength(ref, length);
  }

  @Override
  public void setPathRoutingCost(TCSObjectReference<Path> ref, long cost)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPathRoutingCost(ref, cost);
  }

  @Override
  public void setPathMaxVelocity(TCSObjectReference<Path> ref, int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPathMaxVelocity(ref, velocity);
  }

  @Override
  public void setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPathMaxReverseVelocity(ref, velocity);
  }

  @Override
  public void setPathLocked(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setPathLocked(ref, locked);
  }

  @Override
  public Vehicle createVehicle() {
    log.finer("method entry");
    return kernelState.createVehicle();
  }

  @Override
  public void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                    int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleEnergyLevel(ref, energyLevel);
  }

  @Override
  public void setVehicleEnergyLevelCritical(TCSObjectReference<Vehicle> ref,
                                            int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleEnergyLevelCritical(ref, energyLevel);
  }

  @Override
  public void setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                        int energyLevel)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleEnergyLevelGood(ref, energyLevel);
  }

  @Override
  public void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                          String rechargeOperation)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleRechargeOperation(ref, rechargeOperation);
  }

  @Override
  public void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                            List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleLoadHandlingDevices(ref, devices);
  }

  @Override
  public void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleMaxVelocity(ref, velocity);
  }

  @Override
  public void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                           int velocity)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleMaxReverseVelocity(ref, velocity);
  }

  @Override
  public void setVehicleState(TCSObjectReference<Vehicle> ref,
                              Vehicle.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleState(ref, newState);
  }

  @Override
  public void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                  Vehicle.ProcState newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleProcState(ref, newState);
  }

  @Override
  public void setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                     CommunicationAdapter.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleAdapterState(ref, newState);
  }

  @Override
  public void setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleLength(ref, length);
  }

  @Override
  public void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehiclePosition(vehicleRef, pointRef);
  }

  @Override
  public void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleNextPosition(vehicleRef, pointRef);
  }

  @Override
  public void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                        Triple newPosition)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehiclePrecisePosition(vehicleRef, newPosition);
  }

  @Override
  public void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                         double angle)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleOrientationAngle(vehicleRef, angle);
  }

  @Override
  public void setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                       TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleTransportOrder(vehicleRef, orderRef);
  }

  @Override
  public void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleOrderSequence(vehicleRef, seqRef);
  }

  @Override
  public void setVehicleRouteProgressIndex(TCSObjectReference<Vehicle> vehicleRef,
                                           int index)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setVehicleRouteProgressIndex(vehicleRef, index);
  }

  @Override
  public LocationType createLocationType() {
    log.finer("method entry");
    return kernelState.createLocationType();
  }

  @Override
  public void addLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.addLocationTypeAllowedOperation(ref, operation);
  }

  @Override
  public void removeLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.removeLocationTypeAllowedOperation(ref, operation);
  }

  @Override
  public Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    return kernelState.createLocation(typeRef);
  }

  @Override
  public void setLocationPosition(TCSObjectReference<Location> ref,
                                  Triple position)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setLocationPosition(ref, position);
  }

  @Override
  public void setLocationType(TCSObjectReference<Location> ref,
                              TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setLocationType(ref, typeRef);
  }

  @Override
  public void connectLocationToPoint(TCSObjectReference<Location> locRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.connectLocationToPoint(locRef, pointRef);
  }

  @Override
  public void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.disconnectLocationFromPoint(locRef, pointRef);
  }

  @Override
  public void addLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.addLocationLinkAllowedOperation(locRef, pointRef, operation);
  }

  @Override
  public void removeLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.removeLocationLinkAllowedOperation(locRef, pointRef, operation);
  }

  @Override
  public void clearLocationLinkAllowedOperations(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.clearLocationLinkAllowedOperations(locRef, pointRef);
  }

  @Override
  public Block createBlock() {
    log.finer("method entry");
    return kernelState.createBlock();
  }

  @Override
  public void addBlockMember(TCSObjectReference<Block> ref,
                             TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.addBlockMember(ref, newMemberRef);
  }

  @Override
  public void removeBlockMember(TCSObjectReference<Block> ref,
                                TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.removeBlockMember(ref, rmMemberRef);
  }

  @Override
  public Group createGroup() {
    log.finer("method entry");
    return kernelState.createGroup();
  }

  @Override
  public void addGroupMember(TCSObjectReference<Group> ref,
                             TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.addGroupMember(ref, newMemberRef);
  }

  @Override
  public void removeGroupMember(TCSObjectReference<Group> ref,
                                TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.removeGroupMember(ref, rmMemberRef);
  }

  @Override
  public StaticRoute createStaticRoute() {
    log.finer("method entry");
    return kernelState.createStaticRoute();
  }

  @Override
  public void addStaticRouteHop(TCSObjectReference<StaticRoute> ref,
                                TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.addStaticRouteHop(ref, newHopRef);
  }

  @Override
  public void clearStaticRouteHops(TCSObjectReference<StaticRoute> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.clearStaticRouteHops(ref);
  }

  @Override
  public void attachResource(TCSResourceReference<?> resource,
                             TCSResourceReference<?> newResource)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.attachResource(resource, newResource);
  }

  @Override
  public void detachResource(TCSResourceReference<?> resource,
                             TCSResourceReference<?> rmResource)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.detachResource(resource, rmResource);
  }

  @Override
  public TransportOrder createTransportOrder(List<Destination> destinations) {
    log.finer("method entry");
    return kernelState.createTransportOrder(destinations);
  }

  @Override
  public void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTransportOrderDeadline(ref, deadline);
  }

  @Override
  public void activateTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.activateTransportOrder(ref);
  }

  @Override
  public void setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                     TransportOrder.State newState)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTransportOrderState(ref, newState);
  }

  @Override
  public void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTransportOrderIntendedVehicle(orderRef, vehicleRef);
  }

  @Override
  public void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTransportOrderProcessingVehicle(orderRef, vehicleRef);
  }

  @Override
  public void setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTransportOrderFutureDriveOrders(orderRef, newOrders);
  }

  @Override
  public void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    log.finer("method entry");
    kernelState.setTransportOrderInitialDriveOrder(ref);
  }

  @Override
  public void setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    log.finer("method entry");
    kernelState.setTransportOrderNextDriveOrder(ref);
  }

  @Override
  public void addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.addTransportOrderDependency(orderRef, newDepRef);
  }

  @Override
  public void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.removeTransportOrderDependency(orderRef, rmDepRef);
  }

  @Override
  public void addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.addTransportOrderRejection(orderRef, newRejection);
  }

  @Override
  public void setTransportOrderWrappingSequence(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTransportOrderWrappingSequence(orderRef, seqRef);
  }

  @Override
  public void setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.setTransportOrderDispensable(orderRef, dispensable);
  }

  @Override
  public OrderSequence createOrderSequence() {
    log.finer("method entry");
    return kernelState.createOrderSequence();
  }

  @Override
  public void addOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                                    TCSObjectReference<TransportOrder> orderRef) {
    log.finer("method entry");
    kernelState.addOrderSequenceOrder(seqRef, orderRef);
  }

  @Override
  public void removeOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                                       TCSObjectReference<TransportOrder> orderRef) {
    log.finer("method entry");
    kernelState.removeOrderSequenceOrder(seqRef, orderRef);
  }

  @Override
  public void setOrderSequenceFinishedIndex(TCSObjectReference<OrderSequence> seqRef,
                                            int index) {
    log.finer("method entry");
    kernelState.setOrderSequenceFinishedIndex(seqRef, index);
  }

  @Override
  public void setOrderSequenceComplete(TCSObjectReference<OrderSequence> seqRef) {
    log.finer("method entry");
    kernelState.setOrderSequenceComplete(seqRef);
  }

  @Override
  public void setOrderSequenceFinished(TCSObjectReference<OrderSequence> seqRef) {
    log.finer("method entry");
    kernelState.setOrderSequenceFinished(seqRef);
  }

  @Override
  public void setOrderSequenceFailureFatal(TCSObjectReference<OrderSequence> seqRef,
                                           boolean fatal) {
    log.finer("method entry");
    kernelState.setOrderSequenceFailureFatal(seqRef, fatal);
  }

  @Override
  public void setOrderSequenceIntendedVehicle(TCSObjectReference<OrderSequence> seqRef,
                                              TCSObjectReference<Vehicle> vehicleRef) {
    log.finer("method entry");
    kernelState.setOrderSequenceIntendedVehicle(seqRef, vehicleRef);
  }

  @Override
  public void setOrderSequenceProcessingVehicle(TCSObjectReference<OrderSequence> seqRef,
                                                TCSObjectReference<Vehicle> vehicleRef) {
    log.finer("method entry");
    kernelState.setOrderSequenceProcessingVehicle(seqRef, vehicleRef);
  }

  @Override
  public void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                                     boolean disableVehicle)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.withdrawTransportOrder(ref, disableVehicle);
  }

  @Override
  public void withdrawTransportOrderByVehicle(
      TCSObjectReference<Vehicle> vehicleRef, boolean disableVehicle)
      throws ObjectUnknownException {
    log.finer("method entry");
    kernelState.withdrawTransportOrderByVehicle(vehicleRef, disableVehicle);
  }

  @Override
  public void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable) {
    log.finer("method entry");
    kernelState.dispatchVehicle(vehicleRef, setIdleIfUnavailable);
  }

  @Override
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef,
                                     Object message)
      throws ObjectUnknownException, CredentialsException {
    log.finer("method entry");
    kernelState.sendCommAdapterMessage(vehicleRef, message);
  }

  @Override
  public List<TransportOrder> createTransportOrdersFromScript(String fileName)
      throws ObjectUnknownException, IOException {
    log.finer("method entry");
    return kernelState.createTransportOrdersFromScript(fileName);
  }

  @Override
  public Set<TCSResource> expandResources(Set<TCSResourceReference> resources)
      throws ObjectUnknownException {
    log.finer("method entry");
    return kernelState.expandResources(resources);
  }

  @Override
  public <T extends Query<T>> T query(Class<T> clazz) {
    log.finer("method entry");
    return kernelState.query(clazz);
  }

  @Override
  public double getSimulationTimeFactor() {
    log.finer("method entry");
    return kernelState.getSimulationTimeFactor();
  }

  @Override
  public void setSimulationTimeFactor(double angle) {
    log.finer("method entry");
    kernelState.setSimulationTimeFactor(angle);
  }

  @Override
  public Set<ConfigurationItemTO> getConfigurationItems() {
    log.finer("method entry");
    return kernelState.getConfigurationItems();
  }

  @Override
  public void setConfigurationItem(ConfigurationItemTO itemTO) {
    log.finer("method entry");
    kernelState.setConfigurationItem(itemTO);
  }

  @Override
  public String getDefaultModelName() {
    return configStore.getString("defaultModel", "");
  }

  @Override
  public VehicleManagerPool getVehicleManagerPool() {
    log.finer("method entry");
    return kernelState.getVehicleManagerPool();
  }

  @Override
  public VehicleControllerPool getVehicleControllerPool() {
    log.finer("method entry");
    return kernelState.getVehicleControllerPool();
  }

  @Override
  public CommunicationAdapterRegistry getCommAdapterRegistry() {
    log.finer("method entry");
    return kernelState.getCommAdapterRegistry();
  }

  @Override
  public Scheduler getScheduler() {
    log.finer("method entry");
    return kernelState.getScheduler();
  }

  @Override
  public void addKernelExtension(final KernelExtension newExtension) {
    log.fine("method entry");
    Objects.requireNonNull(newExtension, "newExtension is null");
    kernelExtensions.add(newExtension);
  }

  @Override
  public void removeKernelExtension(final KernelExtension rmExtension) {
    log.fine("method entry");
    Objects.requireNonNull(rmExtension, "rmExtension is null");
    kernelExtensions.remove(rmExtension);
  }

  // Event management methods start here.
  @Override
  public void addEventListener(EventListener<TCSEvent> listener,
                               EventFilter<TCSEvent> filter) {
    log.finer("method entry");
    eventHub.addEventListener(listener, filter);
  }

  @Override
  public void removeEventListener(EventListener<TCSEvent> listener) {
    log.finer("method entry");
    eventHub.removeEventListener(listener);
  }

  // Methods not declared in any interface start here.
  /**
   * Generates an event for a state change.
   *
   * @param leftState The state left.
   * @param enteredState The state entered.
   * @param transitionFinished Whether the transition is finished or not.
   */
  private void emitStateEvent(State leftState,
                              State enteredState,
                              boolean transitionFinished) {
    assert enteredState != null;
    TCSKernelStateEvent event = new TCSKernelStateEvent(leftState,
                                                        enteredState,
                                                        transitionFinished);
    log.fine("Emitting kernel state event: " + event);
    eventHub.processEvent(event);
  }

  /**
   * Generates an event for a Model change.
   *
   * @param oldModelName The state left.
   * @param enteredModelName The state entered.
   * @param modelContentChanged Whether the model's content actually changed.
   * @param transitionFinished Whether the transition is finished or not.
   */
  private void emitModelEvent(String oldModelName,
                              String enteredModelName,
                              boolean modelContentChanged,
                              boolean transitionFinished) {
    assert enteredModelName != null;
    TCSModelTransitionEvent event
        = new TCSModelTransitionEvent(oldModelName,
                                      enteredModelName,
                                      modelContentChanged,
                                      transitionFinished);
    log.fine("Emitting model transition event: " + event);
    eventHub.processEvent(event);
  }

  /**
   * Terminates this Kernel.
   */
  private void terminate() {
    // Note that the actual shutdown of extensions should happen when the kernel
    // thread (see run()) finishes, not here.
    // Set the terminated flag and wake up this kernel's thread.
    terminated = true;
    synchronized (waitObject) {
      waitObject.notifyAll();
    }
  }
}
