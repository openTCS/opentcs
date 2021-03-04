/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.order.OrderSequenceCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.SchedulerService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
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
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Declares the methods an openTCS kernel implements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public interface Kernel
    extends org.opentcs.util.eventsystem.EventSource<org.opentcs.util.eventsystem.TCSEvent> {

  /**
   * The default name used for the empty model created on startup.
   */
  String DEFAULT_MODEL_NAME = "unnamed";

  /**
   * Returns the permissions the calling client is granted.
   *
   * @return The permissions the calling client is granted.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated User management should be strictly configuration-based and will not be supported by
   * kernel interaction in the future.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  Set<org.opentcs.data.user.UserPermission> getUserPermissions()
      throws CredentialsException;

  /**
   * Creates a new user account.
   *
   * @param userName The new user's name.
   * @param userPassword The new user's password.
   * @param userPermissions The new user's permissions.
   * @throws org.opentcs.data.user.UserExistsException If a user with the given name exists already.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated User management should be strictly configuration-based and will not be supported by
   * kernel interaction in the future.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  void createUser(String userName, String userPassword,
                  Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.data.user.UserExistsException, CredentialsException;

  /**
   * Changes a user's password.
   *
   * @param userName The name of the user for which the password is to be
   * changed.
   * @param userPassword The user's new password.
   * @throws org.opentcs.data.user.UserUnknownException If the user does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated User management should be strictly configuration-based and will not be supported by
   * kernel interaction in the future.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  void setUserPassword(String userName, String userPassword)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException;

  /**
   * Changes a user's permissions.
   *
   * @param userName The name of the user for which the permissions are to be
   * changed.
   * @param userPermissions The user's new permissions.
   * @throws org.opentcs.data.user.UserUnknownException If the user does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated User management should be strictly configuration-based and will not be supported by
   * kernel interaction in the future.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  void setUserPermissions(String userName,
                          Set<org.opentcs.data.user.UserPermission> userPermissions)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException;

  /**
   * Removes a user account.
   *
   * @param userName The name of the user whose account is to be removed.
   * @throws org.opentcs.data.user.UserUnknownException If the user does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated User management should be strictly configuration-based and will not be supported by
   * kernel interaction in the future.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  void removeUser(String userName)
      throws org.opentcs.data.user.UserUnknownException, CredentialsException;

  /**
   * Returns the current state of the kernel.
   *
   * @return The current state of the kernel.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  State getState()
      throws CredentialsException;

  /**
   * Sets the current state of the kernel.
   * <p>
   * Note: This method should only be used internally by the Kernel application.
   * </p>
   *
   * @param newState The state the kernel is to be set to.
   * @throws IllegalArgumentException If setting the new state is not possible,
   * e.g. because a transition from the current to the new state is not allowed.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  void setState(State newState)
      throws IllegalArgumentException, CredentialsException;

  /**
   * Returns the name of the model that is stored and could be loaded by the kernel.
   *
   * @return The name of the model, or <code>null</code> if there is no model.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalStateException If retrieving the model name was not possible.
   * @deprecated Use {@link PlantModelService#getPersistentModelName()} instead.
   */
  @Deprecated
  String getPersistentModelName()
      throws CredentialsException, IllegalStateException;

  /**
   * Returns the name of the model that is currently loaded in the kernel.
   *
   * @return The name of the currently loaded model.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link PlantModelService#getLoadedModelName()} instead.
   */
  @Deprecated
  String getLoadedModelName()
      throws CredentialsException;

  /**
   * Replaces the kernel's current model with an empty one.
   *
   * @param modelName The newly created model's name.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  void createModel(String modelName)
      throws CredentialsException;

  /**
   * Creates a new plant model with the objects described in the given transfer object.
   * Implicitly saves/persists the new plant model.
   *
   * @param to The transfer object describing the plant model objects to be created.
   * @throws ObjectUnknownException If any referenced object does not exist.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   * @throws CredentialsException If the calling client is not allowed to execute this method.
   * @throws IllegalStateException If there was a problem persisting the model.
   * @deprecated Use {@link PlantModelService#createPlantModel(
   * org.opentcs.access.to.model.PlantModelCreationTO)} instead.
   */
  @Deprecated
  void createPlantModel(PlantModelCreationTO to)
      throws CredentialsException, ObjectUnknownException, ObjectExistsException,
             IllegalStateException;

  /**
   * Loads the saved model into the kernel.
   * If there is no saved model, a new empty model will be loaded.
   *
   * @throws IOException If the model cannot be loaded.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Loading a plant model is a kernel-internal function. External clients need not care
   * about persistence, internal clients should use {@link LocalKernel#loadPlantModel()} instead.
   */
  @Deprecated
  void loadModel()
      throws IOException, CredentialsException;

  /**
   * Saves the current model under the given name.
   *
   * If there is a saved model, it will be overwritten.
   *
   * @param modelName The name under which the current model is to be saved. If
   * <code>null</code>, the model's current name will be used, otherwise the
   * model will be renamed accordingly.
   * @throws IOException If the model could not be persisted for some reason.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Saving a plant model is a kernel-internal function. External clients need not care
   * about persistence, internal clients should use {@link LocalKernel#savePlantModel()} instead.
   */
  @Deprecated
  void saveModel(@Nullable String modelName)
      throws IOException, CredentialsException;

  /**
   * Removes the saved model if there is one.
   *
   * @throws IOException If deleting the model was not possible for some reason.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Create an empty model with
   * {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeModel()
      throws IOException, CredentialsException;

  /**
   * Returns a single TCSObject of the given class.
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param ref A reference to the object to be returned.
   * @return A copy of the referenced object, or <code>null</code> if no such
   * object exists or if an object exists but is not an instance of the given
   * class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObject(
   * java.lang.Class, org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                          TCSObjectReference<T> ref)
      throws CredentialsException;

  /**
   * Returns a single TCSObject of the given class.
   *
   * @param <T> The TCSObject's actual type.
   * @param clazz The class of the object to be returned.
   * @param name The name of the object to be returned.
   * @return A copy of the named object, or <code>null</code> if no such
   * object exists or if an object exists but is not an instance of the given
   * class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObject(java.lang.Class, java.lang.String)}
   * instead.
   */
  @Deprecated
  <T extends TCSObject<T>> T getTCSObject(Class<T> clazz,
                                          String name)
      throws CredentialsException;

  /**
   * Returns all existing TCSObjects of the given class.
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @return Copies of all existing objects of the given class.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObjects(java.lang.Class)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz)
      throws CredentialsException;

  /**
   * Returns all existing TCSObjects of the given class whose names match the
   * given pattern.
   *
   * @param <T> The TCSObjects' actual type.
   * @param clazz The class of the objects to be returned.
   * @param regexp A regular expression describing the names of the objects to
   * be returned; if <code>null</code>, all objects of the given class are
   * returned.
   * @return Copies of all existing objects of the given class whose names match
   * the given pattern. If no such objects exist, the returned set will be
   * empty.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObjects(
   * java.lang.Class, java.util.function.Predicate)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> Set<T> getTCSObjects(Class<T> clazz,
                                                @Nullable Pattern regexp)
      throws CredentialsException;

  /**
   * Returns all existing {@link TCSObject}s of the given class for which the given predicate is
   * true.
   *
   * @param <T> The objects' actual type.
   * @param clazz The class of the objects to be returned.
   * @param predicate The predicate that must be true for returned objects.
   * @return Copies of all existing objects of the given class for which the given predicate is
   * true. If no such objects exist, the returned set will be empty.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#fetchObjects(
   * java.lang.Class, java.util.function.Predicate)} instead.
   */
  @Deprecated
  <T extends TCSObject<T>> Set<T> getTCSObjects(@Nonnull Class<T> clazz,
                                                @Nonnull Predicate<? super T> predicate)
      throws CredentialsException;

  /**
   * Rename a TCSObject.
   *
   * @param ref A reference to the object to be renamed.
   * @param newName The object's new name.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws ObjectExistsException If the object cannot be renamed because there
   * is already an object with the given new name.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void renameTCSObject(TCSObjectReference<?> ref, String newName)
      throws CredentialsException, ObjectUnknownException, ObjectExistsException;

  /**
   * Sets an object's property.
   *
   * @param ref A reference to the object to be modified.
   * @param key The property's key.
   * @param value The property's (new) value. If <code>null</code>, removes the
   * property from the object.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TCSObjectService#updateObjectProperty(
   * org.opentcs.data.TCSObjectReference, java.lang.String, java.lang.String)} instead.
   */
  @Deprecated
  void setTCSObjectProperty(TCSObjectReference<?> ref,
                            String key,
                            @Nullable String value)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Clears all of an object's properties.
   *
   * @param ref A reference to the object to be modified.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   */
  @Deprecated
  void clearTCSObjectProperties(TCSObjectReference<?> ref)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Remove a TCSObject.
   *
   * @param ref A reference to the object to be removed.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * to create complete plant models, implicitly removing model objects that existed before. Removal
   * of transport orders is handled within the kernel itself.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeTCSObject(TCSObjectReference<?> ref)
      throws CredentialsException, ObjectUnknownException;

  /**
   * Publishes a user notification.
   *
   * @param notification The notification to be published.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use{@link NotificationService#publishUserNotification(
   * org.opentcs.data.notification.UserNotification)} instead.
   */
  @Deprecated
  void publishUserNotification(UserNotification notification)
      throws CredentialsException;

  /**
   * Returns a list of user notifications.
   *
   * @param predicate A filter predicate that accepts the user notifications to be returned. May be
   * <code>null</code> to return all existing user notifications.
   * @return A list of user notifications.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link NotificationService#fetchUserNotifications(
   * java.util.function.Predicate)} instead.
   */
  @Deprecated
  List<UserNotification> getUserNotifications(Predicate<UserNotification> predicate)
      throws CredentialsException;

  /**
   * Adds a visual layout to the current model.
   * A new layout is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created layout is then returned.
   *
   * @return A copy of the newly created layout.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  VisualLayout createVisualLayout()
      throws CredentialsException;

  /**
   * Sets a layout's scale on the X axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleX The layout's new scale on the X axis.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVisualLayoutScaleX(TCSObjectReference<VisualLayout> ref,
                             double scaleX)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's scale on the Y axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleY The layout's new scale on the Y axis.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVisualLayoutScaleY(TCSObjectReference<VisualLayout> ref,
                             double scaleY)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's colors.
   *
   * @param ref A reference to the layout to be modified.
   * @param colors The layout's new colors.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Will be removed. Storing named colors is out of scope here.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVisualLayoutColors(TCSObjectReference<VisualLayout> ref,
                             Map<String, Color> colors)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's elements.
   *
   * @param ref A reference to the layout to be modified.
   * @param elements The layout's new elements.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVisualLayoutElements(TCSObjectReference<VisualLayout> ref,
                               Set<LayoutElement> elements)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a layout's view bookmarks.
   *
   * @param ref A reference to the layout to be modified.
   * @param bookmarks The layout's new bookmarks.
   * @throws ObjectUnknownException If the referenced layout does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Will be removed. Users may find/focus on course elements, instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVisualLayoutViewBookmarks(TCSObjectReference<VisualLayout> ref,
                                    List<ViewBookmark> bookmarks)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a point to the current model.
   * A new point is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created point is then returned.
   *
   * @return A copy of the newly created point.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  Point createPoint()
      throws CredentialsException;

  /**
   * Sets the physical coordinates of a point.
   *
   * @param ref A reference to the point to be modified.
   * @param position The point's new coordinates.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setPointPosition(TCSObjectReference<Point> ref, Triple position)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the vehicle's (assumed) orientation angle at the given position.
   * The allowed value range is [-360.0..360.0], and <code>Double.NaN</code>, to
   * indicate that the vehicle's orientation at the point is unspecified.
   *
   * @param ref A reference to the point to be modified.
   * @param angle The new angle.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                       double angle)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the type of a point.
   *
   * @param ref A reference to the point to be modified.
   * @param newType The point's new type.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a path to the current model.
   * A new path is created with a generated unique ID and name and ending in the
   * point specified by the given name; all other attributes set to default
   * values. Furthermore, the point is registered with the point which it
   * originates in and with the one it ends in. A copy of the newly created path
   * is then returned.
   *
   * @param srcRef A reference to the point which the newly created path
   * originates in.
   * @param destRef A reference to the point which the newly created path ends
   * in.
   * @return A copy of the newly created path.
   * @throws ObjectUnknownException If any of the referenced points does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  Path createPath(TCSObjectReference<Point> srcRef,
                  TCSObjectReference<Point> destRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the length of a path.
   *
   * @param ref A reference to the path to be modified.
   * @param length The new length of the path, in millimetres.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>length</code> is zero or
   * negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setPathLength(TCSObjectReference<Path> ref, long length)
      throws ObjectUnknownException, IllegalArgumentException,
             CredentialsException;

  /**
   * Sets the routing cost of a path.
   *
   * @param ref A reference to the path to be modified.
   * @param cost The new routing cost of the path (unitless).
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>cost</code> is zero or negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setPathRoutingCost(TCSObjectReference<Path> ref, long cost)
      throws ObjectUnknownException, IllegalArgumentException,
             CredentialsException;

  /**
   * Sets the maximum allowed velocity for a path.
   *
   * @param ref A reference to the path to be modified.
   * @param velocity The new maximum allowed velocity in mm/s.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>velocity</code> is negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setPathMaxVelocity(TCSObjectReference<Path> ref, int velocity)
      throws ObjectUnknownException, IllegalArgumentException,
             CredentialsException;

  /**
   * Sets the maximum allowed reverse velocity for a path.
   *
   * @param ref A reference to the path to be modified.
   * @param velocity The new maximum reverse velocity, in mm/s.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws IllegalArgumentException If <code>velocity</code> is negative.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setPathMaxReverseVelocity(TCSObjectReference<Path> ref, int velocity)
      throws ObjectUnknownException, IllegalArgumentException,
             CredentialsException;

  /**
   * Locks/Unlocks a path.
   *
   * @param ref A reference to the path to be modified.
   * @param locked Indicates whether the path is to be locked
   * (<code>true</code>) or unlocked (<code>false</code>).
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link RouterService#updatePathLock(
   * org.opentcs.data.TCSObjectReference, boolean)} instead.
   */
  @Deprecated
  void setPathLocked(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new vehicle.
   * A new vehicle is created with a generated unique ID and name and all other
   * attributes set to default values. A copy of the newly created vehicle is
   * then returned.
   *
   * @return A copy of the newly created vehicle type.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  Vehicle createVehicle()
      throws CredentialsException;

  /**
   * Sets a vehicle's critical energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new critical energy level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVehicleEnergyLevelCritical(TCSObjectReference<Vehicle> ref,
                                     int energyLevel)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a vehicle's good energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new good energy level.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                 int energyLevel)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a vehicle's length.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param length The vehicle's new length.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the categories of transport orders a vehicle can process.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param processableCategories A set of transport order categories.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link VehicleService#updateVehicleProcessableCategories(
   * org.opentcs.data.TCSObjectReference, java.util.Set)} instead.
   */
  @Deprecated
  void setVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                       Set<String> processableCategories)
      throws ObjectUnknownException;

  /**
   * Creates a new location type.
   * A new location type is created with a generated unique ID and name and all
   * other attributes set to default values. A copy of the newly created
   * location type is then returned.
   *
   * @return A copy of the newly created location type.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  LocationType createLocationType()
      throws CredentialsException;

  /**
   * Adds an allowed operation to a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be allowed.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void addLocationTypeAllowedOperation(TCSObjectReference<LocationType> ref,
                                       String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes an allowed operation from a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be disallowed.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeLocationTypeAllowedOperation(TCSObjectReference<LocationType> ref,
                                          String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new location.
   * A new location of the given type is created with a generated unique ID and
   * name and all other attributes set to default values. A copy of the newly
   * created location is then returned.
   *
   * @param typeRef A reference to the location type of which the newly created
   * location is supposed to be.
   * @return A copy of the newly created location.
   * @throws ObjectUnknownException If the reference location type does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  Location createLocation(TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets the physical coordinates of a location.
   *
   * @param ref A reference to the location to be modified.
   * @param position The location's new coordinates.
   * @throws ObjectUnknownException If the referenced location does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setLocationPosition(TCSObjectReference<Location> ref, Triple position)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a location's type.
   *
   * @param ref A reference to the location.
   * @param typeRef A reference to the location's new type.
   * @throws ObjectUnknownException If the referenced location or type do not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setLocationType(TCSObjectReference<Location> ref,
                       TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Connects a location to a point (expressing that the location is reachable
   * from that point).
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @throws ObjectUnknownException If the referenced location or point does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void connectLocationToPoint(TCSObjectReference<Location> locRef,
                              TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Disconnects a location from a point (expressing that the location isn't
   * reachable from the point any more).
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @throws ObjectUnknownException If the referenced location or point does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void disconnectLocationFromPoint(TCSObjectReference<Location> locRef,
                                   TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds an allowed operation to a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be added.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void addLocationLinkAllowedOperation(TCSObjectReference<Location> locRef,
                                       TCSObjectReference<Point> pointRef,
                                       String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes an allowed operation from a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be removed.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeLocationLinkAllowedOperation(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef,
                                          String operation)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes all allowed operations (for all vehicle types) from a link between
   * a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void clearLocationLinkAllowedOperations(TCSObjectReference<Location> locRef,
                                          TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a block to the current model.
   * A new block is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created block is then returned.
   *
   * @return A copy of the newly created block.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  Block createBlock()
      throws CredentialsException;

  /**
   * Adds a member to a block.
   *
   * @param ref A reference to the block to be modified.
   * @param newMemberRef A reference to the new member.
   * @throws ObjectUnknownException If any of the referenced block and member do
   * not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void addBlockMember(TCSObjectReference<Block> ref,
                      TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes a member from a block.
   *
   * @param ref A reference to the block to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @throws ObjectUnknownException If the referenced block does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeBlockMember(TCSObjectReference<Block> ref,
                         TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a group to the current model.
   * A new group is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created block is then returned.
   *
   * @return A copy of the newly created group.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  Group createGroup()
      throws CredentialsException;

  /**
   * Adds a member to a group.
   *
   * @param ref A reference to the group to be modified.
   * @param newMemberRef A reference to the new member.
   * @throws ObjectUnknownException If any of the referenced group and member do
   * not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void addGroupMember(TCSObjectReference<Group> ref,
                      TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes a member from a group.
   *
   * @param ref A reference to the group to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @throws ObjectUnknownException If the referenced group does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeGroupMember(TCSObjectReference<Group> ref,
                         TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Adds a static route to the current model.
   * A new route is created with a unique ID and name and all other attributes
   * set to default values. A copy of the newly created route is then returned.
   *
   * @return A copy of the newly created route.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  org.opentcs.data.model.StaticRoute createStaticRoute()
      throws CredentialsException;

  /**
   * Adds a hop to a route.
   *
   * @param ref A reference to the route to be modified.
   * @param newHopRef A reference to the new hop.
   * @throws ObjectUnknownException If any of the referenced route and hop do
   * not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void addStaticRouteHop(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref,
                         TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes all hops from a route.
   *
   * @param ref A reference to the route to be modified.
   * @throws ObjectUnknownException If the referenced route does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link #createPlantModel(org.opentcs.access.to.model.PlantModelCreationTO)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void clearStaticRouteHops(TCSObjectReference<org.opentcs.data.model.StaticRoute> ref)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new transport order.
   * A new transport order is created with a generated unique ID and name,
   * containing the given <code>DriveOrder</code>s and with all other attributes
   * set to their default values. A copy of the newly created transport order
   * is then returned.
   *
   * @param destinations The list of destinations that have to be travelled to
   * when processing this transport order.
   * @return A copy of the newly created transport order.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  TransportOrder createTransportOrder(List<Destination> destinations)
      throws CredentialsException;

  /**
   * Creates a new transport order.
   * A new transport order is created with a generated unique ID and all other attributes taken from
   * the given transfer object.
   * This method also implicitly adds the transport order to its wrapping sequence, if any.
   * A copy of the newly created transport order is then returned.
   *
   * @param to Describes the transport order to be created.
   * @return A copy of the newly created transport order.
   * @throws ObjectUnknownException If any referenced object does not exist.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   * @throws CredentialsException If the calling client is not allowed to execute this method.
   * @deprecated Use
   * {@link TransportOrderService#createTransportOrder(
   * org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  TransportOrder createTransportOrder(TransportOrderCreationTO to)
      throws CredentialsException, ObjectUnknownException, ObjectExistsException;

  /**
   * Sets a transport order's deadline.
   *
   * @param ref A reference to the transport order to be modified.
   * @param deadline The transport order's new deadline.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setTransportOrderDeadline(TCSObjectReference<TransportOrder> ref,
                                 long deadline)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Activates a transport order and makes it available for processing by a
   * vehicle.
   *
   * @param ref A reference to the transport order to be modified.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TransportOrderService#createTransportOrder(
   * org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  void activateTransportOrder(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets a transport order's intended vehicle.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param vehicleRef A reference to the vehicle intended to process the order.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setTransportOrderIntendedVehicle(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Copies drive order data from a list of drive orders to the given transport
   * order's future drive orders.
   *
   * @param orderRef A reference to the transport order to be modified.
   * @param newOrders The drive orders containing the data to be copied into
   * this transport order's drive orders.
   * @throws ObjectUnknownException If the referenced transport order is not
   * in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   * @deprecated Use {@link LocalKernel#setTransportOrderFutureDriveOrders(org.opentcs.data.TCSObjectReference, java.util.List)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setTransportOrderFutureDriveOrders(
      TCSObjectReference<TransportOrder> orderRef,
      List<DriveOrder> newOrders)
      throws ObjectUnknownException, CredentialsException,
             IllegalArgumentException;

  /**
   * Adds a dependency to a transport order on another transport order.
   *
   * @param orderRef A reference to the order that the dependency is to be added
   * to.
   * @param newDepRef A reference to the order that is the new dependency.
   * @throws ObjectUnknownException If any of the referenced transport orders
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void addTransportOrderDependency(TCSObjectReference<TransportOrder> orderRef,
                                   TCSObjectReference<TransportOrder> newDepRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Removes a dependency from a transport order on another transport order.
   *
   * @param orderRef A reference to the order that the dependency is to be
   * removed from.
   * @param rmDepRef A reference to the order that is not to be depended on any
   * more.
   * @throws ObjectUnknownException If any of the referenced transport orders
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeTransportOrderDependency(
      TCSObjectReference<TransportOrder> orderRef,
      TCSObjectReference<TransportOrder> rmDepRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates a new order sequence.
   * A new order sequence is created with a generated unique ID and name. A copy
   * of the newly created order sequence is then returned.
   *
   * @return A copy of the newly created order sequence.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createOrderSequence(org.opentcs.access.to.order.OrderSequenceCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  OrderSequence createOrderSequence()
      throws CredentialsException;

  /**
   * Creates a new order sequence.
   * A new order sequence is created with a generated unique ID and all other attributes taken from
   * the given transfer object.
   * A copy of the newly created order sequence is then returned.
   *
   * @param to Describes the order sequence to be created.
   * @return A copy of the newly created order sequence.
   * @throws ObjectUnknownException If any referenced object does not exist.
   * @throws ObjectExistsException If an object with the same name already exists in the model.
   * @throws CredentialsException If the calling client is not allowed to execute this method.
   * @deprecated Use
   * {@link TransportOrderService#createOrderSequence(
   * org.opentcs.access.to.order.OrderSequenceCreationTO)} instead.
   */
  @Deprecated
  OrderSequence createOrderSequence(OrderSequenceCreationTO to)
      throws CredentialsException;

  /**
   * Adds a transport order to an order sequence.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param orderRef A reference to the transport order to be added.
   * @throws ObjectUnknownException If the referenced order sequence or
   * transport order is not in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalArgumentException If the sequence is already marked as
   * <em>complete</em>, if the sequence already contains the given order or
   * if the given transport order has already been activated.
   * @deprecated Use
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)} instead.
   */
  @Deprecated
  void addOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                             TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException, CredentialsException,
             IllegalArgumentException;

  /**
   * Removes a transport order from an order sequence.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param orderRef A reference to the transport order to be removed.
   * @throws ObjectUnknownException If the referenced order sequence or
   * transport order is not in this pool.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Usage unclear. Handling of subsequent orders in the sequence is fuzzy.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void removeOrderSequenceOrder(TCSObjectReference<OrderSequence> seqRef,
                                TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's complete flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @throws ObjectUnknownException If the referenced order sequence does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link TransportOrderService#markOrderSequenceComplete(
   * org.opentcs.data.TCSObjectReference)} instead.
   */
  @Deprecated
  void setOrderSequenceComplete(TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's <em>failureFatal</em> flag.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param fatal The sequence's new <em>failureFatal</em> flag.
   * @throws ObjectUnknownException If the referenced order sequence does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createOrderSequence(org.opentcs.access.to.order.OrderSequenceCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setOrderSequenceFailureFatal(TCSObjectReference<OrderSequence> seqRef,
                                    boolean fatal)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sets an order sequence's intended vehicle.
   *
   * @param seqRef A reference to the order sequence to be modified.
   * @param vehicleRef A reference to the vehicle intended to process the order
   * sequence.
   * @throws ObjectUnknownException If the referenced order sequence or vehicle
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link #createOrderSequence(org.opentcs.access.to.order.OrderSequenceCreationTO)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  void setOrderSequenceIntendedVehicle(TCSObjectReference<OrderSequence> seqRef,
                                       TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Withdraw the referenced order, set its state to FAILED and stop the vehicle
   * that might be processing it.
   *
   * @param ref A reference to the transport order to be withdrawn.
   * @param immediateAbort If <code>false</code>, this method once will initiate
   * the withdrawal, leaving the transport order assigned to the vehicle until
   * it has finished the movements that it has already been ordered to execute.
   * The transport order's state will change to WITHDRAWN. If <code>true</code>,
   * the dispatcher will withdraw the order from the vehicle without further
   * waiting.
   * @param disableVehicle Whether setting the processing state of the vehicle
   * currently processing the transport order to
   * {@link org.opentcs.data.model.Vehicle.ProcState#UNAVAILABLE} to prevent
   * immediate redispatching of the vehicle.
   * @throws ObjectUnknownException If the referenced transport order does not
   * exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link DispatcherService#withdrawByTransportOrder(
   * org.opentcs.data.TCSObjectReference, boolean, boolean)} instead.
   */
  @Deprecated
  void withdrawTransportOrder(TCSObjectReference<TransportOrder> ref,
                              boolean immediateAbort,
                              boolean disableVehicle)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Withdraw any order that a vehicle might be processing, set its state to
   * FAILED and stop the vehicle.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param immediateAbort If <code>false</code>, this method once will initiate
   * the withdrawal, leaving the transport order assigned to the vehicle until
   * it has finished the movements that it has already been ordered to execute.
   * The transport order's state will change to WITHDRAWN. If <code>true</code>,
   * the dispatcher will withdraw the order from the vehicle without further
   * waiting.
   * @param disableVehicle Whether to set the processing state of the vehicle
   * currently processing the transport order to
   * {@link org.opentcs.data.model.Vehicle.ProcState#UNAVAILABLE} to prevent
   * immediate redispatching of the vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link DispatcherService#withdrawByVehicle(
   * org.opentcs.data.TCSObjectReference, boolean, boolean)} instead.
   */
  @Deprecated
  void withdrawTransportOrderByVehicle(TCSObjectReference<Vehicle> vehicleRef,
                                       boolean immediateAbort,
                                       boolean disableVehicle)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Explicitly trigger dispatching of the referenced idle vehicle.
   *
   * @param vehicleRef A reference to the vehicle to be dispatched.
   * @param setIdleIfUnavailable Whether to set the vehicle's processing state
   * to IDLE before dispatching if it is currently UNAVAILABLE. If the vehicle's
   * processing state is UNAVAILABLE and this flag is not set, an
   * IllegalArgumentException will be thrown.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IllegalArgumentException If the referenced vehicle is not in a
   * dispatchable state (IDLE or, if the corresponding flag is set, UNAVAILABLE).
   * @deprecated Use {@link DispatcherService#dispatch()} instead.
   */
  @Deprecated
  void dispatchVehicle(TCSObjectReference<Vehicle> vehicleRef,
                       boolean setIdleIfUnavailable)
      throws ObjectUnknownException, CredentialsException,
             IllegalArgumentException;

  /**
   * Reset a vehicle's position, implicitly aborting any transport order it is currently processing,
   * disabling it for transport order disposition and freeing any allocated resources.
   *
   * @param vehicleRef A reference to the vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use {@link DispatcherService#releaseVehicle(org.opentcs.data.TCSObjectReference)}
   * instead.
   */
  @Deprecated
  void releaseVehicle(TCSObjectReference<Vehicle> vehicleRef)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Sends a message to the communication adapter associated with the referenced
   * vehicle.
   * This method provides a generic one-way communication channel to the
   * communication adapter of a vehicle. Note that there is no return value and
   * no guarantee that the communication adapter will understand the message;
   * clients cannot even know which communication adapter is attached to a
   * vehicle, so it's entirely possible that the communication adapter receiving
   * the message does not understand it.
   *
   * @see VehicleCommAdapter#processMessage(java.lang.Object)
   * @param vehicleRef The vehicle whose communication adapter shall receive the
   * message.
   * @param message The message to be delivered.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use{@link VehicleService#sendCommAdapterMessage(
   * org.opentcs.data.TCSObjectReference, java.lang.Object)} instead.
   */
  @Deprecated
  void sendCommAdapterMessage(TCSObjectReference<Vehicle> vehicleRef,
                              Object message)
      throws ObjectUnknownException, CredentialsException;

  /**
   * Creates and returns a list of transport orders defined in a script file.
   *
   * @param fileName The name of the script file defining the transport orders
   * to be created.
   * @return The list of transport orders created. If none were created, the
   * returned list is empty.
   * @throws ObjectUnknownException If any object referenced in the script file
   * does not exist.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws IOException If there was a problem reading or parsing the file with
   * the given name.
   * @deprecated Parsing and evaluating scripts/batch files is out of scope for the kernel. Such
   * files should be handled outside the kernel, e.g. in a kernel extension, and lead to calls of
   * {@link #createTransportOrder(org.opentcs.access.to.order.TransportOrderCreationTO)}, instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Method will be removed.")
  List<TransportOrder> createTransportOrdersFromScript(String fileName)
      throws ObjectUnknownException, CredentialsException, IOException;

  /**
   * Notifies the router that the topology has changed in a significant way and needs to be
   * re-evaluated.
   *
   * @throws CredentialsException If the calling client is not allowed to execute this method.
   * @deprecated Use {@link RouterService#updateRoutingTopology()} instead.
   */
  @Deprecated
  void updateRoutingTopology()
      throws CredentialsException;

  /**
   * Returns the costs for travelling from one location to a given set of
   * others.
   *
   * @param vRef A reference to the vehicle that shall be used for calculating
   * the costs. If it's <code>null</code> a random vehicle will be used.
   * @param srcRef A reference to the source Location
   * @param destRefs A set containing all destination locations
   * @return A list containing tuples of a location and the costs to travel there
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @throws ObjectUnknownException If something is not known.
   * @deprecated Providing travel costs to external clients will not be part of the standard kernel
   * API any more.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  List<TravelCosts> getTravelCosts(@Nullable TCSObjectReference<Vehicle> vRef,
                                   TCSObjectReference<Location> srcRef,
                                   Set<TCSObjectReference<Location>> destRefs)
      throws CredentialsException, ObjectUnknownException;

  /**
   * Returns the result of the query defined by the given class.
   *
   * @param <T> The result's actual type.
   * @param clazz Defines the query and the class of the result to be returned.
   * @return The result of the query defined by the given class, or
   * <code>null</code>, if the defined query is not supported in the kernel's
   * current state.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Use
   * {@link SchedulerService#fetchSchedulerAllocations()} instead.
   */
  @Deprecated
  <T extends org.opentcs.access.queries.Query<T>> T query(Class<T> clazz)
      throws CredentialsException;

  /**
   * Returns the current time factor for simulation.
   *
   * @return The current time factor for simulation.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Simulation is out of scope for the openTCS project. Simulation-related components
   * should be configured individually.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  double getSimulationTimeFactor()
      throws CredentialsException;

  /**
   * Sets a time factor for simulation.
   *
   * @param factor The new time factor.
   * @throws CredentialsException If the calling client is not allowed to
   * execute this method.
   * @deprecated Simulation is out of scope for the openTCS project. Simulation-related components
   * should be configured individually.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  void setSimulationTimeFactor(double factor)
      throws CredentialsException;

  /**
   * {@inheritDoc}
   *
   * @deprecated Subscribe/Unsubscribe to the application's event bus instead.
   */
  @Deprecated
  @Override
  public void removeEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener);

  /**
   * {@inheritDoc}
   *
   * @deprecated Subscribe/Unsubscribe to the application's event bus instead.
   */
  @Deprecated
  @Override
  public void addEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener);

  /**
   * {@inheritDoc}
   *
   * @deprecated Subscribe/Unsubscribe to the application's event bus instead.
   */
  @Deprecated
  @Override
  public void addEventListener(
      org.opentcs.util.eventsystem.EventListener<org.opentcs.util.eventsystem.TCSEvent> listener,
      org.opentcs.util.eventsystem.EventFilter<org.opentcs.util.eventsystem.TCSEvent> filter);

  /**
   * The various states a kernel instance may be running in.
   */
  enum State {

    /**
     * The state in which the model/topology is created and parameters are set.
     */
    MODELLING,
    /**
     * The normal mode of operation in which transport orders may be accepted
     * and dispatched to vehicles.
     */
    OPERATING,
    /**
     * A transitional state the kernel is in while shutting down.
     */
    SHUTDOWN
  }
}
