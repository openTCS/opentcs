/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.ShapeLayoutElementCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
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
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.ShapeLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.util.Comparators;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class present a view on the complete static topology of an
 * openTCS model, i.e. Points, Paths etc., and Vehicles, contained
 * in a {@link TCSObjectPool TCSObjectPool}.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of
 * instances of this class must be synchronized externally.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Model {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Model.class);
  /**
   * The system's global object pool.
   */
  private final TCSObjectPool objectPool;
  /**
   * This model's name.
   */
  private String name = "";
  /**
   * This model's properties.
   */
  private Map<String, String> properties = new HashMap<>();

  /**
   * Creates a new model.
   *
   * @param globalPool The object pool serving as the container for this model's
   * data.
   */
  @Inject
  public Model(TCSObjectPool globalPool) {
    this.objectPool = Objects.requireNonNull(globalPool);
  }

  /**
   * Returns the <code>TCSObjectPool</code> serving as the container for this
   * model's data.
   *
   * @return The <code>TCSObjectPool</code> serving as the container for this
   * model's data.
   */
  public TCSObjectPool getObjectPool() {
    LOG.debug("method entry");
    return objectPool;
  }

  /**
   * Returns this model's name.
   *
   * @return This model's name.
   */
  public String getName() {
    LOG.debug("method entry");
    return name;
  }

  /**
   * Sets this model's name.
   *
   * @param name This model's new name.
   */
  public void setName(String name) {
    this.name = requireNonNull(name, "name");
  }

  /**
   * Returns this model's properties.
   *
   * @return This model's properties.
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Sets this model's properties.
   *
   * @param properties The properties.
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = requireNonNull(properties, "properties");
  }

  /**
   * Removes all model objects from this model and the object pool by which it is backed.
   */
  @SuppressWarnings("deprecation")
  public void clear() {
    LOG.debug("method entry");
    for (TCSObject<?> curObject : objectPool.getObjects((Pattern) null)) {
      if (curObject instanceof Point
          || curObject instanceof Path
          || curObject instanceof Vehicle
          || curObject instanceof LocationType
          || curObject instanceof Location
          || curObject instanceof Block
          || curObject instanceof Group
          || curObject instanceof org.opentcs.data.model.StaticRoute
          || curObject instanceof VisualLayout) {
        objectPool.removeObject(curObject.getReference());
        objectPool.emitObjectEvent(null,
                                   curObject,
                                   TCSObjectEvent.Type.OBJECT_REMOVED);
      }
    }
  }

  /**
   * Creates new plant model objects with unique IDs and all other attributes taken from the given
   * transfer object.
   *
   * @param to The transfer object from which to create the new objects.
   * @throws ObjectExistsException If an object with a new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  @SuppressWarnings("deprecation")
  public void createPlantModelObjects(PlantModelCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    for (PointCreationTO point : to.getPoints()) {
      createPoint(point);
    }
    for (PathCreationTO path : to.getPaths()) {
      createPath(path);
    }
    for (LocationTypeCreationTO locType : to.getLocationTypes()) {
      createLocationType(locType);
    }
    for (LocationCreationTO loc : to.getLocations()) {
      createLocation(loc);
    }
    for (BlockCreationTO block : to.getBlocks()) {
      createBlock(block);
    }
    for (GroupCreationTO group : to.getGroups()) {
      createGroup(group);
    }
    for (org.opentcs.access.to.model.StaticRouteCreationTO route : to.getStaticRoutes()) {
      createStaticRoute(route);
    }
    for (VehicleCreationTO vehicle : to.getVehicles()) {
      createVehicle(vehicle);
    }
    for (VisualLayoutCreationTO layout : to.getVisualLayouts()) {
      createVisualLayout(layout);
    }
  }

  /**
   * Creates a new visual layout with a unique ID and all other attributes set
   * to default values.
   *
   * @param objectID The object ID of the newly created layout. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created layout.
   * @deprecated Use {@link #createVisualLayout(org.opentcs.access.to.VisualLayoutCreationTO)}
   * instead.
   */
  @Deprecated
  public VisualLayout createVisualLayout(Integer objectID) {
    LOG.debug("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int layoutID
        = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String layoutName = objectPool.getUniqueObjectName("VLayout-", "00");
    VisualLayout newLayout = new VisualLayout(layoutID, layoutName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newLayout);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newLayout.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created layout.
    return newLayout;
  }

  /**
   * Creates a new visual layout with a unique name and all other attributes set
   * to default values.
   *
   * @param to The transfer object from which to create the new layout.
   * @return The newly created layout.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  @SuppressWarnings("deprecation")
  public VisualLayout createVisualLayout(VisualLayoutCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    VisualLayout newLayout = new VisualLayout(to.getName())
        .withScaleX(to.getScaleX())
        .withScaleY(to.getScaleY());
    for (ModelLayoutElementCreationTO mleTO : to.getModelElements()) {
      TCSObject<?> object = objectPool.getObject(mleTO.getName());
      ModelLayoutElement mle = new ModelLayoutElement(object.getReference());
      mle.setLayer(mleTO.getLayer());
      mle.setProperties(mleTO.getProperties());
      newLayout.getLayoutElements().add(mle);
    }
    for (ShapeLayoutElementCreationTO shapeTO : to.getShapeElements()) {
      ShapeLayoutElement shape = new ShapeLayoutElement();
      shape.setLayer(shapeTO.getLayer());
      shape.setProperties(shapeTO.getProperties());
      newLayout.getLayoutElements().add(shape);
    }
    objectPool.addObject(newLayout);
    objectPool.emitObjectEvent(newLayout.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created layout.
    return newLayout;
  }

  /**
   * Sets the layout's scale on the X axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleX The layout's new scale on the X axis.
   * @return The modified layout.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @deprecated Use {@link #createVisualLayout(org.opentcs.access.to.VisualLayoutCreationTO)}
   * instead.
   */
  @Deprecated
  public VisualLayout setVisualLayoutScaleX(
      TCSObjectReference<VisualLayout> ref,
      double scaleX)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    VisualLayout layout = objectPool.getObjectOrNull(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setScaleX(scaleX);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's scale on the Y axis.
   *
   * @param ref A reference to the layout to be modified.
   * @param scaleY The layout's new scale on the Y axis.
   * @return The modified layout.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @deprecated Use {@link #createVisualLayout(org.opentcs.access.to.VisualLayoutCreationTO)}
   * instead.
   */
  @Deprecated
  public VisualLayout setVisualLayoutScaleY(
      TCSObjectReference<VisualLayout> ref,
      double scaleY)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    VisualLayout layout = objectPool.getObjectOrNull(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setScaleY(scaleY);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's colors.
   *
   * @param ref A reference to the point to be modified.
   * @param colors The layout's new colors.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @deprecated Use {@link #createVisualLayout(org.opentcs.access.to.VisualLayoutCreationTO)}
   * instead.
   */
  @Deprecated
  public VisualLayout setVisualLayoutColors(
      TCSObjectReference<VisualLayout> ref,
      Map<String, Color> colors)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    VisualLayout layout = objectPool.getObjectOrNull(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setColors(colors);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's elements.
   *
   * @param ref A reference to the point to be modified.
   * @param elements The layout's new elements.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @deprecated Use {@link #createVisualLayout(org.opentcs.access.to.VisualLayoutCreationTO)}
   * instead.
   */
  @Deprecated
  public VisualLayout setVisualLayoutElements(
      TCSObjectReference<VisualLayout> ref,
      Set<LayoutElement> elements)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    VisualLayout layout = objectPool.getObjectOrNull(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setLayoutElements(elements);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Sets the layout's view bookmarks.
   *
   * @param ref A reference to the point to be modified.
   * @param bookmarks The layout's new bookmarks.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced object does not exist.
   * @deprecated Use {@link #createVisualLayout(org.opentcs.access.to.VisualLayoutCreationTO)}
   * instead.
   */
  @Deprecated
  public VisualLayout setVisualLayoutViewBookmarks(
      TCSObjectReference<VisualLayout> ref,
      List<org.opentcs.data.model.visualization.ViewBookmark> bookmarks)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    VisualLayout layout = objectPool.getObjectOrNull(VisualLayout.class, ref);
    if (layout == null) {
      throw new ObjectUnknownException(ref);
    }
    VisualLayout previousState = layout.clone();
    layout.setViewBookmarks(bookmarks);
    objectPool.emitObjectEvent(layout.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return layout;
  }

  /**
   * Creates a new point with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The object ID of the newly created point. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created point.
   * @deprecated Use {@link #createPoint(org.opentcs.access.to.PointCreationTO)} instead.
   */
  @Deprecated
  public Point createPoint(Integer objectID) {
    LOG.debug("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int pointID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String pointName = objectPool.getUniqueObjectName("Point-", "0000");
    Point newPoint = new Point(pointID, pointName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newPoint);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newPoint.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created point.
    return newPoint;
  }

  /**
   * Creates a new point with a unique name and all other attributes set to
   * default values.
   *
   * @param to The transfer object from which to create the new point.
   * @return The newly created point.
   * @throws ObjectExistsException If an object with the point's name already exists.
   */
  @SuppressWarnings("deprecation")
  public Point createPoint(PointCreationTO to)
      throws ObjectExistsException {
    // Get a unique ID for the new point and create an instance.
    Point newPoint = new Point(to.getName())
        .withPosition(to.getPosition())
        .withType(to.getType())
        .withVehicleOrientationAngle(to.getVehicleOrientationAngle())
        .withProperties(to.getProperties());
    objectPool.addObject(newPoint);
    objectPool.emitObjectEvent(newPoint.clone(), null, TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created point.
    return newPoint;
  }

  /**
   * Returns the point belonging to the given reference.
   *
   * @param ref A reference to the point to return.
   * @return The referenced point, if it exists, or <code>null</code>, if it
   * doesn't.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Point getPoint(TCSObjectReference<Point> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Point.class, ref);
  }

  /**
   * Returns the point with the given name.
   *
   * @param pointName The name of the point to return.
   * @return The point with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Point getPoint(String pointName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Point.class, pointName);
  }

  /**
   * Returns a set of points whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the points returned. If
   * <code>null</code>, all points will be returned.
   * @return A set of points whose names match the given regular expression. If
   * no such points exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<Point> getPoints(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(Point.class, regexp);
  }

  /**
   * Sets the physical coordinates of a given point.
   *
   * @param ref A reference to the point to be modified.
   * @param position The point's new coordinates.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @deprecated Use {@link #createPoint(org.opentcs.access.to.PointCreationTO)} instead.
   */
  @Deprecated
  public Point setPointPosition(TCSObjectReference<Point> ref, Triple position)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObjectOrNull(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    Point previousState = point.clone();
    point.setPosition(position);
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Sets the vehicle's (assumed) orientation angle at the given position.
   *
   * @param ref A reference to the point to be modified.
   * @param angle The new angle.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @deprecated Use {@link #createPoint(org.opentcs.access.to.PointCreationTO)} instead.
   */
  @Deprecated
  public Point setPointVehicleOrientationAngle(TCSObjectReference<Point> ref,
                                               double angle)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObjectOrNull(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    Point previousState = point.clone();
    point.setVehicleOrientationAngle(angle);
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Sets the type of a given point.
   *
   * @param ref A reference to the point to be modified.
   * @param newType The point's new type.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @deprecated Use {@link #createPoint(org.opentcs.access.to.PointCreationTO)} instead.
   */
  @Deprecated
  public Point setPointType(TCSObjectReference<Point> ref, Point.Type newType)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    if (newType == null) {
      throw new NullPointerException("newType is null");
    }
    Point point = objectPool.getObjectOrNull(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    Point previousState = point.clone();
    point.setType(newType);
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Adds an incoming path to a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  @SuppressWarnings("deprecation")
  private Point addPointIncomingPath(TCSObjectReference<Point> pointRef,
                                     TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    Path path = objectPool.getObject(Path.class, pathRef);
    // Check if the point really is the path's destination point.
    if (!path.getDestinationPoint().equals(point.getReference())) {
      throw new IllegalArgumentException(
          "Point is not the path's destination.");
    }
    Path previousState = path.clone();
    Set<TCSObjectReference<Path>> incomingPaths = new HashSet<>(point.getIncomingPaths());
    incomingPaths.add(path.getReference());
    point = objectPool.replaceObject(point.withIncomingPaths(incomingPaths));
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Removes an incoming path from a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  @SuppressWarnings("deprecation")
  private Point removePointIncomingPath(TCSObjectReference<Point> pointRef,
                                        TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    Path path = objectPool.getObject(Path.class, pathRef);
    Path previousState = path.clone();
    Set<TCSObjectReference<Path>> incomingPaths = new HashSet<>(point.getIncomingPaths());
    incomingPaths.remove(path.getReference());
    point = objectPool.replaceObject(point.withIncomingPaths(incomingPaths));
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Adds an outgoing path to a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  @SuppressWarnings("deprecation")
  private Point addPointOutgoingPath(TCSObjectReference<Point> pointRef,
                                     TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    Path path = objectPool.getObject(Path.class, pathRef);
    // Check if the point really is the path's source.
    if (!path.getSourcePoint().equals(point.getReference())) {
      throw new IllegalArgumentException("Point is not the path's source.");
    }
    Path previousState = path.clone();
    Set<TCSObjectReference<Path>> outgoingPaths = new HashSet<>(point.getOutgoingPaths());
    outgoingPaths.add(path.getReference());
    point = objectPool.replaceObject(point.withOutgoingPaths(outgoingPaths));
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Removes an outgoing path from a point.
   *
   * @param pointRef A reference to the point to be modified.
   * @param pathRef A reference to the path.
   * @return The modified point.
   * @throws ObjectUnknownException If the referenced point or path do not
   * exist.
   */
  @SuppressWarnings("deprecation")
  private Point removePointOutgoingPath(TCSObjectReference<Point> pointRef,
                                        TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    Path path = objectPool.getObject(Path.class, pathRef);
    Path previousState = path.clone();
    Set<TCSObjectReference<Path>> outgoingPaths = new HashSet<>(point.getOutgoingPaths());
    outgoingPaths.remove(path.getReference());
    point = objectPool.replaceObject(point.withOutgoingPaths(outgoingPaths));
    objectPool.emitObjectEvent(point.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Removes a point.
   *
   * @param ref A reference to the point to be removed.
   * @return The removed point.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @deprecated Use {@link #clear()} instead.
   */
  @Deprecated
  public Point removePoint(TCSObjectReference<Point> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObjectOrNull(Point.class, ref);
    if (point == null) {
      throw new ObjectUnknownException(ref);
    }
    // Remove any links to locations attached to this point.
    for (Location.Link curLink : point.getAttachedLinks()) {
      disconnectLocationFromPoint(curLink.getLocation(), ref);
    }
    // Remove any paths starting or ending in the removed point.
    for (TCSObjectReference<Path> curPathRef
             : new ArrayList<>(point.getOutgoingPaths())) {
      removePath(curPathRef);
    }
    for (TCSObjectReference<Path> curPathRef
             : new ArrayList<>(point.getIncomingPaths())) {
      removePath(curPathRef);
    }
    // Remove the point.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               point.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return point;
  }

  /**
   * Creates a new path with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The ID of the newly created path. If <code>null</code>, a
   * new, unique one will be generated.
   * @param srcRef A reference to the point which the new path originates in.
   * @param destRef A reference to the point which the new path ends in.
   * @return The newly created path.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @deprecated Use {@link #createPath(org.opentcs.access.to.PathCreationTO)} instead.
   */
  @Deprecated
  public Path createPath(Integer objectID,
                         TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> destRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point srcPoint = objectPool.getObjectOrNull(Point.class, srcRef);
    if (srcPoint == null) {
      throw new ObjectUnknownException(srcRef);
    }
    Point destPoint = objectPool.getObjectOrNull(Point.class, destRef);
    if (destPoint == null) {
      throw new ObjectUnknownException(destRef);
    }
    // Get a unique ID and name for the new path and create an instance.
    int pathID
        = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String pathName = objectPool.getUniqueObjectName("Path-", "0000");
    Path newPath = new Path(pathID, pathName, srcPoint.getReference(),
                            destPoint.getReference());
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newPath);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newPath.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    addPointOutgoingPath(srcRef, newPath.getReference());
    addPointIncomingPath(destRef, newPath.getReference());
    // Return the newly created point.
    return newPath;
  }

  /**
   * Creates a new path from the given transfer object.
   *
   * @param to The transfer object from which to create the new path.
   * @return The newly created path.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws ObjectExistsException If an object with the same name as the path already exists.
   */
  @SuppressWarnings("deprecation")
  public Path createPath(PathCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    requireNonNull(to, "to");

    Point srcPoint = objectPool.getObject(Point.class, to.getSrcPointName());
    Point destPoint = objectPool.getObject(Point.class, to.getDestPointName());
    Path newPath = new Path(to.getName(),
                            srcPoint.getReference(),
                            destPoint.getReference())
        .withLength(to.getLength())
        .withRoutingCost(to.getRoutingCost())
        .withMaxVelocity(to.getMaxVelocity())
        .withMaxReverseVelocity(to.getMaxReverseVelocity())
        .withProperties(to.getProperties())
        .withLocked(to.isLocked());

    // Store the instance in the global object pool.
    objectPool.addObject(newPath);

    objectPool.emitObjectEvent(newPath.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);

    addPointOutgoingPath(srcPoint.getReference(), newPath.getReference());
    addPointIncomingPath(destPoint.getReference(), newPath.getReference());

    return newPath;
  }

  /**
   * Returns a referenced path.
   *
   * @param ref A reference to the path to be returned.
   * @return The path with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Path getPath(TCSObjectReference<Path> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Path.class, ref);
  }

  /**
   * Returns the path with the given name.
   *
   * @param pathName The name of the path to be returned.
   * @return The path with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Path getPath(String pathName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Path.class, pathName);
  }

  /**
   * Returns a set of paths whose names match the given regular expression.
   *
   * @param regexp The regular expression which the returned path's names must
   * match. If <code>null</code>, all paths are returned.
   * @return A set of paths whose names match the given regular expression. If
   * no such paths exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<Path> getPaths(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(Path.class, regexp);
  }

  /**
   * Sets the length of a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newLength The path's new length.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @deprecated Use {@link #createPath(org.opentcs.access.to.PathCreationTO)} instead.
   */
  @Deprecated
  public Path setPathLength(TCSObjectReference<Path> ref, long newLength)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Path path = objectPool.getObjectOrNull(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setLength(newLength);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Sets the routing cost of a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newCost The path's new cost.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @deprecated Use {@link #createPath(org.opentcs.access.to.PathCreationTO)} instead.
   */
  @Deprecated
  public Path setPathRoutingCost(TCSObjectReference<Path> ref, long newCost)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Path path = objectPool.getObjectOrNull(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setRoutingCost(newCost);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Sets the maximum allowed velocity for a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newVelocity The path's new maximum allowed velocity.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @deprecated Use {@link #createPath(org.opentcs.access.to.PathCreationTO)} instead.
   */
  @Deprecated
  public Path setPathMaxVelocity(TCSObjectReference<Path> ref, int newVelocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Path path = objectPool.getObjectOrNull(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setMaxVelocity(newVelocity);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Sets the maximum allowed reverse velocity for a given path.
   *
   * @param ref A reference to the path to be modified.
   * @param newVelocity The path's new maximum allowed reverse velocity.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @deprecated Use {@link #createPath(org.opentcs.access.to.PathCreationTO)} instead.
   */
  @Deprecated
  public Path setPathMaxReverseVelocity(TCSObjectReference<Path> ref,
                                        int newVelocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Path path = objectPool.getObjectOrNull(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    path.setMaxReverseVelocity(newVelocity);
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Locks/Unlocks a path.
   *
   * @param ref A reference to the path to be modified.
   * @param newLocked If <code>true</code>, this path will be locked when the
   * method call returns; if <code>false</code>, this path will be unlocked.
   * @return The modified path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   */
  @SuppressWarnings("deprecation")
  public Path setPathLocked(TCSObjectReference<Path> ref, boolean newLocked)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    Path previousState = path.clone();
    path = objectPool.replaceObject(path.withLocked(newLocked));
    objectPool.emitObjectEvent(path.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Removes a path.
   *
   * @param ref A reference to the path to be removed.
   * @return The removed path.
   * @throws ObjectUnknownException If the referenced path does not exist.
   * @deprecated Use {@link #clear()} instead.
   */
  @Deprecated
  public Path removePath(TCSObjectReference<Path> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Path path = objectPool.getObjectOrNull(Path.class, ref);
    if (path == null) {
      throw new ObjectUnknownException(ref);
    }
    Path previousState = path.clone();
    removePointOutgoingPath(path.getSourcePoint(), ref);
    removePointIncomingPath(path.getDestinationPoint(), ref);
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return path;
  }

  /**
   * Creates a new location type with a unique name and all other attributes set
   * to their default values.
   *
   * @param objectID The new location type's ID. If <code>null</code>, a new,
   * unique one will be generated.
   * @return The newly created location type.
   * @deprecated Use {@link #createLocationType(org.opentcs.access.to.LocationTypeCreationTO)}
   * instead.
   */
  @Deprecated
  public LocationType createLocationType(Integer objectID) {
    LOG.debug("method entry");
    int typeID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String typeName = objectPool.getUniqueObjectName("LType-", "00");
    LocationType newType = new LocationType(typeID, typeName);
    try {
      objectPool.addObject(newType);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newType.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newType;
  }

  /**
   * Creates a new location type with a unique name and all other attributes set
   * to their default values.
   *
   * @param to The transfer object from which to create the new location type.
   * @return The newly created location type.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   */
  @SuppressWarnings("deprecation")
  public LocationType createLocationType(LocationTypeCreationTO to)
      throws ObjectExistsException {
    LocationType newType = new LocationType(to.getName())
        .withAllowedOperations(to.getAllowedOperations())
        .withProperties(to.getProperties());
    objectPool.addObject(newType);
    objectPool.emitObjectEvent(newType.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newType;
  }

  /**
   * Returns the referenced location type.
   *
   * @param ref A reference to the location type to be returned.
   * @return The referenced location type, or <code>null</code>, if no such
   * location type exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public LocationType getLocationType(TCSObjectReference<LocationType> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(LocationType.class, ref);
  }

  /**
   * Returns the location type with the given name.
   *
   * @param typeName The name of the location type to return.
   * @return The location type with the given name, or <code>null</code>, if no
   * such location type exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public LocationType getLocationType(String typeName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(LocationType.class, typeName);
  }

  /**
   * Returns a set of location types whose names match the given regular
   * expression.
   *
   * @param regexp The regular expression describing the names of the
   * location types to return. If <code>null</code>, all location types are
   * returned.
   * @return A set of location types whose names match the given regular
   * expression. If no such location types exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<LocationType> getLocationTypes(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(LocationType.class, regexp);
  }

  /**
   * Adds an allowed operation to a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be allowed.
   * @return The modified location type.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @deprecated Use {@link #createLocationType(org.opentcs.access.to.LocationTypeCreationTO)}
   * instead.
   */
  @Deprecated
  public LocationType addLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    LocationType type = objectPool.getObjectOrNull(LocationType.class, ref);
    if (type == null) {
      throw new ObjectUnknownException(ref);
    }
    LocationType previousState = type.clone();
    type.addAllowedOperation(operation);
    objectPool.emitObjectEvent(type.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return type;
  }

  /**
   * Removes an allowed operation from a location type.
   *
   * @param ref A reference to the location type to be modified.
   * @param operation The operation to be disallowed.
   * @return The modified location type.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @deprecated Use {@link #createLocationType(org.opentcs.access.to.LocationTypeCreationTO)}
   * instead.
   */
  @Deprecated
  public LocationType removeLocationTypeAllowedOperation(
      TCSObjectReference<LocationType> ref, String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    LocationType type = objectPool.getObjectOrNull(LocationType.class, ref);
    if (type == null) {
      throw new ObjectUnknownException(ref);
    }
    LocationType previousState = type.clone();
    type.removeAllowedOperation(operation);
    objectPool.emitObjectEvent(type.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return type;
  }

  /**
   * Removes a location type.
   *
   * @param ref A reference to the location type to be removed.
   * @return The removed location type.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @deprecated Use {@link #clear()} instead.
   */
  @Deprecated
  public LocationType removeLocationType(TCSObjectReference<LocationType> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    LocationType type = objectPool.getObjectOrNull(LocationType.class, ref);
    if (type == null) {
      throw new ObjectUnknownException(ref);
    }
    // XXX Check if any locations of this type still exist, first.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               type.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return type;
  }

  /**
   * Creates a new location with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The ID of the newly created location. If <code>null</code>,
   * a new, unique one will be generated.
   * @param typeRef The location type the location will belong to.
   * @return The newly created location.
   * @throws ObjectUnknownException If the referenced location type does not
   * exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public Location createLocation(Integer objectID,
                                 TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    LocationType type = objectPool.getObjectOrNull(LocationType.class, typeRef);
    if (type == null) {
      throw new ObjectUnknownException(typeRef);
    }
    // Get a unique ID and name for the new location and create an instance.
    int locID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String locationName = objectPool.getUniqueObjectName("Location-", "0000");
    Location newLocation
        = new Location(locID, locationName, type.getReference());
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newLocation);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newLocation.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created point.
    return newLocation;
  }

  /**
   * Creates a new location with a unique name and all other attributes set to
   * default values.
   *
   * @param to The transfer object from which to create the new location type.
   * @return The newly created location.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  @SuppressWarnings("deprecation")
  public Location createLocation(LocationCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    LocationType type = objectPool.getObject(LocationType.class, to.getTypeName());
    Location newLocation = new Location(to.getName(), type.getReference())
        .withPosition(to.getPosition())
        .withProperties(to.getProperties());

    Set<Location.Link> locationLinks = new HashSet<>();
    for (Map.Entry<String, Set<String>> linkEntry : to.getLinks().entrySet()) {
      Point point = objectPool.getObject(Point.class, linkEntry.getKey());
      Location.Link link = new Location.Link(newLocation.getReference(), point.getReference())
          .withAllowedOperations(linkEntry.getValue());
      locationLinks.add(link);
    }
    newLocation = newLocation.withAttachedLinks(locationLinks);

    objectPool.addObject(newLocation);
    objectPool.emitObjectEvent(newLocation.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);

    // Add the location's links to the respective points, too.
    for (Location.Link link : locationLinks) {
      Point point = objectPool.getObjectOrNull(Point.class, link.getPoint());

      Set<Location.Link> pointLinks = new HashSet<>(point.getAttachedLinks());
      pointLinks.add(link);

      Point previousPointState = point.clone();
      point = objectPool.replaceObject(point.withAttachedLinks(pointLinks));

      objectPool.emitObjectEvent(point.clone(),
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }

    return newLocation;
  }

  /**
   * Returns the referenced location.
   *
   * @param ref A reference to the location to be returned.
   * @return The referenced location, or <code>null</code>, if no such location
   * exists in this pool.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Location getLocation(TCSObjectReference<Location> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Location.class, ref);
  }

  /**
   * Returns the location with the given name.
   *
   * @param locName The name of the location to return.
   * @return The location with the given name, or <code>null</code>, if no such
   * location exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Location getLocation(String locName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Location.class, locName);
  }

  /**
   * Returns a set of locations whose names match the given regular expression.
   *
   * @param regexp The regular expression describing the names of the locations
   * to return. If <code>null</code>, all locations are returned.
   * @return A set of locations whose names match the given regular expression.
   * If no such locations exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<Location> getLocations(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(Location.class, regexp);
  }

  /**
   * Sets the physical coordinates of a given location.
   *
   * @param ref A reference to the location to be modified.
   * @param position The location's new coordinates.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public Location setLocationPosition(TCSObjectReference<Location> ref,
                                      Triple position)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, ref);
    if (location == null) {
      throw new ObjectUnknownException(ref);
    }
    Location previousState = location.clone();
    location.setPosition(position);
    objectPool.emitObjectEvent(location.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Sets a location's type.
   *
   * @param ref A reference to the location to be modified.
   * @param typeRef The location's new type.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location or name do not
   * exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public Location setLocationType(TCSObjectReference<Location> ref,
                                  TCSObjectReference<LocationType> typeRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, ref);
    if (location == null) {
      throw new ObjectUnknownException(ref);
    }
    LocationType type = objectPool.getObjectOrNull(LocationType.class, typeRef);
    if (type == null) {
      throw new ObjectUnknownException(typeRef);
    }
    Location previousState = location.clone();
    location.setType(type.getReference());
    objectPool.emitObjectEvent(location.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Connects a location to a point.
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @return The modified location.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public Location connectLocationToPoint(TCSObjectReference<Location> locRef,
                                         TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObjectOrNull(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    Location.Link newLink
        = new Location.Link(location.getReference(), point.getReference());
    location.attachLink(newLink);
    point.attachLink(newLink);
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Disconnects a location from a point.
   *
   * @param locRef A reference to the location.
   * @param pointRef A reference to the point.
   * @return The modified location.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public Location disconnectLocationFromPoint(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObjectOrNull(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    location.detachLink(point.getReference());
    point.detachLink(location.getReference());
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Adds an allowed operation to a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be added.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public void addLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObjectOrNull(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    // Get the link between the point and location, if any exists.
    Location.Link referredLink = null;
    for (Location.Link curLink : location.getAttachedLinks()) {
      if (point.getReference().equals(curLink.getPoint())) {
        referredLink = curLink;
        break;
      }
    }
    if (referredLink == null) {
      throw new ObjectUnknownException("Described link not in this model");
    }
    referredLink.addAllowedOperation(operation);
    // Emit an event for both the location and the point end of the link.
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    // XXX Do we want to return anything here?
  }

  /**
   * Removes an allowed operation from a link between a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @param operation The operation to be removed.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public void removeLocationLinkAllowedOperation(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef,
      String operation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObjectOrNull(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    // Get the link between the point and location, if any exists.
    Location.Link referredLink = null;
    for (Location.Link curLink : location.getAttachedLinks()) {
      if (curLink.getPoint().equals(point.getReference())) {
        referredLink = curLink;
        break;
      }
    }
    if (referredLink == null) {
      throw new ObjectUnknownException("Described link not in this model");
    }
    // Emit an event for both the location and the point end of the link.
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    // XXX Do we want to return anything here?
  }

  /**
   * Removes all allowed operations (for all vehicle types) from a link between
   * a location and a point.
   *
   * @param locRef A reference to the location end of the link to be modified.
   * @param pointRef A reference to the point end of the link to be modified.
   * @throws ObjectUnknownException If any of the referenced objects does not
   * exist.
   * @deprecated Use {@link #createLocation(org.opentcs.access.to.LocationCreationTO)} instead.
   */
  @Deprecated
  public void clearLocationLinkAllowedOperations(
      TCSObjectReference<Location> locRef, TCSObjectReference<Point> pointRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, locRef);
    if (location == null) {
      throw new ObjectUnknownException(locRef);
    }
    Location previousLocationState = location.clone();
    Point point = objectPool.getObjectOrNull(Point.class, pointRef);
    if (point == null) {
      throw new ObjectUnknownException(pointRef);
    }
    Point previousPointState = point.clone();
    // Get the link between the point and location, if any exists.
    Location.Link referredLink = null;
    for (Location.Link curLink : location.getAttachedLinks()) {
      if (curLink.getPoint().equals(point.getReference())) {
        referredLink = curLink;
        break;
      }
    }
    if (referredLink == null) {
      throw new ObjectUnknownException("Described link not in this model");
    }
    referredLink.clearAllowedOperations();
    // Emit an event for both the location and the point end of the link.
    objectPool.emitObjectEvent(location.clone(),
                               previousLocationState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    objectPool.emitObjectEvent(point.clone(),
                               previousPointState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    // XXX Do we want to return anything here?
  }

  /**
   * Removes a location.
   *
   * @param ref A reference to the location to be removed.
   * @return The removed location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   * @deprecated Use {@link #clear()} instead.
   */
  @Deprecated
  public Location removeLocation(TCSObjectReference<Location> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Location location = objectPool.getObjectOrNull(Location.class, ref);
    if (location == null) {
      throw new ObjectUnknownException(ref);
    }
    // XXX Check if there are links pointing to this location, first.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               location.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return location;
  }

  /**
   * Creates a new vehicle with a unique name and all other attributes set to
   * their default values.
   *
   * @param objectID The ID of the newly created vehicle. If <code>null</code>,
   * a new, unique one will be generated.
   * @return The newly created vehicle.
   * @throws ObjectUnknownException If the referenced vehicle type is not in
   * this model.
   * @deprecated Use {@link #createVehicle(org.opentcs.access.to.VehicleCreationTO)} instead.
   */
  @Deprecated
  public Vehicle createVehicle(Integer objectID)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    int vehicleID
        = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String vehicleName = objectPool.getUniqueObjectName("Vehicle-", "00");
    Vehicle newVehicle = new Vehicle(vehicleID, vehicleName);
    try {
      objectPool.addObject(newVehicle);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newVehicle.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newVehicle;
  }

  /**
   * Creates a new vehicle with a unique name and all other attributes set to
   * their default values.
   *
   * @param to The transfer object from which to create the new group.
   * @return The newly created group.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   */
  @SuppressWarnings("deprecation")
  public Vehicle createVehicle(VehicleCreationTO to)
      throws ObjectExistsException {
    Vehicle newVehicle = new Vehicle(to.getName())
        .withLength(to.getLength())
        .withEnergyLevelGood(to.getEnergyLevelGood())
        .withEnergyLevelCritical(to.getEnergyLevelCritical())
        .withMaxVelocity(to.getMaxVelocity())
        .withMaxReverseVelocity(to.getMaxReverseVelocity())
        .withProperties(to.getProperties());
    objectPool.addObject(newVehicle);
    objectPool.emitObjectEvent(newVehicle.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newVehicle;
  }

  /**
   * Returns the referenced vehicle.
   *
   * @param ref A reference to the vehicle to be returned.
   * @return The referenced vehicle, or <code>null</code>, if no such vehicle
   * exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Vehicle getVehicle(TCSObjectReference<Vehicle> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Vehicle.class, ref);
  }

  /**
   * Returns the vehicle with the given name.
   *
   * @param vehicleName The name of the vehicle to return.
   * @return The vehicle with the given name, or <code>null</code>, if no
   * such vehicle exists.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Vehicle getVehicle(String vehicleName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Vehicle.class, vehicleName);
  }

  /**
   * Returns a set of vehicles whose names match the given regular expression.
   *
   * @param regexp The regular expression describing the names of the
   * vehicles to return. If <code>null</code>, all vehicles are returned.
   * @return A set of Vehicles whose names match the given regular expression.
   * If no such vehicles exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<Vehicle> getVehicles(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(Vehicle.class, regexp);
  }

  /**
   * Sets a vehicle's energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new energy level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                       int energyLevel)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withEnergyLevel(energyLevel));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's critical energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new critical energy level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #createVehicle(org.opentcs.access.to.VehicleCreationTO)} instead.
   */
  @Deprecated
  public Vehicle setVehicleEnergyLevelCritical(TCSObjectReference<Vehicle> ref,
                                               int energyLevel)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withEnergyLevelCritical(energyLevel));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's good energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new good energy level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #createVehicle(org.opentcs.access.to.VehicleCreationTO)} instead.
   */
  @Deprecated
  public Vehicle setVehicleEnergyLevelGood(TCSObjectReference<Vehicle> ref,
                                           int energyLevel)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withEnergyLevelGood(energyLevel));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's recharge operation.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param rechargeOperation The vehicle's new recharge operation.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                             String rechargeOperation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withRechargeOperation(rechargeOperation));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's load handling devices.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param devices The vehicle's new load handling devices.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                               List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withLoadHandlingDevices(devices));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's maximum velocity (in mm/s).
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum velocity.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #createVehicle(org.opentcs.access.to.VehicleCreationTO)} instead.
   */
  @Deprecated
  public Vehicle setVehicleMaxVelocity(TCSObjectReference<Vehicle> ref,
                                       int velocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withMaxVelocity(velocity));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's maximum reverse velocity.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param velocity The vehicle's new maximum reverse velocity.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #createVehicle(org.opentcs.access.to.VehicleCreationTO)} instead.
   */
  @Deprecated
  public Vehicle setVehicleMaxReverseVelocity(TCSObjectReference<Vehicle> ref,
                                              int velocity)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withMaxReverseVelocity(velocity));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new state.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleState(TCSObjectReference<Vehicle> ref,
                                 Vehicle.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withState(newState));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle integration level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param integrationLevel The vehicle's new integration level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleIntegrationLevel(TCSObjectReference<Vehicle> ref,
                                            Vehicle.IntegrationLevel integrationLevel)
      throws ObjectUnknownException {
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withIntegrationLevel(integrationLevel));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's processing state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's new processing state.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                     Vehicle.ProcState newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withProcState(newState));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's communication adapter's state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newState The vehicle's communication adapter's new state.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated VehicleCommAdapter.State is deprecated.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Vehicle setVehicleAdapterState(TCSObjectReference<Vehicle> ref,
                                        VehicleCommAdapter.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withAdapterState(newState));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's length.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param length The vehicle's new length.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #createVehicle(org.opentcs.access.to.VehicleCreationTO)} instead.
   */
  @Deprecated
  public Vehicle setVehicleLength(TCSObjectReference<Vehicle> ref, int length)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withLength(length));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  @SuppressWarnings("deprecation")
  public Vehicle setVehicleProcessableCategories(TCSObjectReference<Vehicle> ref,
                                                 Set<String> processableCategories)
      throws ObjectUnknownException {
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withProcessableCategories(processableCategories));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's position.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newPosRef A reference to the point the vehicle is occupying.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehiclePosition(TCSObjectReference<Vehicle> ref,
                                    TCSObjectReference<Point> newPosRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousVehicleState = vehicle.clone();
    // If the vehicle was occupying a point before, clear it and send an event.
    if (vehicle.getCurrentPosition() != null) {
      Point oldVehiclePos = objectPool.getObject(Point.class, vehicle.getCurrentPosition());
      Point previousPointState = oldVehiclePos.clone();
      oldVehiclePos = objectPool.replaceObject(oldVehiclePos.withOccupyingVehicle(null));
      objectPool.emitObjectEvent(oldVehiclePos.clone(),
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    // If the vehicle is occupying a point now, set that and send an event.
    if (newPosRef != null) {
      Point newVehiclePos = objectPool.getObject(Point.class, newPosRef);
      Point previousPointState = newVehiclePos.clone();
      newVehiclePos = objectPool.replaceObject(newVehiclePos.withOccupyingVehicle(ref));
      objectPool.emitObjectEvent(newVehiclePos.clone(),
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    vehicle = objectPool.replaceObject(vehicle.withCurrentPosition(newPosRef));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousVehicleState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);

    return vehicle;
  }

  /**
   * Sets a vehicle's next position.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newPosition A reference to the point the vehicle is expected to
   * occupy next.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleNextPosition(TCSObjectReference<Vehicle> ref,
                                        TCSObjectReference<Point> newPosition)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withNextPosition(newPosition));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's precise position.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newPosition The vehicle's precise position.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehiclePrecisePosition(TCSObjectReference<Vehicle> ref,
                                           Triple newPosition)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withPrecisePosition(newPosition));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's current orientation angle.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param angle The vehicle's orientation angle.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleOrientationAngle(TCSObjectReference<Vehicle> ref,
                                            double angle)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withOrientationAngle(angle));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param orderRef A reference to the transport order the vehicle processes.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                          TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle.clone();
    if (orderRef == null) {
      vehicle = objectPool.replaceObject(vehicle.withTransportOrder(null));
    }
    else {
      TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
      vehicle = objectPool.replaceObject(vehicle.withTransportOrder(order.getReference()));
    }
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's order sequence.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param seqRef A reference to the order sequence the vehicle processes.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                         TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle.clone();
    if (seqRef == null) {
      vehicle = objectPool.replaceObject(vehicle.withOrderSequence(null));
    }
    else {
      OrderSequence seq = objectPool.getObject(OrderSequence.class, seqRef);
      vehicle = objectPool.replaceObject(vehicle.withOrderSequence(seq.getReference()));
    }
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's index of the last route step travelled for the current
   * drive order of its current transport order.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param index The new index.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  @SuppressWarnings("deprecation")
  public Vehicle setVehicleRouteProgressIndex(TCSObjectReference<Vehicle> vehicleRef,
                                              int index)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle.clone();
    vehicle = objectPool.replaceObject(vehicle.withRouteProgressIndex(index));
    objectPool.emitObjectEvent(vehicle.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Removes a vehicle.
   *
   * @param ref A reference to the vehicle to be removed.
   * @return The removed vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @deprecated Use {@link #clear()} instead.
   */
  @Deprecated
  public Vehicle removeVehicle(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               vehicle.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return vehicle;
  }

  /**
   * Creates a new block with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The object ID of the newly created block. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created block.
   * @deprecated Use {@link #createBlock(org.opentcs.access.to.BlockCreationTO)} instead.
   */
  @Deprecated
  public Block createBlock(Integer objectID) {
    LOG.debug("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int blockID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String blockName = objectPool.getUniqueObjectName("Block-", "0000");
    Block newBlock = new Block(blockID, blockName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newBlock);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newBlock.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created block.
    return newBlock;
  }

  /**
   * Creates a new block with a unique name and all other attributes set to
   * default values.
   *
   * @param to The transfer object from which to create the new block.
   * @return The newly created block.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  @SuppressWarnings("deprecation")
  public Block createBlock(BlockCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    Set<TCSResourceReference<?>> members = new HashSet<>();
    for (String memberName : to.getMemberNames()) {
      TCSObject<?> object = objectPool.getObject(memberName);
      if (!(object instanceof TCSResource)) {
        throw new ObjectUnknownException(memberName);
      }
      members.add(((TCSResource) object).getReference());
    }
    Block newBlock = new Block(to.getName())
        .withMembers(members)
        .withProperties(to.getProperties());
    objectPool.addObject(newBlock);
    objectPool.emitObjectEvent(newBlock.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created block.
    return newBlock;
  }

  /**
   * Returns the block belonging to the given reference.
   *
   * @param ref A reference to the block to return.
   * @return The referenced block, if it exists, or <code>null</code>, if it
   * doesn't.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Block getBlock(TCSObjectReference<Block> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Block.class, ref);
  }

  /**
   * Returns the block with the given name.
   *
   * @param blockName The name of the block to return.
   * @return The block with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Block getBlock(String blockName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(Block.class, blockName);
  }

  /**
   * Returns a set of blocks whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the blocks returned. If
   * <code>null</code>, all points will be returned.
   * @return A set of blocks whose names match the given regular expression. If
   * no such blocks exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<Block> getBlocks(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(Block.class, regexp);
  }

  /**
   * Adds a member to a block.
   *
   * @param ref A reference to the block to be modified.
   * @param newMemberRef A reference to the new member.
   * @return The modified block.
   * @throws ObjectUnknownException If any of the referenced block or member do
   * not exist.
   * @deprecated Use {@link #createBlock(org.opentcs.access.to.BlockCreationTO)} instead.
   */
  @Deprecated
  public Block addBlockMember(TCSObjectReference<Block> ref,
                              TCSResourceReference<?> newMemberRef)
      throws ObjectUnknownException {
    Block block = objectPool.getObjectOrNull(Block.class, ref);
    if (block == null) {
      throw new ObjectUnknownException(ref);
    }
    Block previousState = block.clone();
    TCSObject<?> object = objectPool.getObjectOrNull(newMemberRef);
    if (!(object instanceof TCSResource)) {
      throw new ObjectUnknownException(ref);
    }
    TCSResourceReference<?> memberRef = ((TCSResource) object).getReference();
    block.addMember(memberRef);
    objectPool.emitObjectEvent(block.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return block;
  }

  /**
   * Removes a member from a block.
   *
   * @param ref A reference to the block to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @return The modified block.
   * @throws ObjectUnknownException If the referenced block does not exist.
   * @deprecated Use {@link #createBlock(org.opentcs.access.to.BlockCreationTO)} instead.
   */
  @Deprecated
  public Block removeBlockMember(TCSObjectReference<Block> ref,
                                 TCSResourceReference<?> rmMemberRef)
      throws ObjectUnknownException {
    Block block = objectPool.getObjectOrNull(Block.class, ref);
    if (block == null) {
      throw new ObjectUnknownException(ref);
    }
    Block previousState = block.clone();
    block.removeMember(rmMemberRef);
    objectPool.emitObjectEvent(block.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return block;
  }

  /**
   * Removes a block.
   *
   * @param ref A reference to the block to be removed.
   * @return The removed block.
   * @throws ObjectUnknownException If the referenced block does not exist.
   * @deprecated Use {@link #clear()} instead.
   */
  @Deprecated
  public Block removeBlock(TCSObjectReference<Block> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Block block = objectPool.getObjectOrNull(Block.class, ref);
    if (block == null) {
      throw new ObjectUnknownException(ref);
    }
    // Remove the block.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               block.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return block;
  }

  /**
   * Creates a new group with a unique name and all other attributes set to
   * default values.
   *
   * @param objectID The object ID of the newly created group. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created group.
   * @deprecated Use {@link #createGroup(org.opentcs.access.to.GroupCreationTO)} instead.
   */
  @Deprecated
  public Group createGroup(Integer objectID) {
    LOG.debug("method entry");
    // Get a unique ID and name for the new point and create an instance.
    int groupID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String groupName = objectPool.getUniqueObjectName("Group-", "0000");
    Group newGroup = new Group(groupID, groupName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newGroup);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newGroup.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created group.
    return newGroup;
  }

  /**
   * Creates a new group with a unique name and all other attributes set to
   * default values.
   *
   * @param to The transfer object from which to create the new group.
   * @return The newly created group.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  @SuppressWarnings("deprecation")
  public Group createGroup(GroupCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    Set<TCSObjectReference<?>> members = new HashSet<>();
    for (String memberName : to.getMemberNames()) {
      TCSObject<?> object = objectPool.getObject(memberName);
      if (object == null) {
        throw new ObjectUnknownException(memberName);
      }
      members.add(object.getReference());
    }
    Group newGroup = new Group(to.getName())
        .withMembers(members)
        .withProperties(to.getProperties());
    objectPool.addObject(newGroup);
    objectPool.emitObjectEvent(newGroup.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created group.
    return newGroup;
  }

  /**
   * Returns a set of groups whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the groups returned. If
   * <code>null</code>, all points will be returned.
   * @return A set of groups whose names match the given regular expression. If
   * no such groups exist, the returned set is empty.
   * @deprecated Use methods in {@link TCSObjectPool} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Set<Group> getGroups(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(Group.class, regexp);
  }

  /**
   * Adds a member to a group.
   *
   * @param ref A reference to the group to be modified.
   * @param newMemberRef A reference to the new member.
   * @return The modified group.
   * @throws ObjectUnknownException If any of the referenced group or member do
   * not exist.
   * @deprecated Use {@link #createGroup(org.opentcs.access.to.GroupCreationTO)} instead.
   */
  @Deprecated
  public Group addGroupMember(TCSObjectReference<Group> ref,
                              TCSObjectReference<?> newMemberRef)
      throws ObjectUnknownException {
    Group group = objectPool.getObjectOrNull(Group.class, ref);
    if (group == null) {
      throw new ObjectUnknownException(ref);
    }
    Group previousState = group.clone();
    TCSObject<?> object = objectPool.getObjectOrNull(newMemberRef);
    if (object == null) {
      throw new ObjectUnknownException(newMemberRef);
    }
    group.addMember(object.getReference());
    objectPool.emitObjectEvent(group.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return group;
  }

  /**
   * Removes a member from a group.
   *
   * @param ref A reference to the group to be modified.
   * @param rmMemberRef A reference to the member to be removed.
   * @return The modified group.
   * @throws ObjectUnknownException If the referenced group does not exist.
   * @deprecated Use {@link #createGroup(org.opentcs.access.to.GroupCreationTO)} instead.
   */
  @Deprecated
  public Group removeGroupMember(TCSObjectReference<Group> ref,
                                 TCSObjectReference<?> rmMemberRef)
      throws ObjectUnknownException {
    Group group = objectPool.getObjectOrNull(Group.class, ref);
    if (group == null) {
      throw new ObjectUnknownException(ref);
    }
    Group previousState = group.clone();
    group.removeMember(rmMemberRef);
    objectPool.emitObjectEvent(group.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return group;
  }

  /**
   * Removes a group.
   *
   * @param ref A reference to the group to be removed.
   * @return The removed group.
   * @throws ObjectUnknownException If the referenced group does not exist.
   * @deprecated Use {@link #clear()} instead.
   */
  @Deprecated
  public Group removeGroup(TCSObjectReference<Group> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Group group = objectPool.getObjectOrNull(Group.class, ref);
    if (group == null) {
      throw new ObjectUnknownException(ref);
    }
    // Remove the block.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(null,
                               group.clone(),
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return group;
  }

  /**
   * Creates a new static route with a unique name and all other attributes set
   * to default values.
   *
   * @param objectID The object ID of the newly created route. If
   * <code>null</code>, a new, unique one will be generated.
   * @return The newly created static route.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public org.opentcs.data.model.StaticRoute createStaticRoute(Integer objectID) {
    LOG.debug("method entry");
    // Get a unique ID and name for the new object and create an instance.
    int routeID = objectID != null ? objectID : objectPool.getUniqueObjectId();
    String routeName = objectPool.getUniqueObjectName("Route-", "0000");
    org.opentcs.data.model.StaticRoute newRoute = new org.opentcs.data.model.StaticRoute(routeID,
                                                                                         routeName);
    // Store the instance in the global object pool.
    try {
      objectPool.addObject(newRoute);
    }
    catch (ObjectExistsException exc) {
      LOG.error("Allegedly unique object ID/name already exists", exc);
      throw new IllegalStateException(
          "Allegedly unique object ID/name already exists", exc);
    }
    objectPool.emitObjectEvent(newRoute.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created route.
    return newRoute;
  }

  /**
   * Creates a new static route with a unique name and all other attributes set
   * to default values.
   *
   * @param to The transfer object from which to create the new route.
   * @return The newly created route.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public org.opentcs.data.model.StaticRoute createStaticRoute(
      org.opentcs.access.to.model.StaticRouteCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    List<TCSObjectReference<Point>> hops = new LinkedList<>();
    for (String memberName : to.getHopNames()) {
      TCSObject<?> object = objectPool.getObjectOrNull(memberName);
      if (!(object instanceof Point)) {
        throw new ObjectUnknownException(memberName);
      }
      hops.add(((Point) object).getReference());
    }
    org.opentcs.data.model.StaticRoute newRoute
        = new org.opentcs.data.model.StaticRoute(to.getName())
            .withHops(hops)
            .withProperties(to.getProperties());
    objectPool.addObject(newRoute);
    objectPool.emitObjectEvent(newRoute.clone(),
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created route.
    return newRoute;
  }

  /**
   * Returns the route belonging to the given reference.
   *
   * @param ref A reference to the route to return.
   * @return The referenced route, if it exists, or <code>null</code>, if it
   * doesn't.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public org.opentcs.data.model.StaticRoute getStaticRoute(
      TCSObjectReference<org.opentcs.data.model.StaticRoute> ref) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(org.opentcs.data.model.StaticRoute.class, ref);
  }

  /**
   * Returns the route with the given name.
   *
   * @param routeName The name of the route to return.
   * @return The route with the given name, if it exists, or <code>null</code>,
   * if it doesn't.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public org.opentcs.data.model.StaticRoute getStaticRoute(String routeName) {
    LOG.debug("method entry");
    return objectPool.getObjectOrNull(org.opentcs.data.model.StaticRoute.class, routeName);
  }

  /**
   * Returns a set of routes whose names match the given regular expression.
   *
   * @param regexp The regular expression selecting the routes returned. If
   * <code>null</code>, all routes will be returned.
   * @return A set of routes whose names match the given regular expression. If
   * no such routes exist, the returned set is empty.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public Set<org.opentcs.data.model.StaticRoute> getStaticRoutes(Pattern regexp) {
    LOG.debug("method entry");
    return objectPool.getObjects(org.opentcs.data.model.StaticRoute.class, regexp);
  }

  /**
   * Adds a static route hop.
   *
   * @param routeRef A reference to the route
   * @param newHopRef A reference to the new hop
   * @return The new static route
   * @throws ObjectUnknownException If a parameter is unknown
   * @deprecated Use {@link #createStaticRoute(org.opentcs.access.to.StaticRouteCreationTO)}
   * instead.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public org.opentcs.data.model.StaticRoute addStaticRouteHop(
      TCSObjectReference<org.opentcs.data.model.StaticRoute> routeRef,
      TCSObjectReference<Point> newHopRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    org.opentcs.data.model.StaticRoute route
        = objectPool.getObjectOrNull(org.opentcs.data.model.StaticRoute.class, routeRef);
    if (route == null) {
      throw new ObjectUnknownException(routeRef);
    }
    org.opentcs.data.model.StaticRoute previousState = route.clone();
    Point point = objectPool.getObjectOrNull(Point.class, newHopRef);
    if (point == null) {
      throw new ObjectUnknownException(newHopRef);
    }
    route.addHop(point.getReference());
    objectPool.emitObjectEvent(route.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return route;
  }

  /**
   * Clears all static route hops in the given route.
   *
   * @param routeRef A reference to the route to be cleared
   * @return The modifired static route
   * @throws ObjectUnknownException If a parameter is unknown
   * @deprecated Use {@link #createStaticRoute(org.opentcs.access.to.StaticRouteCreationTO)}
   * instead.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public org.opentcs.data.model.StaticRoute clearStaticRouteHops(
      TCSObjectReference<org.opentcs.data.model.StaticRoute> routeRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    org.opentcs.data.model.StaticRoute route
        = objectPool.getObjectOrNull(org.opentcs.data.model.StaticRoute.class, routeRef);
    if (route == null) {
      throw new ObjectUnknownException(routeRef);
    }
    org.opentcs.data.model.StaticRoute previousState = route.clone();
    route.clearHops();
    objectPool.emitObjectEvent(route.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return route;
  }

  /**
   * Removes a static route.
   *
   * @param ref A reference to the block to be removed.
   * @return The removed block.
   * @throws ObjectUnknownException If the referenced block does not exist.
   * @deprecated Use {@link #clear()} instead.
   * @deprecated Support for static routes will be removed.
   */
  @Deprecated
  public org.opentcs.data.model.StaticRoute removeStaticRoute(
      TCSObjectReference<org.opentcs.data.model.StaticRoute> ref)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    org.opentcs.data.model.StaticRoute route
        = objectPool.getObjectOrNull(org.opentcs.data.model.StaticRoute.class, ref);
    if (route == null) {
      throw new ObjectUnknownException(ref);
    }
    org.opentcs.data.model.StaticRoute previousState = route.clone();
    // Remove the block.
    objectPool.removeObject(ref);
    objectPool.emitObjectEvent(route.clone(),
                               previousState,
                               TCSObjectEvent.Type.OBJECT_REMOVED);
    return route;
  }

  /**
   * Expands a set of resources <em>A</em> to a set of resources <em>B</em>.
   * <em>B</em> contains the resources in <em>A</em> with blocks expanded to
   * their actual members.
   * The given set is not modified.
   *
   * @param resources The set of resources to be expanded.
   * @return The given set with resources expanded.
   * @throws ObjectUnknownException If an object referenced in the given set
   * does not exist.
   */
  public Set<TCSResource<?>> expandResources(Set<TCSResourceReference<?>> resources)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Set<TCSResource<?>> result = new HashSet<>();
    Set<Block> blocks = getBlocks(null);
    for (TCSResourceReference<?> curRef : resources) {
      TCSObject<?> object = objectPool.getObject(curRef);
      TCSResource<?> resource = (TCSResource<?>) object;
      result.add(resource);
      for (Block curBlock : blocks) {
        // If the current block contains the resource, add all of the block's
        // members to the result.
        if (curBlock.getMembers().contains(resource.getReference())) {
          for (TCSResourceReference<?> curResRef : curBlock.getMembers()) {
            TCSResource<?> member = (TCSResource<?>) objectPool.getObject(curResRef);
            result.add(member);
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns an informational string describing this model's topology.
   *
   * @return An informational string describing this model's topology.
   */
  public String getInfo() {
    StringBuilder result = new StringBuilder();
    Set<Point> points = new TreeSet<>(Comparators.objectsByName());
    Set<Path> paths = new TreeSet<>(Comparators.objectsByName());
    Set<LocationType> locationTypes = new TreeSet<>(Comparators.objectsByName());
    Set<Location> locations = new TreeSet<>(Comparators.objectsByName());
    Set<Vehicle> vehicles = new TreeSet<>(Comparators.objectsByName());
    Set<TCSObject<?>> objects = objectPool.getObjects((Pattern) null);
    for (TCSObject<?> curObject : objects) {
      if (curObject instanceof Point) {
        points.add((Point) curObject);
      }
      else if (curObject instanceof Path) {
        paths.add((Path) curObject);
      }
      else if (curObject instanceof LocationType) {
        locationTypes.add((LocationType) curObject);
      }
      else if (curObject instanceof Location) {
        locations.add((Location) curObject);
      }
      else if (curObject instanceof Vehicle) {
        vehicles.add((Vehicle) curObject);
      }
    }
    result.append("Model data:\n");
    result.append(" Name: " + name + "\n");
    result.append("Points:\n");
    for (Point curPoint : points) {
      result.append(" Point:\n");
      result.append("  Name: " + curPoint.getName() + "\n");
      result.append("  Type: " + curPoint.getType() + "\n");
      result.append("  X: " + curPoint.getPosition().getX() + "\n");
      result.append("  Y: " + curPoint.getPosition().getY() + "\n");
      result.append("  Z: " + curPoint.getPosition().getZ() + "\n");
    }
    result.append("Paths:\n");
    for (Path curPath : paths) {
      result.append(" Path:\n");
      result.append("  Name: " + curPath.getName() + "\n");
      result.append("  Source: " + curPath.getSourcePoint().getName() + "\n");
      result.append("  Destination: " + curPath.getDestinationPoint().getName() + "\n");
      result.append("  Length: " + curPath.getLength() + "\n");
    }
    result.append("LocationTypes:\n");
    for (LocationType curType : locationTypes) {
      result.append(" LocationType:\n");
      result.append("  Name: " + curType.getName() + "\n");
      result.append("  Operations: "
          + curType.getAllowedOperations().toString() + "\n");
    }
    result.append("Locations:\n");
    for (Location curLocation : locations) {
      result.append(" Location:\n");
      result.append("  Name: " + curLocation.getName() + "\n");
      result.append("  Type: " + curLocation.getType().getName() + "\n");
      for (Location.Link curLink : curLocation.getAttachedLinks()) {
        result.append("  Link:\n");
        result.append("   Point: " + curLink.getPoint().getName() + "\n");
        result.append("   Allowed operations:" + curLink.getAllowedOperations() + "\n");
      }
    }
    result.append("Vehicles:\n");
    for (Vehicle curVehicle : vehicles) {
      result.append(" Vehicle:\n");
      result.append("  Name: " + curVehicle.getName() + "\n");
      result.append("  Length: " + curVehicle.getLength());
    }
    return result.toString();
  }
}
