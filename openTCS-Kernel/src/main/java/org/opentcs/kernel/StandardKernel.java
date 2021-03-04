/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Provider;
import java.awt.Color;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.Kernel.State;
import org.opentcs.access.KernelStateTransitionEvent;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.ModelTransitionEvent;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.kernel.KernelExecutor;
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
import org.opentcs.util.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the standard openTCS kernel.
 * <hr>
 * <h4>Configuration entries</h4>
 * <dl>
 * <dt><b>messageBufferCapacity:</b></dt>
 * <dd>An integer defining the maximum number of messages to be kept in the
 * kernel's message buffer (default: 500).</dd>
 * </dl>
 * <hr>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class StandardKernel
    implements LocalKernel,
               Runnable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardKernel.class);
  /**
   * Message for UnsupportedKernelOpExceptions thrown in user management
   * methods.
   */
  private static final String MSG_NO_USER_MANAGEMENT = "No user management in local kernel";
  /**
   * A map to state providers used when switching kernel states.
   */
  private final Map<Kernel.State, Provider<KernelState>> stateProviders;
  /**
   * An event hub for synchronous dispatching of events.
   */
  @SuppressWarnings("deprecation")
  private final org.opentcs.util.eventsystem.EventHub<org.opentcs.util.eventsystem.TCSEvent> eventHub;
  /**
   * The application's event bus.
   */
  private final EventBus eventBus;
  /**
   * Our executor.
   */
  private final ScheduledExecutorService kernelExecutor;
  /**
   * This kernel's order receivers.
   */
  private final Set<KernelExtension> kernelExtensions = new HashSet<>();
  /**
   * Functions as a barrier for the kernel's {@link #run() run()} method.
   */
  private final Semaphore terminationSemaphore = new Semaphore(0);
  /**
   * This kernel's <em>initialized</em> flag.
   */
  private volatile boolean initialized;
  /**
   * The kernel implementing the actual functionality for the current mode.
   */
  private KernelState kernelState;

  /**
   * Creates a new kernel.
   *
   * @param eventHub The central event hub to be used.
   * @param kernelExecutor An executor for this kernel's tasks.
   * @param stateProviders The state map to be used.
   */
  @Inject
  @SuppressWarnings("deprecation")
  StandardKernel(
      @org.opentcs.customizations.kernel.CentralEventHub org.opentcs.util.eventsystem.EventHub<org.opentcs.util.eventsystem.TCSEvent> eventHub,
      @ApplicationEventBus EventBus eventBus,
      @KernelExecutor ScheduledExecutorService kernelExecutor,
      Map<Kernel.State, Provider<KernelState>> stateProviders) {
    this.eventHub = requireNonNull(eventHub, "eventHub");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    this.stateProviders = requireNonNull(stateProviders, "stateProviders");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    // First of all, start all kernel extensions that are already registered.
    for (KernelExtension extension : kernelExtensions) {
      LOG.debug("Initializing extension: {}", extension.getName());
      extension.initialize();
    }

    // Initial state is modelling.
    setState(State.MODELLING);

    initialized = true;
    LOG.debug("Starting kernel thread");
    Thread kernelThread = new Thread(this, "kernelThread");
    kernelThread.start();
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    // Note that the actual shutdown of extensions should happen when the kernel
    // thread (see run()) finishes, not here.
    // Set the terminated flag and wake up this kernel's thread for termination.
    initialized = false;
    terminationSemaphore.release();
  }

  @Override
  public void run() {
    // Wait until terminated.
    terminationSemaphore.acquireUninterruptibly();
    LOG.info("Terminating...");
    // Sleep a bit so clients have some time to receive an event for the
    // SHUTDOWN state change and shut down gracefully themselves.
    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    // Shut down all kernel extensions.
    LOG.debug("Shutting down kernel extensions...");
    for (KernelExtension extension : kernelExtensions) {
      extension.terminate();
    }
    kernelExecutor.shutdown();
    LOG.info("Kernel thread finished.");
  }

  // Implementation of interface Kernel starts here.
  @Override
  @Deprecated
  public Set<org.opentcs.data.user.UserPermission> getUserPermissions() {
    LOG.debug("method entry");
    return EnumSet.allOf(org.opentcs.data.user.UserPermission.class);
  }

  @Override
  @Deprecated
  public void createUser(String userName, String userPassword,
                         Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.access.UnsupportedKernelOpException {
    LOG.debug("method entry");
    throw new org.opentcs.access.UnsupportedKernelOpException(MSG_NO_USER_MANAGEMENT);
  }

  @Override
  @Deprecated
  public void setUserPassword(String userName, String userPassword)
      throws org.opentcs.access.UnsupportedKernelOpException {
    LOG.debug("method entry");
    throw new org.opentcs.access.UnsupportedKernelOpException(MSG_NO_USER_MANAGEMENT);
  }

  @Override
  @Deprecated
  public void setUserPermissions(String userName,
                                 Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.access.UnsupportedKernelOpException {
    LOG.debug("method entry");
    throw new org.opentcs.access.UnsupportedKernelOpException(MSG_NO_USER_MANAGEMENT);
  }

  @Override
  @Deprecated
  public void removeUser(String userName)
      throws org.opentcs.access.UnsupportedKernelOpException {
    LOG.debug("method entry");
    throw new org.opentcs.access.UnsupportedKernelOpException(MSG_NO_USER_MANAGEMENT);
  }

  @Override
  public State getState() {
    LOG.debug("method entry");
    return kernelState.getState();
  }

  @Override
  public void setState(State newState)
      throws IllegalArgumentException {
    Objects.requireNonNull(newState, "newState is null");
    final Kernel.State oldState;
    if (kernelState != null) {
      oldState = kernelState.getState();
      // Don't do anything if the new state is the same as the current one.
      if (oldState == newState) {
        LOG.debug("Already in state '{}', doing nothing.", newState.name());
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
    LOG.info("Switching kernel to state '{}'", newState.name());
    switch (newState) {
      case SHUTDOWN:
        kernelState = stateProviders.get(Kernel.State.SHUTDOWN).get();
        kernelState.initialize();
        terminate();
        break;
      case MODELLING:
        kernelState = stateProviders.get(Kernel.State.MODELLING).get();
        kernelState.initialize();
        break;
      case OPERATING:
        kernelState = stateProviders.get(Kernel.State.OPERATING).get();
        kernelState.initialize();
        break;
      default:
        throw new IllegalArgumentException("Unexpected state: " + newState);
    }
    emitStateEvent(oldState, newState, true);
    publishUserNotification(new UserNotification("Kernel is now in state " + newState,
                                                 UserNotification.Level.INFORMATIONAL));
  }

  @Override
  @Deprecated
  public List<org.opentcs.access.TravelCosts> getTravelCosts(
      TCSObjectReference<Vehicle> vRef,
      TCSObjectReference<Location> srcRef,
      Set<TCSObjectReference<Location>> destRefs) {
    LOG.debug("method entry");
    return kernelState.getTravelCosts(vRef, srcRef, destRefs);
  }

  @Override
  @Deprecated
  public String getPersistentModelName()
      throws IllegalStateException {
    LOG.debug("method entry");
    return kernelState.getPersistentModelName().orElse(null);
  }

  @Override
  @Deprecated
  public String getLoadedModelName() {
    LOG.debug("method entry");
    return kernelState.getLoadedModelName();
  }

  @Override
  @Deprecated
  public void createPlantModel(PlantModelCreationTO to) {

    boolean kernelInOperating = getState() == Kernel.State.OPERATING;
    // If we are in state operating, change the kernel state before creating the plant model
    if (kernelInOperating) {
      setState(Kernel.State.MODELLING);
    }

    final String oldModelName = kernelState.getLoadedModelName();
    emitModelEvent(oldModelName, to.getName(), true, false);
    kernelState.createPlantModel(to);
    kernelState.savePlantModel();

    // If we were in state operating before, change the kernel state back to operating
    if (kernelInOperating) {
      setState(Kernel.State.OPERATING);
    }

    emitModelEvent(oldModelName, to.getName(), true, true);
    publishUserNotification(new UserNotification("Kernel created model " + to.getName(),
                                                 UserNotification.Level.INFORMATIONAL));
  }

  @Override
  @Deprecated
  public void createModel(String modelName) {
    LOG.debug("method entry");
    final String oldModelName = kernelState.getLoadedModelName();
    emitModelEvent(oldModelName, modelName, true, false);
    kernelState.createModel(modelName);
    emitModelEvent(oldModelName, modelName, true, true);
    publishUserNotification(new UserNotification("Kernel created model " + modelName,
                                                 UserNotification.Level.INFORMATIONAL));
  }

  @Override
  @Deprecated
  public void loadPlantModel()
      throws IllegalStateException {
    final String oldModelName = kernelState.getLoadedModelName();
    final String newModelName = kernelState.getPersistentModelName().orElse("");
    // Let listeners know we're in transition.
    emitModelEvent(oldModelName, newModelName, true, false);
    // Load the new model
    kernelState.loadPlantModel();
    // Let listeners know we're done with the transition.
    emitModelEvent(oldModelName, newModelName, true, true);
    publishUserNotification(new UserNotification("Kernel loaded model " + newModelName,
                                                 UserNotification.Level.INFORMATIONAL));
  }

  @Override
  @Deprecated
  public void loadModel()
      throws IOException {
    LOG.debug("method entry");
    final String oldModelName = kernelState.getLoadedModelName();
    final String newModelName = kernelState.getPersistentModelName().orElse("");
    // Let listeners know we're in transition.
    emitModelEvent(oldModelName, newModelName, true, false);
    // Load the new model
    kernelState.loadModel();
    // Let listeners know we're done with the transition.
    emitModelEvent(oldModelName, newModelName, true, true);
    publishUserNotification(new UserNotification("Kernel loaded model " + newModelName,
                                                 UserNotification.Level.INFORMATIONAL));
  }

  @Override
  @Deprecated
  public void savePlantModel()
      throws IllegalStateException {
//    final String modelName = kernelState.getLoadedModelName();
//    // Let listeners know we're in transition.
//    emitModelEvent(modelName, modelName, false, false);
    kernelState.savePlantModel();
//    // Let listeners know we're done with the transition.
//    emitModelEvent(modelName, modelName, false, true);
//    publishUserNotification(new UserNotification("Kernel saved model " + modelName,
//                                                 UserNotification.Level.INFORMATIONAL));
  }

  @Override
  @Deprecated
  public void saveModel(String modelName)
      throws IOException {
    LOG.debug("method entry");
    final String oldModelName = kernelState.getLoadedModelName();
    final String newModelName = (modelName == null) ? oldModelName : modelName;
    // Let listeners know we're in transition.
    emitModelEvent(oldModelName, newModelName, false, false);
    kernelState.saveModel(newModelName);
    // Let listeners know we're done with the transition.
    emitModelEvent(oldModelName, newModelName, false, true);
    publishUserNotification(new UserNotification("Kernel saved model " + newModelName,
                                                 UserNotification.Level.INFORMATIONAL));
  }

  @Override
  @Deprecated
  public void removeModel()
      throws IOException {
    LOG.debug("method entry");
    kernelState.removeModel();
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                 TCSObjectReference<T> ref)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObject(clazz, ref);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                                 String name)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObject(clazz, name);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObjects(clazz);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz,
                                                       Pattern regexp)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObjects(clazz, regexp);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz,
                                                       Predicate<? super T> predicate)
      throws CredentialsException {
    return kernelState.getTCSObjects(clazz, predicate);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> T getTCSObjectOriginal(
      Class<T> clazz,
      TCSObjectReference<T> ref)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObjectOriginal(clazz, ref);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> T getTCSObjectOriginal(Class<T> clazz,
                                                         String name)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObjectOriginal(clazz, name);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> Set<T> getTCSObjectsOriginal(Class<T> clazz)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObjectsOriginal(clazz);
  }

  @Override
  @Deprecated
  public <T extends TCSObject<T>> Set<T> getTCSObjectsOriginal(Class<T> clazz,
                                                               Pattern regexp)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getTCSObjectsOriginal(clazz, regexp);
  }

  @Override
  @Deprecated
  public void renameTCSObject(TCSObjectReference<?> ref, String newName)
      throws CredentialsException, ObjectUnknownException, ObjectExistsException {
    LOG.debug("method entry");
    kernelState.renameTCSObject(ref, newName);
  }

  @Override
  @Deprecated
  public void setTCSObjectProperty(TCSObjectReference<?> ref, String key,
                                   String value)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTCSObjectProperty(ref, key, value);
  }

  @Override
  @Deprecated
  public void clearTCSObjectProperties(TCSObjectReference<?> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.clearTCSObjectProperties(ref);
  }

  @Override
  @Deprecated
  public void removeTCSObject(TCSObjectReference<?> ref) {
    LOG.debug("method entry");
    kernelState.removeTCSObject(ref);
  }

  @Override
  @Deprecated
  public void publishUserNotification(UserNotification notification) {
    LOG.debug("method entry");
    kernelState.publishUserNotification(notification);
  }

  @Override
  @Deprecated
  public List<UserNotification> getUserNotifications(Predicate<UserNotification> predicate)
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.getUserNotifications(predicate);
  }

  @Override
  @Deprecated
  public VisualLayout createVisualLayout()
      throws CredentialsException {
    LOG.debug("method entry");
    return kernelState.createVisualLayout();
  }

  @Override
  @Deprecated
  public void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                                    double scaleX)
      throws ObjectUnknownException, CredentialsException {
    LOG.debug("method entry");
    kernelState.setVisualLayoutScaleX(ref, scaleX);
  }

  @Override
  @Deprecated
  public void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                                    double scaleY)
      throws ObjectUnknownException, CredentialsException {
    LOG.debug("method entry");
    kernelState.setVisualLayoutScaleY(ref, scaleY);
  }

  @Override
  @Deprecated
  public void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                                    Map<String, Color> colors)
      throws ObjectUnknownException, CredentialsException {
    LOG.debug("method entry");
    kernelState.setVisualLayoutColors(ref, colors);
  }

  @Override
  @Deprecated
  public void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                                      Set<LayoutElement> elements)
      throws ObjectUnknownException, CredentialsException {
    LOG.debug("method entry");
    kernelState.setVisualLayoutElements(ref, elements);
  }

  @Override
  @Deprecated
  public void setVisualLayoutViewBookmarks(
      TCSObjectReference<VisualLayout> ref,
      List<org.opentcs.data.model.visualization.ViewBookmark> bookmarks)
      throws ObjectUnknownException, CredentialsException {
    LOG.debug("method entry");
    kernelState.setVisualLayoutViewBookmarks(ref, bookmarks);
  }

  @Override
  @Deprecated
  public Point createPoint() {
    LOG.debug("method entry");
    return kernelState.createPoint();
  }

  @Override
  @Deprecated
  public void setPointPosition(TCSObjectReference<Point> ref, Triple position)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPointPosition(ref, position);
  }

  @Override
  @Deprecated
  public void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                              double angle)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPointVehicleOrientationAngle(ref, angle);
  }

  @Override
  @Deprecated
  public void setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPointType(ref, newType);
  }

  @Override
  @Deprecated
  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    return kernelState.createPath(srcRef, destRef);
  }

  @Override
  @Deprecated
  public void setPathLength(TCSObjectReference<Path> ref, long length)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPathLength(ref, length);
  }

  @Override
  @Deprecated
  public void setPathRoutingCost(TCSObjectReference<Path> ref, long cost)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPathRoutingCost(ref, cost);
  }

  @Override
  @Deprecated
  public void setPathMaxVelocity(TCSObjectReference<Path> ref, int velocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPathMaxVelocity(ref, velocity);
  }

  @Override
  @Deprecated
  public void setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int velocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPathMaxReverseVelocity(ref, velocity);
  }

  @Override
  @Deprecated
  public void setPathLocked(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setPathLocked(ref, locked);
  }

  @Override
  @Deprecated
  public Vehicle createVehicle() {
    LOG.debug("method entry");
    return kernelState.createVehicle();
  }

  @Override
  @Deprecated
  public void setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                    int energyLevel)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleEnergyLevel(ref, energyLevel);
  }

  @Override
  @Deprecated
  public void setVehicleEnergyLevelCritical(TCSObjectReference<Vehicle> ref,
                                            int energyLevel)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleEnergyLevelCritical(ref, energyLevel);
  }

  @Override
  @Deprecated
  public void setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                        int energyLevel)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleEnergyLevelGood(ref, energyLevel);
  }

  @Override
  @Deprecated
  public void setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                          String rechargeOperation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleRechargeOperation(ref, rechargeOperation);
  }

  @Override
  @Deprecated
  public void setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                            List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleLoadHandlingDevices(ref, devices);
  }

  @Override
  @Deprecated
  public void setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                    int velocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleMaxVelocity(ref, velocity);
  }

  @Override
  @Deprecated
  public void setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                           int velocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleMaxReverseVelocity(ref, velocity);
  }

  @Override
  @Deprecated
  public void setVehicleState(TCSObjectReference<Vehicle> ref,
                              Vehicle.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleState(ref, newState);
  }

  @Override
  @Deprecated
  public void setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                  Vehicle.ProcState newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleProcState(ref, newState);
  }

  @Override
  @Deprecated
  public void setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                     VehicleCommAdapter.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleAdapterState(ref, newState);
  }

  @Override
  @Deprecated
  public void setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleLength(ref, length);
  }

  @Override
  @Deprecated
  public void setVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                              Set<String> processableCategories)
      throws ObjectUnknownException {
    kernelState.setVehicleProcessableCategories(ref, processableCategories);
  }

  @Override
  @Deprecated
  public void setVehiclePosition(TCSObjectReference<Vehicle> vehicleRef,
                                 TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehiclePosition(vehicleRef, pointRef);
  }

  @Override
  @Deprecated
  public void setVehicleNextPosition(TCSObjectReference<Vehicle> vehicleRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleNextPosition(vehicleRef, pointRef);
  }

  @Override
  @Deprecated
  public void setVehiclePrecisePosition(TCSObjectReference<Vehicle> vehicleRef,
                                        Triple newPosition)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehiclePrecisePosition(vehicleRef, newPosition);
  }

  @Override
  @Deprecated
  public void setVehicleOrientationAngle(TCSObjectReference<Vehicle> vehicleRef,
                                         double angle)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleOrientationAngle(vehicleRef, angle);
  }

  @Override
  @Deprecated
  public void setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                       TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleTransportOrder(vehicleRef, orderRef);
  }

  @Override
  @Deprecated
  public void setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleOrderSequence(vehicleRef, seqRef);
  }

  @Override
  @Deprecated
  public void setVehicleRouteProgressIndex(
      TCSObjectReference<Vehicle> vehicleRef,
      int index)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setVehicleRouteProgressIndex(vehicleRef, index);
  }

  @Override
  @Deprecated
  public LocationType createLocationType() {
    LOG.debug("method entry");
    return kernelState.createLocationType();
  }

  @Override
  @Deprecated
  public void addLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.addLocationTypeAllowedOperation(ref, operation);
  }

  @Override
  @Deprecated
  public void removeLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.removeLocationTypeAllowedOperation(ref, operation);
  }

  @Override
  @Deprecated
  public Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    return kernelState.createLocation(typeRef);
  }

  @Override
  @Deprecated
  public void setLocationPosition(TCSObjectReference<Location> ref,
                                  Triple position)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setLocationPosition(ref, position);
  }

  @Override
  @Deprecated
  public void setLocationType(TCSObjectReference<Location> ref,
                              TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setLocationType(ref, typeRef);
  }

  @Override
  @Deprecated
  public void connectLocationToPoint(TCSObjectReference<Location> locRef,
                                     TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.connectLocationToPoint(locRef, pointRef);
  }

  @Override
  @Deprecated
  public void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.disconnectLocationFromPoint(locRef, pointRef);
  }

  @Override
  @Deprecated
  public void addLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.addLocationLinkAllowedOperation(locRef, pointRef, operation);
  }

  @Override
  @Deprecated
  public void removeLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.removeLocationLinkAllowedOperation(locRef, pointRef, operation);
  }

  @Override
  @Deprecated
  public void clearLocationLinkAllowedOperations(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.clearLocationLinkAllowedOperations(locRef, pointRef);
  }

  @Override
  @Deprecated
  public Block createBlock() {
    LOG.debug("method entry");
    return kernelState.createBlock();
  }

  @Override
  @Deprecated
  public void addBlockMember(TCSObjectReference<Block> ref,
                             TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.addBlockMember(ref, newMemberRef);
  }

  @Override
  @Deprecated
  public void removeBlockMember(TCSObjectReference<Block> ref,
                                TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.removeBlockMember(ref, rmMemberRef);
  }

  @Override
  @Deprecated
  public Group createGroup() {
    LOG.debug("method entry");
    return kernelState.createGroup();
  }

  @Override
  @Deprecated
  public void addGroupMember(TCSObjectReference<Group> ref,
                             TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.addGroupMember(ref, newMemberRef);
  }

  @Override
  @Deprecated
  public void removeGroupMember(TCSObjectReference<Group> ref,
                                TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.removeGroupMember(ref, rmMemberRef);
  }

  @Override
  @Deprecated
  public org.opentcs.data.model.StaticRoute createStaticRoute() {
    LOG.debug("method entry");
    return kernelState.createStaticRoute();
  }

  @Override
  @Deprecated
  public void addStaticRouteHop(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref,
                                TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.addStaticRouteHop(ref, newHopRef);
  }

  @Override
  @Deprecated
  public void clearStaticRouteHops(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.clearStaticRouteHops(ref);
  }

  @Override
  @Deprecated
  public TransportOrder createTransportOrder(List<Destination> destinations) {
    LOG.debug("method entry");
    return kernelState.createTransportOrder(destinations);
  }

  @Override
  @Deprecated
  public TransportOrder createTransportOrder(TransportOrderCreationTO to) {
    LOG.debug("method entry");
    return kernelState.createTransportOrder(to);
  }

  @Override
  @Deprecated
  public void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderDeadline(ref, deadline);
  }

  @Deprecated
  @Override
  public void activateTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.activateTransportOrder(ref);
  }

  @Override
  @Deprecated
  public void setTransportOrderState(TCSObjectReference<TransportOrder> ref,
                                     TransportOrder.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderState(ref, newState);
  }

  @Override
  @Deprecated
  public void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderIntendedVehicle(orderRef, vehicleRef);
  }

  @Override
  @Deprecated
  public void setTransportOrderProcessingVehicle(TCSObjectReference<TransportOrder> orderRef,
                                                 TCSObjectReference<Vehicle> vehicleRef,
                                                 List<DriveOrder> driveOrders)
      throws ObjectUnknownException, IllegalArgumentException {
    kernelState.setTransportOrderProcessingVehicle(orderRef, vehicleRef, driveOrders);
  }

  @Override
  @Deprecated
  public void setTransportOrderProcessingVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderProcessingVehicle(orderRef, vehicleRef);
  }

  @Override
  @Deprecated
  public void setTransportOrderFutureDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                                 List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderFutureDriveOrders(orderRef, newOrders);
  }

  @Override
  @Deprecated
  public void setTransportOrderDriveOrders(TCSObjectReference<TransportOrder> orderRef,
                                           List<DriveOrder> newOrders)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderDriveOrders(orderRef, newOrders);
  }

  @Override
  @Deprecated
  public void setTransportOrderInitialDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    LOG.debug("method entry");
    kernelState.setTransportOrderInitialDriveOrder(ref);
  }

  @Override
  @Deprecated
  public void setTransportOrderNextDriveOrder(
      TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, IllegalStateException {
    LOG.debug("method entry");
    kernelState.setTransportOrderNextDriveOrder(ref);
  }

  @Override
  @Deprecated
  public void addTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.addTransportOrderDependency(orderRef, newDepRef);
  }

  @Override
  @Deprecated
  public void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.removeTransportOrderDependency(orderRef, rmDepRef);
  }

  @Override
  @Deprecated
  public void addTransportOrderRejection(
      TCSObjectReference<TransportOrder> orderRef,
      Rejection newRejection)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.addTransportOrderRejection(orderRef, newRejection);
  }

  @Override
  @Deprecated
  public void setTransportOrderWrappingSequence(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderWrappingSequence(orderRef, seqRef);
  }

  @Override
  @Deprecated
  public void setTransportOrderDispensable(
      TCSObjectReference<TransportOrder> orderRef,
      boolean dispensable)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.setTransportOrderDispensable(orderRef, dispensable);
  }

  @Override
  @Deprecated
  public OrderSequence createOrderSequence() {
    LOG.debug("method entry");
    return kernelState.createOrderSequence();
  }

  @Override
  @Deprecated
  public OrderSequence createOrderSequence(OrderSequenceCreationTO to) {
    LOG.debug("method entry");
    return kernelState.createOrderSequence(to);
  }

  @Override
  @Deprecated
  public void addOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                                    TCSObjectReference<TransportOrder> orderRef) {
    LOG.debug("method entry");
    kernelState.addOrderSequenceOrder(seqRef, orderRef);
  }

  @Override
  @Deprecated
  public void removeOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                                       TCSObjectReference<TransportOrder> orderRef) {
    LOG.debug("method entry");
    kernelState.removeOrderSequenceOrder(seqRef, orderRef);
  }

  @Override
  @Deprecated
  public void setOrderSequenceFinishedIndex(
      TCSObjectReference<OrderSequence> seqRef,
      int index) {
    LOG.debug("method entry");
    kernelState.setOrderSequenceFinishedIndex(seqRef, index);
  }

  @Override
  @Deprecated
  public void setOrderSequenceComplete(TCSObjectReference<OrderSequence> seqRef) {
    LOG.debug("method entry");
    kernelState.setOrderSequenceComplete(seqRef);
  }

  @Override
  @Deprecated
  public void setOrderSequenceFinished(TCSObjectReference<OrderSequence> seqRef) {
    LOG.debug("method entry");
    kernelState.setOrderSequenceFinished(seqRef);
  }

  @Override
  @Deprecated
  public void setOrderSequenceFailureFatal(
      TCSObjectReference<OrderSequence> seqRef,
      boolean fatal) {
    LOG.debug("method entry");
    kernelState.setOrderSequenceFailureFatal(seqRef, fatal);
  }

  @Override
  @Deprecated
  public void setOrderSequenceIntendedVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    LOG.debug("method entry");
    kernelState.setOrderSequenceIntendedVehicle(seqRef, vehicleRef);
  }

  @Override
  @Deprecated
  public void setOrderSequenceProcessingVehicle(
      TCSObjectReference<OrderSequence> seqRef,
      TCSObjectReference<Vehicle> vehicleRef) {
    LOG.debug("method entry");
    kernelState.setOrderSequenceProcessingVehicle(seqRef, vehicleRef);
  }

  @Override
  @Deprecated
  public void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                                     boolean immediateAbort,
                                     boolean disableVehicle)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.withdrawTransportOrder(ref, immediateAbort, disableVehicle);
  }

  @Override
  @Deprecated
  public void withdrawTransportOrderByVehicle(TCSObjectReference<Vehicle> vehicleRef,
                                              boolean immediateAbort,
                                              boolean disableVehicle)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    kernelState.withdrawTransportOrderByVehicle(vehicleRef, immediateAbort, disableVehicle);
  }

  @Override
  @Deprecated
  public void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable) {
    LOG.debug("method entry");
    kernelState.dispatchVehicle(vehicleRef, setIdleIfUnavailable);
  }

  @Override
  @Deprecated
  public void releaseVehicle(TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, CredentialsException {
    LOG.debug("method entry");
    kernelState.releaseVehicle(vehicleRef);
  }

  @Override
  @Deprecated
  public void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef,
                                     Object message)
      throws ObjectUnknownException, CredentialsException {
    LOG.debug("method entry");
    kernelState.sendCommAdapterMessage(vehicleRef, message);
  }

  @Override
  @Deprecated
  public void updateRoutingTopology()
      throws CredentialsException {
    LOG.debug("method entry");
    kernelState.updateRoutingTopology();
  }

  @Override
  @Deprecated
  public List<TransportOrder> createTransportOrdersFromScript(String fileName)
      throws ObjectUnknownException, IOException {
    LOG.debug("method entry");
    return kernelState.createTransportOrdersFromScript(fileName);
  }

  @Override
  @Deprecated
  public Set<TCSResource<?>> expandResources(Set<TCSResourceReference<?>> resources)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    return kernelState.expandResources(resources);
  }

  @Override
  @Deprecated
  public <T extends org.opentcs.access.queries.Query<T>> T query(Class<T> clazz) {
    LOG.debug("method entry");
    return kernelState.query(clazz);
  }

  @Override
  @Deprecated
  public double getSimulationTimeFactor() {
    LOG.debug("method entry");
    return kernelState.getSimulationTimeFactor();
  }

  @Override
  @Deprecated
  public void setSimulationTimeFactor(double angle) {
    LOG.debug("method entry");
    kernelState.setSimulationTimeFactor(angle);
  }

  @Override
  @Deprecated
  public Set<org.opentcs.access.ConfigurationItemTO> getConfigurationItems() {
    LOG.debug("method entry");
    return kernelState.getConfigurationItems();
  }

  @Override
  @Deprecated
  public void setConfigurationItem(org.opentcs.access.ConfigurationItemTO itemTO) {
    LOG.debug("method entry");
    kernelState.setConfigurationItem(itemTO);
  }

  @Override
  public void addKernelExtension(final KernelExtension newExtension) {
    LOG.debug("method entry");
    Objects.requireNonNull(newExtension, "newExtension is null");
    kernelExtensions.add(newExtension);
  }

  @Override
  public void removeKernelExtension(final KernelExtension rmExtension) {
    LOG.debug("method entry");
    Objects.requireNonNull(rmExtension, "rmExtension is null");
    kernelExtensions.remove(rmExtension);
  }

  // Event management methods start here.
  @Override
  @Deprecated
  public void addEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener,
      org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> filter) {
    LOG.debug("method entry");
    eventHub.addEventListener(listener, filter);
  }

  @Override
  @Deprecated
  public void addEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener) {
    eventHub.addEventListener(listener);
  }

  @Override
  @Deprecated
  public void removeEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener) {
    LOG.debug("method entry");
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
    // Keep on emitting the old TCSKernelStateEvent until it is actually removed.
    @SuppressWarnings("deprecation")
    org.opentcs.access.TCSKernelStateEvent event
        = new org.opentcs.access.TCSKernelStateEvent(leftState, enteredState, transitionFinished);
    LOG.debug("Emitting kernel state event: {}", event);
    eventHub.processEvent(event);
    eventBus.onEvent(new KernelStateTransitionEvent(leftState, enteredState, transitionFinished));
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
    // Keep on emitting the old TCSKernelStateEvent until it is actually removed.
    @SuppressWarnings("deprecation")
    org.opentcs.access.TCSModelTransitionEvent event
        = new org.opentcs.access.TCSModelTransitionEvent(oldModelName,
                                                         enteredModelName,
                                                         modelContentChanged,
                                                         transitionFinished);
    LOG.debug("Emitting model transition event: {}", event);
    eventHub.processEvent(event);
    eventBus.onEvent(new ModelTransitionEvent(oldModelName,
                                              enteredModelName,
                                              modelContentChanged,
                                              transitionFinished));
  }
}
