/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import java.awt.Color;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel.State;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.rmi.services.RemoteKernelServicePortal;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
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
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Declares the methods provided by an openTCS kernel via RMI.
 * <p>
 * The majority of the methods declared here have signatures analogous to their
 * counterparts in {@link org.opentcs.access.Kernel Kernel}, with an additional
 * {@link ClientID ClientID} parameter which serves the purpose of identifying
 * the calling client and determining its permissions. Except for the methods
 * dealing with user management and event handling, which behave differently,
 * the methods declared here merely check permissions for the respective
 * operations and, if they are granted, pass the given arguments through to the
 * local kernel.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their
 * arguments are not explicitly documented here again. See the corresponding API
 * documentation in {@link org.opentcs.access.Kernel Kernel} for these, instead.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link RemoteKernelServicePortal} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public interface RemoteKernel
    extends Remote {

  /**
   * The default name by which a remote kernel registers itself with a RMI
   * registry.
   */
  String REGISTRATION_NAME = "org.opentcs.remote.rmi.RemoteKernel";
  /**
   * The default/guest user name.
   */
  String GUEST_USER = "Alice";
  /**
   * The default/guest password.
   */
  String GUEST_PASSWORD = "xyz";

  /**
   * Introduce the calling client to the server and authenticate for operations.
   *
   * @param userName The user's name.
   * @param password The user's password.
   * @return An identification object that is required for subsequent method
   * calls.
   * @throws CredentialsException If authentication with the given username and
   * password failed.
   * @throws RemoteException If there was an RMI-related problem.
   * @deprecated Use {@link RemoteKernelServicePortal#login(
   * java.lang.String, java.lang.String, java.util.function.Predicate)} instead.
   */
  @Deprecated
  @CallPermissions({})
  ClientID login(String userName, String password)
      throws CredentialsException, RemoteException;

  /**
   * Logout the client with the given ID from the server.
   * After calling this method, the client with the given ID will be not be
   * allowed to call methods on this <code>RemoteKernel</code> any more.
   *
   * @param clientID The client's ID.
   * @throws RemoteException If there was an RMI-related problem.
   * @deprecated Use {@link RemoteKernelServicePortal#logout(org.opentcs.access.rmi.ClientID)}
   * instead.
   */
  @Deprecated
  @CallPermissions({})
  void logout(ClientID clientID)
      throws RemoteException;

  @Deprecated
  @CallPermissions({})
  Set<org.opentcs.data.user.UserPermission> getUserPermissions(ClientID clientID)
      throws RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MANAGE_USERS})
  void createUser(ClientID clientID, String userName, String userPassword,
                  Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.data.user.UserExistsException, CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({})
  void setUserPassword(ClientID clientID, String userName, String userPassword)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MANAGE_USERS})
  void setUserPermissions(ClientID clientID, String userName,
                          Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MANAGE_USERS})
  void removeUser(ClientID clientID, String userName)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  void setEventFilter(
      ClientID clientID,
      org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> eventFilter)
      throws CredentialsException, RemoteException;

  /**
   * Fetches events buffered for the client.
   *
   * @param clientID The identification object of the client calling the method.
   * @param timeout A timeout (in ms) for which to wait for events to arrive.
   * @return A list of events (in the order they arrived).
   * @throws RemoteException If there was an RMI-related problem.
   * @deprecated Use {@link KernelServicePortal#fetchEvents(long)} instead.
   */
  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  List<org.opentcs.util.eventsystem.TCSEvent> pollEvents(ClientID clientID, long timeout)
      throws CredentialsException, RemoteException;

  /**
   * Returns the current state of the kernel.
   *
   * @param clientID The identification object of the client calling the method.
   * @return The current state of the kernel.
   * @throws CredentialsException If the calling client is not allowed to execute this method.
   * @throws RemoteException If there was an RMI-related problem.
   * @deprecated Use {@link RemoteKernelServicePortal#getState(org.opentcs.access.rmi.ClientID)}
   * instead.
   */
  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  State getState(ClientID clientID)
      throws CredentialsException, RemoteException;

  /**
   * Sets the current state of the kernel.
   *
   * @param clientID The identification object of the client calling the method.
   * @param newState The state the kernel is to be set to.
   * @throws CredentialsException If the calling client is not allowed to execute this method.
   * @throws RemoteException If there was an RMI-related problem.
   * @deprecated Remote clients will not be allowed to set the kernel's state in the future.
   */
  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.CHANGE_KERNEL_STATE})
  void setState(ClientID clientID, State newState)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  String getPersistentModelName(ClientID clientID)
      throws CredentialsException, IOException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  String getLoadedModelName(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void createModel(ClientID clientID, String modelName)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.LOAD_MODEL})
  void loadModel(ClientID clientID)
      throws CredentialsException, IOException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.SAVE_MODEL})
  void saveModel(ClientID clientID, String modelName)
      throws CredentialsException, IOException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.SAVE_MODEL})
  void removeModel(ClientID clientID)
      throws CredentialsException, IOException,
             RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  <T extends TCSObject<T>> T getTCSObject(ClientID clientID,
                                          Class<T> clazz,
                                          TCSObjectReference<T> ref)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  <T extends TCSObject<T>> T getTCSObject(ClientID clientID,
                                          Class<T> clazz,
                                          String name)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  <T extends TCSObject<T>> Set<T> getTCSObjects(ClientID clientID,
                                                Class<T> clazz)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  <T extends TCSObject<T>> Set<T> getTCSObjects(ClientID clientID,
                                                Class<T> clazz,
                                                Pattern regexp)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  <T extends TCSObject<T>> Set<T> getTCSObjects(ClientID clientID,
                                                Class<T> clazz,
                                                Predicate<? super T> predicate)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void renameTCSObject(ClientID clientID,
                       TCSObjectReference<?> ref,
                       String newName)
      throws CredentialsException, ObjectUnknownException,
             ObjectExistsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setTCSObjectProperty(ClientID clientID, TCSObjectReference<?> ref,
                            String key, String value)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void clearTCSObjectProperties(ClientID clientID, TCSObjectReference<?> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void removeTCSObject(ClientID clientID,
                       TCSObjectReference<?> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.PUBLISH_MESSAGES})
  void publishUserNotification(ClientID clientID, UserNotification notification)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  List<UserNotification> getUserNotifications(ClientID clientID,
                                              Predicate<UserNotification> predicate)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void createPlantModel(ClientID clientID, PlantModelCreationTO to)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  VisualLayout createVisualLayout(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVisualLayoutScaleX(ClientID clientID,
                             TCSObjectReference<VisualLayout> ref,
                             double scaleX)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVisualLayoutScaleY(ClientID clientID,
                             TCSObjectReference<VisualLayout> ref,
                             double scaleY)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVisualLayoutColors(ClientID clientID,
                             TCSObjectReference<VisualLayout> ref,
                             Map<String, Color> colors)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVisualLayoutElements(ClientID clientID,
                               TCSObjectReference<VisualLayout> ref,
                               Set<LayoutElement> elements)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVisualLayoutViewBookmarks(
      ClientID clientID,
      TCSObjectReference<VisualLayout> ref,
      List<org.opentcs.data.model.visualization.ViewBookmark> bookmarks)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  Point createPoint(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPointPosition(ClientID clientID,
                        TCSObjectReference<Point> ref,
                        Triple position)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPointVehicleOrientationAngle(ClientID clientID,
                                       TCSObjectReference<Point> ref,
                                       double angle)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPointType(ClientID clientID, TCSObjectReference<Point> ref,
                    Point.Type newType)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  Path createPath(ClientID clientID, TCSObjectReference<Point> srcRef,
                  TCSObjectReference<Point> destRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPathLength(ClientID clientID, TCSObjectReference<Path> ref,
                     long length)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPathRoutingCost(ClientID clientID, TCSObjectReference<Path> ref,
                          long cost)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPathMaxVelocity(ClientID clientID, TCSObjectReference<Path> ref,
                          int velocity)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPathMaxReverseVelocity(ClientID clientID,
                                 TCSObjectReference<Path> ref, int velocity)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setPathLocked(ClientID clientID, TCSObjectReference<Path> ref,
                     boolean locked)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  Vehicle createVehicle(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVehicleEnergyLevelCritical(ClientID clientID,
                                     TCSObjectReference<Vehicle> ref,
                                     int energyLevel)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVehicleEnergyLevelGood(ClientID clientID,
                                 TCSObjectReference<Vehicle> ref,
                                 int energyLevel)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVehicleLength(ClientID clientID, TCSObjectReference<Vehicle> ref,
                        int length)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setVehicleProcessableCategories(ClientID clientID, TCSObjectReference<Vehicle> ref,
                                       Set<String> processableCategories)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  LocationType createLocationType(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void addLocationTypeAllowedOperation(ClientID clientID,
                                       TCSObjectReference<LocationType> ref,
                                       String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void removeLocationTypeAllowedOperation(ClientID clientID,
                                          TCSObjectReference<LocationType> ref,
                                          String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  Location createLocation(ClientID clientID,
                          TCSObjectReference<LocationType> typeRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setLocationPosition(ClientID clientID,
                           TCSObjectReference<Location> ref,
                           Triple position)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void setLocationType(ClientID clientID,
                       TCSObjectReference<Location> ref,
                       TCSObjectReference<LocationType> typeRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void connectLocationToPoint(ClientID clientID,
                              TCSObjectReference<Location> locRef,
                              TCSObjectReference<Point> pointRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void disconnectLocationFromPoint(ClientID clientID,
                                   TCSObjectReference<Location> locRef,
                                   TCSObjectReference<Point> pointRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void addLocationLinkAllowedOperation(ClientID clientID,
                                       TCSObjectReference<Location> locRef,
                                       TCSObjectReference<Point> pointRef,
                                       String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void removeLocationLinkAllowedOperation(ClientID clientID,
                                          TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef,
                                          String operation)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void clearLocationLinkAllowedOperations(ClientID clientID,
                                          TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  Block createBlock(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void addBlockMember(ClientID clientID, TCSObjectReference<Block> ref,
                      TCSResourceReference<?> newMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void removeBlockMember(ClientID clientID, TCSObjectReference<Block> ref,
                         TCSResourceReference<?> rmMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  Group createGroup(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void addGroupMember(ClientID clientID, TCSObjectReference<Group> ref,
                      TCSObjectReference<?> newMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void removeGroupMember(ClientID clientID, TCSObjectReference<Group> ref,
                         TCSObjectReference<?> rmMemberRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  org.opentcs.data.model.StaticRoute createStaticRoute(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void addStaticRouteHop(ClientID clientID,
                         TCSObjectReference<org.opentcs.data.model.StaticRoute> ref,
                         TCSObjectReference<Point> newHopRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void clearStaticRouteHops(ClientID clientID,
                            TCSObjectReference<org.opentcs.data.model.StaticRoute> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  TransportOrder createTransportOrder(ClientID clientID,
                                      List<Destination> destinations)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  TransportOrder createTransportOrder(ClientID clientID, TransportOrderCreationTO to)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void setTransportOrderDeadline(ClientID clientID,
                                 TCSObjectReference<TransportOrder> ref, long deadline)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void activateTransportOrder(ClientID clientID,
                              TCSObjectReference<TransportOrder> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void setTransportOrderIntendedVehicle(ClientID clientID,
                                        TCSObjectReference<TransportOrder> orderRef,
                                        TCSObjectReference<Vehicle> vehicleRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void setTransportOrderFutureDriveOrders(ClientID clientID,
                                          TCSObjectReference<TransportOrder> orderRef,
                                          List<DriveOrder> newOrders)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void addTransportOrderDependency(ClientID clientID,
                                   TCSObjectReference<TransportOrder> orderRef,
                                   TCSObjectReference<TransportOrder> newDepRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void removeTransportOrderDependency(ClientID clientID,
                                      TCSObjectReference<TransportOrder> orderRef,
                                      TCSObjectReference<TransportOrder> rmDepRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  OrderSequence createOrderSequence(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  OrderSequence createOrderSequence(ClientID clientID, OrderSequenceCreationTO to)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void addOrderSequenceOrder(ClientID clientID,
                             TCSObjectReference<OrderSequence> seqRef,
                             TCSObjectReference<TransportOrder> orderRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void removeOrderSequenceOrder(ClientID clientID,
                                TCSObjectReference<OrderSequence> seqRef,
                                TCSObjectReference<TransportOrder> orderRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void setOrderSequenceComplete(ClientID clientID,
                                TCSObjectReference<OrderSequence> ref)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void setOrderSequenceFailureFatal(ClientID clientID,
                                    TCSObjectReference<OrderSequence> ref,
                                    boolean fatal)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void setOrderSequenceIntendedVehicle(ClientID clientID,
                                       TCSObjectReference<OrderSequence> seqRef,
                                       TCSObjectReference<Vehicle> vehicleRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  void withdrawTransportOrder(ClientID clientID,
                              TCSObjectReference<TransportOrder> ref,
                              boolean immediateAbort,
                              boolean disableVehicle)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_VEHICLES})
  void withdrawTransportOrderByVehicle(ClientID clientID,
                                       TCSObjectReference<Vehicle> vehicleRef,
                                       boolean immediateAbort,
                                       boolean disableVehicle)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_VEHICLES})
  void dispatchVehicle(ClientID clientID,
                       TCSObjectReference<Vehicle> vehicleRef,
                       boolean setIdleIfUnavailable)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_VEHICLES})
  void releaseVehicle(ClientID clientID,
                      TCSObjectReference<Vehicle> vehicleRef)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_VEHICLES})
  void sendCommAdapterMessage(ClientID clientID,
                              TCSObjectReference<Vehicle> vehicleRef,
                              Object message)
      throws CredentialsException, ObjectUnknownException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_ORDER})
  List<TransportOrder> createTransportOrdersFromScript(ClientID clientID,
                                                       String fileName)
      throws CredentialsException, ObjectUnknownException, IOException,
             RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.MODIFY_MODEL})
  void updateRoutingTopology(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  <T extends org.opentcs.access.queries.Query<T>> T query(ClientID clientID, Class<T> clazz)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  double getSimulationTimeFactor(ClientID clientID)
      throws CredentialsException, RemoteException;

  @Deprecated
  @CallPermissions({org.opentcs.data.user.UserPermission.CHANGE_CONFIGURATION})
  void setSimulationTimeFactor(ClientID clientID, double factor)
      throws CredentialsException, RemoteException;

  @CallPermissions({org.opentcs.data.user.UserPermission.READ_DATA})
  List<org.opentcs.access.TravelCosts> getTravelCosts(ClientID clientID,
                                                      TCSObjectReference<Vehicle> vRef,
                                                      TCSObjectReference<Location> srcRef,
                                                      Set<TCSObjectReference<Location>> destRefs)
      throws CredentialsException, RemoteException;
}
