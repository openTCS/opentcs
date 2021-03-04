/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.awt.Color;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.services.NotificationService;
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
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.rmi.UserManager.ClientEntry;
import org.opentcs.kernel.util.RegistryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link org.opentcs.access.rmi.RemoteKernel}
 * interface.
 * It is basically a wrapper object around an openTCS kernel instance that is
 * exported via RMI and adds some checks of the client's credentials before
 * allowing a method call to be passed through to the actual kernel.
 * <p>
 * Upon creation, an instance of this class registers itself with the RMI
 * registry by the name declared as {@link org.opentcs.access.rmi.RemoteKernel#REGISTRATION_NAME}.
 * </p>
 *
 * <hr>
 *
 * <h4>Configuration entries</h4>
 * <dl>
 * <dt><b>clientSweepInterval:</b></dt>
 * <dd>The interval for cleaning out inactive clients (in ms), defaulting to
 * five minutes.</dd>
 * </dl>
 * <hr>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class StandardRemoteKernel
    implements org.opentcs.access.rmi.RemoteKernel,
               KernelExtension {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardRemoteKernel.class);
  /**
   * The local kernel implementing the actual functionality.
   */
  private final Kernel localKernel;
  /**
   * The notification service.
   */
  private final NotificationService notificationService;
  /**
   * The user manager.
   */
  private final UserManager userManager;
  /**
   * Provides configuration data.
   */
  private final RmiKernelInterfaceConfiguration configuration;
  /**
   * Provides socket factories used for RMI.
   */
  private final SocketFactoryProvider socketFactoryProvider;
  /**
   * Provides the registry with which this <code>RemoteKernel</code> registers.
   */
  private final RegistryProvider registryProvider;
  /**
   * The registry with which this <code>RemoteKernel</code> registers.
   */
  private Registry rmiRegistry;
  /**
   * This kernel extension's <em>enabled</em> flag.
   */
  private boolean enabled;

  /**
   * Creates and registers a new RMI object for a locally running kernel.
   *
   * @param kernel The local kernel.
   * @param userManager The user manager.
   * @param configuration This class' configuration.
   * @param socketFactoryProvider The socket factory provider used for RMI.
   * @param registryProvider Provides the registry with which this <code>RemoteKernel</code>
   * registers.
   */
  @Inject
  StandardRemoteKernel(LocalKernel kernel,
                       NotificationService notificationService,
                       UserManager userManager,
                       RmiKernelInterfaceConfiguration configuration,
                       SocketFactoryProvider socketFactoryProvider,
                       RegistryProvider registryProvider) {
    this.localKernel = requireNonNull(kernel, "kernel");
    this.notificationService = requireNonNull(notificationService, "notificationService");
    this.userManager = requireNonNull(userManager, "userManager");
    this.configuration = requireNonNull(configuration, "configuration");
    this.socketFactoryProvider = requireNonNull(socketFactoryProvider, "socketFactoryProvider");
    this.registryProvider = requireNonNull(registryProvider, "registryProvider");
  }

  // Implementation of interface KernelExtension starts here.
  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    registryProvider.initialize();
    userManager.initialize();

    rmiRegistry = registryProvider.get();
    // Export this instance via RMI.
    try {
      LOG.debug("Exporting proxy...");
      UnicastRemoteObject.exportObject(this,
                                       configuration.remoteKernelPort(),
                                       socketFactoryProvider.getClientSocketFactory(),
                                       socketFactoryProvider.getServerSocketFactory());
      LOG.debug("Binding instance with RMI registry...");
      rmiRegistry.rebind(org.opentcs.access.rmi.RemoteKernel.REGISTRATION_NAME, this);
      LOG.debug("Bound instance {} with registry {}.", rmiRegistry.list(), rmiRegistry);
    }
    catch (RemoteException exc) {
      LOG.error("Could not export or bind with RMI registry", exc);
    }
    enabled = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    try {
      LOG.debug("Unbinding from RMI registry...");
      rmiRegistry.unbind(org.opentcs.access.rmi.RemoteKernel.REGISTRATION_NAME);
      LOG.debug("Unexporting RMI interface...");
      UnicastRemoteObject.unexportObject(this, true);
    }
    catch (RemoteException | NotBoundException exc) {
      LOG.warn("Exception shutting down RMI interface", exc);
    }
    userManager.terminate();
    registryProvider.terminate();
    enabled = false;
  }

  @Override
  public boolean isInitialized() {
    return enabled;
  }

  // Implementation of RemoteKernel start here.
  @Deprecated
  @Override
  public ClientID login(String userName, String password)
      throws CredentialsException {
    requireNonNull(userName, "userName");
    requireNonNull(password, "password");
    synchronized (userManager.getKnownClients()) {
      UserAccount account = userManager.getUser(userName);
      if (account == null || !account.getPassword().equals(password)) {
        LOG.info("Authentication failed for user {}.", userName);
        throw new CredentialsException("Authentication failed for user " + userName);
      }
      // Generate a new ID for the client.
      ClientID clientId = new ClientID(userName);
      // Add an entry for the newly connected client.
      ClientEntry clientEntry = new ClientEntry(userName, account.getPermissions());
      userManager.getKnownClients().put(clientId, clientEntry);
      LOG.debug("New client named {} logged in", clientId.getClientName());
      return clientId;
    }
  }

  @Deprecated
  @Override
  public void logout(ClientID clientID) {
    requireNonNull("clientID");
    // Forget the client so it won't be able to call methods on this kernel and
    // won't receive events any more.
    synchronized (userManager.getKnownClients()) {
      userManager.getKnownClients().remove(clientID);
    }
  }

  @Deprecated
  @Override
  public Set<org.opentcs.data.user.UserPermission> getUserPermissions(ClientID clientID) {
    requireNonNull(clientID, "clientID");

    Set<org.opentcs.data.user.UserPermission> result;
    synchronized (userManager.getKnownClients()) {
      ClientEntry clientEntry = userManager.getClient(clientID);
      if (clientEntry != null) {
        // Set the 'alive' flag for the cleaning thread.
        clientEntry.setAlive(true);
        //result = clientEntry.getPermissions();
        // XXX This is not correct, but since this method is deprecated and not used anywhere, ...
        result = EnumSet.noneOf(org.opentcs.data.user.UserPermission.class);
      }
      else {
        result = EnumSet.noneOf(org.opentcs.data.user.UserPermission.class);
      }
    }
    return result;
  }

  @Deprecated
  @Override
  public void createUser(ClientID clientID,
                         String userName,
                         String userPassword,
                         Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.data.user.UserExistsException, CredentialsException {
    requireNonNull(userName, "userName");
    requireNonNull(userPassword, "userPassword");
    requireNonNull(userPermissions, "userPermissions");
    checkCredentialsForRole(clientID, UserPermission.MANAGE_USERS);

    // Check if a user with the given name already exists.
    UserAccount account = userManager.getUser(userName);
    if (account != null) {
      LOG.warn("attempt to create existing user '{}'", userName);
      throw new org.opentcs.data.user.UserExistsException("user exists: '" + userName + "'");
    }
    // XXX This is not correct, but since this method is deprecated and not used anywhere, ...
    account = new UserAccount(userName, userPassword, EnumSet.noneOf(UserPermission.class));
    userManager.getKnownUsers().put(userName, account);
  }

  @Deprecated
  @Override
  public void setUserPassword(ClientID clientID,
                              String userName,
                              String userPassword)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException {
    // Check if it's the user himself changing his own password.
    boolean userChangesOwnPass;
    synchronized (userManager.getKnownClients()) {
      ClientEntry clientEntry = userManager.getClient(clientID);
      userChangesOwnPass = userName.equals(clientEntry.getUserName());
    }
    // If the user is not changing his own password, check for permissions.
    if (!userChangesOwnPass) {
      LOG.debug("user changes foreign password, checking permissions");
      checkCredentialsForRole(clientID, UserPermission.MANAGE_USERS);
    }
    // Check if a user with the given name really exists.
    UserAccount account = userManager.getUser(userName);
    if (account == null) {
      LOG.warn("unknown user: {}", userName);
      throw new org.opentcs.data.user.UserUnknownException("unknown user: '" + userName + "'");
    }
    account.setPassword(userPassword);
  }

  @Deprecated
  @Override
  public void setUserPermissions(ClientID clientID,
                                 String userName,
                                 Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException {
    // Check if a user with the given name really exists.
    UserAccount account = userManager.getUser(userName);
    if (account == null) {
      LOG.warn("unknown user: {}", userName);
      throw new org.opentcs.data.user.UserUnknownException("unknown user: '" + userName + "'");
    }
    // XXX This is not correct, but since this method is deprecated and not used anywhere, ...
    account.setPermissions(EnumSet.noneOf(UserPermission.class));
  }

  @Deprecated
  @Override
  public void removeUser(ClientID clientID, String userName)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException {
    checkCredentialsForRole(clientID, UserPermission.MANAGE_USERS);

    UserAccount account = userManager.getKnownUsers().remove(userName);
    if (account == null) {
      LOG.warn("unknown user: {}", userName);
      throw new org.opentcs.data.user.UserUnknownException("unknown user: '" + userName + "'");
    }
  }

  @Deprecated
  @Override
  public void setEventFilter(
      ClientID clientID,
      org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> eventFilter)
      throws CredentialsException {
    requireNonNull(eventFilter, "eventFilter");
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    synchronized (userManager.getKnownClients()) {
      userManager.getClient(clientID).getEventBuffer().setFilter(eventFilter);
    }
  }

  @Deprecated
  @Override
  public List<org.opentcs.util.eventsystem.TCSEvent> pollEvents(ClientID clientID, long timeout)
      throws CredentialsException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    List<org.opentcs.util.eventsystem.TCSEvent> result = new LinkedList<>();
    for (Object event : userManager.pollEvents(clientID, timeout)) {
      if (event instanceof org.opentcs.util.eventsystem.TCSEvent) {
        result.add((org.opentcs.util.eventsystem.TCSEvent) event);
      }
    }
    return result;
  }

  @Deprecated
  @Override
  public Kernel.State getState(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getState();
  }

  @Deprecated
  @Override
  public void setState(ClientID clientID, Kernel.State newState)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.CHANGE_KERNEL_STATE);

    localKernel.setState(newState);
  }

  @Deprecated
  @Override
  public String getPersistentModelName(ClientID clientID)
      throws CredentialsException, IOException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getPersistentModelName();
  }

  @Deprecated
  @Override
  public String getLoadedModelName(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getLoadedModelName();
  }

  @Deprecated
  @Override
  public void createModel(ClientID clientID, String modelName)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.createModel(modelName);
  }

  @Deprecated
  @Override
  public void loadModel(ClientID clientID)
      throws CredentialsException, IOException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.LOAD_MODEL);

    localKernel.loadModel();
  }

  @Deprecated
  @Override
  public void saveModel(ClientID clientID, String modelName)
      throws CredentialsException, IOException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.SAVE_MODEL);

    localKernel.saveModel(modelName);
  }

  @Deprecated
  @Override
  public void removeModel(ClientID clientID)
      throws CredentialsException, IOException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.removeModel();
  }

  @Deprecated
  @Override
  public <T extends TCSObject<T>> T getTCSObject(ClientID clientID, Class<T> clazz,
                                                 TCSObjectReference<T> ref)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getTCSObject(clazz, ref);
  }

  @Deprecated
  @Override
  public <T extends TCSObject<T>> T getTCSObject(ClientID clientID, Class<T> clazz, String name)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getTCSObject(clazz, name);
  }

  @Deprecated
  @Override
  public <T extends TCSObject<T>> Set<T> getTCSObjects(ClientID clientID, Class<T> clazz)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getTCSObjects(clazz);
  }

  @Deprecated
  @Override
  public <T extends TCSObject<T>> Set<T> getTCSObjects(ClientID clientID, Class<T> clazz,
                                                       Pattern regexp)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getTCSObjects(clazz, regexp);
  }

  @Deprecated
  @Override
  public <T extends TCSObject<T>> Set<T> getTCSObjects(ClientID clientID, Class<T> clazz,
                                                       Predicate<? super T> predicate)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getTCSObjects(clazz, predicate);
  }

  @Deprecated
  @Override
  public void renameTCSObject(ClientID clientID,
                              TCSObjectReference<?> ref, String newName)
      throws CredentialsException, ObjectUnknownException, ObjectExistsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.renameTCSObject(ref, newName);
  }

  @Deprecated
  @Override
  public void setTCSObjectProperty(ClientID clientID,
                                   TCSObjectReference<?> ref, String key, String value)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setTCSObjectProperty(ref, key, value);
  }

  @Deprecated
  @Override
  public void clearTCSObjectProperties(ClientID clientID,
                                       TCSObjectReference<?> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.clearTCSObjectProperties(ref);
  }

  @Deprecated
  @Override
  public void removeTCSObject(ClientID clientID,
                              TCSObjectReference<?> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.removeTCSObject(ref);
  }

  @Deprecated
  @Override
  public void publishUserNotification(ClientID clientID, UserNotification notification)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.PUBLISH_MESSAGES);

    localKernel.publishUserNotification(notification);
  }

  @Deprecated
  @Override
  public List<UserNotification> getUserNotifications(ClientID clientID,
                                                     Predicate<UserNotification> predicate)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getUserNotifications(predicate);
  }

  @Deprecated
  @Override
  public void createPlantModel(ClientID clientID, PlantModelCreationTO to)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.createPlantModel(to);
  }

  @Deprecated
  @Override
  public VisualLayout createVisualLayout(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createVisualLayout();
  }

  @Deprecated
  @Override
  public void setVisualLayoutScaleX(ClientID clientID, TCSObjectReference<VisualLayout> ref,
                                    double scaleX)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVisualLayoutScaleX(ref, scaleX);
  }

  @Deprecated
  @Override
  public void setVisualLayoutScaleY(ClientID clientID, TCSObjectReference<VisualLayout> ref,
                                    double scaleY)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVisualLayoutScaleY(ref, scaleY);
  }

  @Deprecated
  @Override
  public void setVisualLayoutColors(ClientID clientID, TCSObjectReference<VisualLayout> ref,
                                    Map<String, Color> colors)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVisualLayoutColors(ref, colors);
  }

  @Deprecated
  @Override
  public void setVisualLayoutElements(ClientID clientID, TCSObjectReference<VisualLayout> ref,
                                      Set<LayoutElement> elements)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVisualLayoutElements(ref, elements);
  }

  @Deprecated
  @Override
  public void setVisualLayoutViewBookmarks(
      ClientID clientID,
      TCSObjectReference<VisualLayout> ref,
      List<org.opentcs.data.model.visualization.ViewBookmark> bookmarks)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVisualLayoutViewBookmarks(ref, bookmarks);
  }

  @Deprecated
  @Override
  public Point createPoint(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createPoint();
  }

  @Deprecated
  @Override
  public void setPointPosition(ClientID clientID, TCSObjectReference<Point> ref, Triple position)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPointPosition(ref, position);
  }

  @Deprecated
  @Override
  public void setPointVehicleOrientationAngle(ClientID clientID, TCSObjectReference<Point> ref,
                                              double angle)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPointVehicleOrientationAngle(ref, angle);
  }

  @Deprecated
  @Override
  public void setPointType(ClientID clientID, TCSObjectReference<Point> ref, Point.Type newType)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPointType(ref, newType);
  }

  @Deprecated
  @Override
  public Path createPath(ClientID clientID, TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createPath(srcRef, destRef);
  }

  @Deprecated
  @Override
  public void setPathLength(ClientID clientID, TCSObjectReference<Path> ref, long length)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPathLength(ref, length);
  }

  @Deprecated
  @Override
  public void setPathRoutingCost(ClientID clientID, TCSObjectReference<Path> ref, long cost)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPathRoutingCost(ref, cost);
  }

  @Deprecated
  @Override
  public void setPathMaxVelocity(ClientID clientID, TCSObjectReference<Path> ref, int velocity)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPathMaxVelocity(ref, velocity);
  }

  @Deprecated
  @Override
  public void setPathMaxReverseVelocity(ClientID clientID, TCSObjectReference<Path> ref,
                                        int velocity)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPathMaxReverseVelocity(ref, velocity);
  }

  @Deprecated
  @Override
  public void setPathLocked(ClientID clientID, TCSObjectReference<Path> ref, boolean locked)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setPathLocked(ref, locked);
  }

  @Deprecated
  @Override
  public Vehicle createVehicle(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createVehicle();
  }

  @Deprecated
  @Override
  public void setVehicleEnergyLevelCritical(ClientID clientID, TCSObjectReference<Vehicle> ref,
                                            int energyLevel)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVehicleEnergyLevelCritical(ref, energyLevel);
  }

  @Deprecated
  @Override
  public void setVehicleEnergyLevelGood(ClientID clientID, TCSObjectReference<Vehicle> ref,
                                        int energyLevel)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVehicleEnergyLevelGood(ref, energyLevel);
  }

  @Deprecated
  @Override
  public void setVehicleLength(ClientID clientID, TCSObjectReference<Vehicle> ref, int length)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVehicleLength(ref, length);
  }

  @Deprecated
  @Override
  public void setVehicleProcessableCategories(ClientID clientID, TCSObjectReference<Vehicle> ref,
                                              Set<String> processableCategories)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setVehicleProcessableCategories(ref, processableCategories);
  }

  @Deprecated
  @Override
  public LocationType createLocationType(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createLocationType();
  }

  @Deprecated
  @Override
  public void addLocationTypeAllowedOperation(ClientID clientID,
                                              TCSObjectReference<LocationType> ref,
                                              String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.addLocationTypeAllowedOperation(ref, operation);
  }

  @Deprecated
  @Override
  public void removeLocationTypeAllowedOperation(ClientID clientID,
                                                 TCSObjectReference<LocationType> ref,
                                                 String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.removeLocationTypeAllowedOperation(ref, operation);
  }

  @Deprecated
  @Override
  public Location createLocation(ClientID clientID, TCSObjectReference<LocationType> typeRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createLocation(typeRef);
  }

  @Deprecated
  @Override
  public void setLocationPosition(ClientID clientID, TCSObjectReference<Location> ref,
                                  Triple position)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setLocationPosition(ref, position);
  }

  @Deprecated
  @Override
  public void setLocationType(ClientID clientID, TCSObjectReference<Location> ref,
                              TCSObjectReference<LocationType> typeRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.setLocationType(ref, typeRef);
  }

  @Deprecated
  @Override
  public void connectLocationToPoint(ClientID clientID, TCSObjectReference<Location> locRef,
                                     TCSObjectReference<Point> pointRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.connectLocationToPoint(locRef, pointRef);
  }

  @Deprecated
  @Override
  public void disconnectLocationFromPoint(ClientID clientID, TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.disconnectLocationFromPoint(locRef, pointRef);
  }

  @Deprecated
  @Override
  public void addLocationLinkAllowedOperation(ClientID clientID,
                                              TCSObjectReference<Location> locRef,
                                              TCSObjectReference<Point> pointRef,
                                              String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.addLocationLinkAllowedOperation(locRef, pointRef, operation);
  }

  @Deprecated
  @Override
  public void removeLocationLinkAllowedOperation(ClientID clientID,
                                                 TCSObjectReference<Location> locRef,
                                                 TCSObjectReference<Point> pointRef,
                                                 String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.removeLocationLinkAllowedOperation(locRef, pointRef, operation);
  }

  @Deprecated
  @Override
  public void clearLocationLinkAllowedOperations(ClientID clientID,
                                                 TCSObjectReference<Location> locRef,
                                                 TCSObjectReference<Point> pointRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.clearLocationLinkAllowedOperations(locRef, pointRef);
  }

  @Deprecated
  @Override
  public Block createBlock(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createBlock();
  }

  @Deprecated
  @Override
  public void addBlockMember(ClientID clientID, TCSObjectReference<Block> ref,
                             TCSResourceReference<?> newMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.addBlockMember(ref, newMemberRef);
  }

  @Deprecated
  @Override
  public void removeBlockMember(ClientID clientID, TCSObjectReference<Block> ref,
                                TCSResourceReference<?> rmMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.removeBlockMember(ref, rmMemberRef);
  }

  @Deprecated
  @Override
  public Group createGroup(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createGroup();
  }

  @Deprecated
  @Override
  public void addGroupMember(ClientID clientID, TCSObjectReference<Group> ref,
                             TCSObjectReference<?> newMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.addGroupMember(ref, newMemberRef);
  }

  @Deprecated
  @Override
  public void removeGroupMember(ClientID clientID, TCSObjectReference<Group> ref,
                                TCSObjectReference<?> rmMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.removeGroupMember(ref, rmMemberRef);
  }

  @Deprecated
  @Override
  public org.opentcs.data.model.StaticRoute createStaticRoute(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    return localKernel.createStaticRoute();
  }

  @Deprecated
  @Override
  public void addStaticRouteHop(ClientID clientID,
                                TCSObjectReference<org.opentcs.data.model.StaticRoute> ref,
                                TCSObjectReference<Point> newHopRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.addStaticRouteHop(ref, newHopRef);
  }

  @Deprecated
  @Override
  public void clearStaticRouteHops(ClientID clientID,
                                   TCSObjectReference<org.opentcs.data.model.StaticRoute> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.clearStaticRouteHops(ref);
  }

  @Deprecated
  @Override
  public TransportOrder createTransportOrder(ClientID clientID,
                                             List<DriveOrder.Destination> destinations)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    return localKernel.createTransportOrder(destinations);
  }

  @Deprecated
  @Override
  public TransportOrder createTransportOrder(ClientID clientID, TransportOrderCreationTO to)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    return localKernel.createTransportOrder(to);
  }

  @Deprecated
  @Override
  public void setTransportOrderDeadline(ClientID clientID, TCSObjectReference<TransportOrder> ref,
                                        long deadline)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.setTransportOrderDeadline(ref, deadline);
  }

  @Deprecated
  @Override
  public void activateTransportOrder(ClientID clientID, TCSObjectReference<TransportOrder> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.activateTransportOrder(ref);
  }

  @Deprecated
  @Override
  public void setTransportOrderIntendedVehicle(ClientID clientID,
                                               TCSObjectReference<TransportOrder> orderRef,
                                               TCSObjectReference<Vehicle> vehicleRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.setTransportOrderIntendedVehicle(orderRef, vehicleRef);
  }

  @Deprecated
  @Override
  public void setTransportOrderFutureDriveOrders(ClientID clientID,
                                                 TCSObjectReference<TransportOrder> orderRef,
                                                 List<DriveOrder> newOrders)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.setTransportOrderFutureDriveOrders(orderRef, newOrders);
  }

  @Deprecated
  @Override
  public void addTransportOrderDependency(ClientID clientID,
                                          TCSObjectReference<TransportOrder> orderRef,
                                          TCSObjectReference<TransportOrder> newDepRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.addTransportOrderDependency(orderRef, newDepRef);
  }

  @Deprecated
  @Override
  public void removeTransportOrderDependency(ClientID clientID,
                                             TCSObjectReference<TransportOrder> orderRef,
                                             TCSObjectReference<TransportOrder> rmDepRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.removeTransportOrderDependency(orderRef, rmDepRef);
  }

  @Deprecated
  @Override
  public OrderSequence createOrderSequence(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    return localKernel.createOrderSequence();
  }

  @Deprecated
  @Override
  public OrderSequence createOrderSequence(ClientID clientID, OrderSequenceCreationTO to)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    return localKernel.createOrderSequence(to);
  }

  @Deprecated
  @Override
  public void addOrderSequenceOrder(ClientID clientID, TCSObjectReference<OrderSequence> seqRef,
                                    TCSObjectReference<TransportOrder> orderRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.addOrderSequenceOrder(seqRef, orderRef);
  }

  @Deprecated
  @Override
  public void removeOrderSequenceOrder(ClientID clientID, TCSObjectReference<OrderSequence> seqRef,
                                       TCSObjectReference<TransportOrder> orderRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.removeOrderSequenceOrder(seqRef, orderRef);
  }

  @Deprecated
  @Override
  public void setOrderSequenceComplete(ClientID clientID, TCSObjectReference<OrderSequence> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.setOrderSequenceComplete(ref);
  }

  @Deprecated
  @Override
  public void setOrderSequenceFailureFatal(ClientID clientID, TCSObjectReference<OrderSequence> ref,
                                           boolean fatal)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.setOrderSequenceFailureFatal(ref, fatal);
  }

  @Deprecated
  @Override
  public void setOrderSequenceIntendedVehicle(ClientID clientID,
                                              TCSObjectReference<OrderSequence> seqRef,
                                              TCSObjectReference<Vehicle> vehicleRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.setOrderSequenceIntendedVehicle(seqRef, vehicleRef);
  }

  @Deprecated
  @Override
  public void withdrawTransportOrder(ClientID clientID, TCSObjectReference<TransportOrder> ref,
                                     boolean immediateAbort, boolean disableVehicle)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.withdrawTransportOrder(ref, immediateAbort, disableVehicle);
  }

  @Deprecated
  @Override
  public void withdrawTransportOrderByVehicle(ClientID clientID,
                                              TCSObjectReference<Vehicle> vehicleRef,
                                              boolean immediateAbort, boolean disableVehicle)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    localKernel.withdrawTransportOrderByVehicle(vehicleRef, immediateAbort, disableVehicle);
  }

  @Deprecated
  @Override
  public void dispatchVehicle(ClientID clientID, TCSObjectReference<Vehicle> vehicleRef,
                              boolean setIdleIfUnavailable)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_VEHICLES);

    localKernel.dispatchVehicle(vehicleRef, setIdleIfUnavailable);
  }

  @Deprecated
  @Override
  public void releaseVehicle(ClientID clientID, TCSObjectReference<Vehicle> vehicleRef)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_VEHICLES);

    localKernel.releaseVehicle(vehicleRef);
  }

  @Deprecated
  @Override
  public void sendCommAdapterMessage(ClientID clientID, TCSObjectReference<Vehicle> vehicleRef,
                                     Object message)
      throws CredentialsException, ObjectUnknownException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_VEHICLES);

    localKernel.sendCommAdapterMessage(vehicleRef, message);
  }

  @Deprecated
  @Override
  public List<TransportOrder> createTransportOrdersFromScript(ClientID clientID, String fileName)
      throws CredentialsException, ObjectUnknownException, IOException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_ORDER);

    return localKernel.createTransportOrdersFromScript(fileName);
  }

  @Deprecated
  @Override
  public void updateRoutingTopology(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.MODIFY_MODEL);

    localKernel.updateRoutingTopology();
  }

  @Deprecated
  @Override
  public <T extends org.opentcs.access.queries.Query<T>> T query(ClientID clientID, Class<T> clazz)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.query(clazz);
  }

  @Deprecated
  @Override
  public double getSimulationTimeFactor(ClientID clientID)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getSimulationTimeFactor();
  }

  @Deprecated
  @Override
  public void setSimulationTimeFactor(ClientID clientID, double factor)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.CHANGE_CONFIGURATION);

    localKernel.setSimulationTimeFactor(factor);
  }

  @Override
  @Deprecated
  public List<org.opentcs.access.TravelCosts> getTravelCosts(
      ClientID clientID,
      TCSObjectReference<Vehicle> vRef,
      TCSObjectReference<Location> srcRef,
      Set<TCSObjectReference<Location>> destRefs)
      throws CredentialsException, RemoteException {
    checkCredentialsForRole(clientID, UserPermission.READ_DATA);

    return localKernel.getTravelCosts(vRef, srcRef, destRefs);
  }

  // Private methods start here.
  private void checkCredentialsForRole(ClientID clientID, UserPermission requiredPermission)
      throws CredentialsException {
    userManager.verifyCredentials(clientID, requiredPermission);
  }
}
