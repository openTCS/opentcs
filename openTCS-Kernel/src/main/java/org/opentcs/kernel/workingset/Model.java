/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

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
import java.util.stream.Collectors;
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
import org.opentcs.data.model.visualization.ImageLayoutElement;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.ShapeLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.util.Comparators;
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
  public void createPlantModelObjects(PlantModelCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    clear();
    setName(to.getName());
    setProperties(to.getProperties());

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
    for (VehicleCreationTO vehicle : to.getVehicles()) {
      createVehicle(vehicle);
    }
    for (VisualLayoutCreationTO layout : to.getVisualLayouts()) {
      createVisualLayout(layout);
    }
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
    objectPool.emitObjectEvent(newLayout,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created layout.
    return newLayout;
  }

  /**
   * Creates a new point with a unique name and all other attributes set to
   * default values.
   *
   * @param to The transfer object from which to create the new point.
   * @return The newly created point.
   * @throws ObjectExistsException If an object with the point's name already exists.
   */
  public Point createPoint(PointCreationTO to)
      throws ObjectExistsException {
    // Get a unique ID for the new point and create an instance.
    Point newPoint = new Point(to.getName())
        .withPosition(to.getPosition())
        .withType(to.getType())
        .withVehicleOrientationAngle(to.getVehicleOrientationAngle())
        .withProperties(to.getProperties());
    objectPool.addObject(newPoint);
    objectPool.emitObjectEvent(newPoint, null, TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created point.
    return newPoint;
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
  private Point addPointIncomingPath(TCSObjectReference<Point> pointRef,
                                     TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    Path path = objectPool.getObject(Path.class, pathRef);
    // Check if the point really is the path's destination point.
    if (!path.getDestinationPoint().equals(point.getReference())) {
      throw new IllegalArgumentException("Point is not the path's destination.");
    }
    Path previousState = path;
    Set<TCSObjectReference<Path>> incomingPaths = new HashSet<>(point.getIncomingPaths());
    incomingPaths.add(path.getReference());
    point = objectPool.replaceObject(point.withIncomingPaths(incomingPaths));
    objectPool.emitObjectEvent(point,
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
  private Point removePointIncomingPath(TCSObjectReference<Point> pointRef,
                                        TCSObjectReference<Path> pathRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Point point = objectPool.getObject(Point.class, pointRef);
    Path path = objectPool.getObject(Path.class, pathRef);
    Path previousState = path;
    Set<TCSObjectReference<Path>> incomingPaths = new HashSet<>(point.getIncomingPaths());
    incomingPaths.remove(path.getReference());
    point = objectPool.replaceObject(point.withIncomingPaths(incomingPaths));
    objectPool.emitObjectEvent(point,
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
    Path previousState = path;
    Set<TCSObjectReference<Path>> outgoingPaths = new HashSet<>(point.getOutgoingPaths());
    outgoingPaths.add(path.getReference());
    point = objectPool.replaceObject(point.withOutgoingPaths(outgoingPaths));
    objectPool.emitObjectEvent(point,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  /**
   * Creates a new path from the given transfer object.
   *
   * @param to The transfer object from which to create the new path.
   * @return The newly created path.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws ObjectExistsException If an object with the same name as the path already exists.
   */
  public Path createPath(PathCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    requireNonNull(to, "to");

    Point srcPoint = objectPool.getObject(Point.class, to.getSrcPointName());
    Point destPoint = objectPool.getObject(Point.class, to.getDestPointName());
    Path newPath = new Path(to.getName(),
                            srcPoint.getReference(),
                            destPoint.getReference())
        .withLength(to.getLength())
        .withMaxVelocity(to.getMaxVelocity())
        .withMaxReverseVelocity(to.getMaxReverseVelocity())
        .withProperties(to.getProperties())
        .withLocked(to.isLocked());

    // Store the instance in the global object pool.
    objectPool.addObject(newPath);

    objectPool.emitObjectEvent(newPath,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);

    addPointOutgoingPath(srcPoint.getReference(), newPath.getReference());
    addPointIncomingPath(destPoint.getReference(), newPath.getReference());

    return newPath;
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
  public Path setPathLocked(TCSObjectReference<Path> ref, boolean newLocked)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Path path = objectPool.getObject(Path.class, ref);
    Path previousState = path;
    path = objectPool.replaceObject(path.withLocked(newLocked));
    objectPool.emitObjectEvent(path,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Creates a new location type with a unique name and all other attributes set
   * to their default values.
   *
   * @param to The transfer object from which to create the new location type.
   * @return The newly created location type.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   */
  public LocationType createLocationType(LocationTypeCreationTO to)
      throws ObjectExistsException {
    LocationType newType = new LocationType(to.getName())
        .withAllowedOperations(to.getAllowedOperations())
        .withProperties(to.getProperties());
    objectPool.addObject(newType);
    objectPool.emitObjectEvent(newType,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newType;
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
    objectPool.emitObjectEvent(newLocation,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);

    // Add the location's links to the respective points, too.
    for (Location.Link link : locationLinks) {
      Point point = objectPool.getObjectOrNull(Point.class, link.getPoint());

      Set<Location.Link> pointLinks = new HashSet<>(point.getAttachedLinks());
      pointLinks.add(link);

      Point previousPointState = point;
      point = objectPool.replaceObject(point.withAttachedLinks(pointLinks));

      objectPool.emitObjectEvent(point,
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }

    return newLocation;
  }

  /**
   * Creates a new vehicle with a unique name and all other attributes set to
   * their default values.
   *
   * @param to The transfer object from which to create the new group.
   * @return The newly created group.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   */
  public Vehicle createVehicle(VehicleCreationTO to)
      throws ObjectExistsException {
    Vehicle newVehicle = new Vehicle(to.getName())
        .withLength(to.getLength())
        .withEnergyLevelGood(to.getEnergyLevelGood())
        .withEnergyLevelCritical(to.getEnergyLevelCritical())
        .withEnergyLevelFullyRecharged(to.getEnergyLevelFullyRecharged())
        .withEnergyLevelSufficientlyRecharged(to.getEnergyLevelSufficientlyRecharged())
        .withMaxVelocity(to.getMaxVelocity())
        .withMaxReverseVelocity(to.getMaxReverseVelocity())
        .withProperties(to.getProperties());
    objectPool.addObject(newVehicle);
    objectPool.emitObjectEvent(newVehicle,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    return newVehicle;
  }

  /**
   * Sets a vehicle's energy level.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param energyLevel The vehicle's new energy level.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleEnergyLevel(TCSObjectReference<Vehicle> ref,
                                       int energyLevel)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withEnergyLevel(energyLevel));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleRechargeOperation(TCSObjectReference<Vehicle> ref,
                                             String rechargeOperation)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withRechargeOperation(rechargeOperation));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleLoadHandlingDevices(TCSObjectReference<Vehicle> ref,
                                               List<LoadHandlingDevice> devices)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withLoadHandlingDevices(devices));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleState(TCSObjectReference<Vehicle> ref,
                                 Vehicle.State newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withState(newState));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleIntegrationLevel(TCSObjectReference<Vehicle> ref,
                                            Vehicle.IntegrationLevel integrationLevel)
      throws ObjectUnknownException {
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withIntegrationLevel(integrationLevel));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleProcState(TCSObjectReference<Vehicle> ref,
                                     Vehicle.ProcState newState)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withProcState(newState));
    objectPool.emitObjectEvent(vehicle,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  public Vehicle setVehicleAllowedOrderTypes(TCSObjectReference<Vehicle> ref,
                                             Set<String> allowedOrderTypes)
      throws ObjectUnknownException {
    Vehicle vehicle = objectPool.getObjectOrNull(Vehicle.class, ref);
    if (vehicle == null) {
      throw new ObjectUnknownException(ref);
    }
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withAllowedOrderTypes(allowedOrderTypes));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehiclePosition(TCSObjectReference<Vehicle> ref,
                                    TCSObjectReference<Point> newPosRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousVehicleState = vehicle;
    // If the vehicle was occupying a point before, clear it and send an event.
    if (vehicle.getCurrentPosition() != null) {
      Point oldVehiclePos = objectPool.getObject(Point.class, vehicle.getCurrentPosition());
      Point previousPointState = oldVehiclePos;
      oldVehiclePos = objectPool.replaceObject(oldVehiclePos.withOccupyingVehicle(null));
      objectPool.emitObjectEvent(oldVehiclePos,
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    // If the vehicle is occupying a point now, set that and send an event.
    if (newPosRef != null) {
      Point newVehiclePos = objectPool.getObject(Point.class, newPosRef);
      Point previousPointState = newVehiclePos;
      newVehiclePos = objectPool.replaceObject(newVehiclePos.withOccupyingVehicle(ref));
      objectPool.emitObjectEvent(newVehiclePos,
                                 previousPointState,
                                 TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    vehicle = objectPool.replaceObject(vehicle.withCurrentPosition(newPosRef));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleNextPosition(TCSObjectReference<Vehicle> ref,
                                        TCSObjectReference<Point> newPosition)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withNextPosition(newPosition));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehiclePrecisePosition(TCSObjectReference<Vehicle> ref,
                                           Triple newPosition)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withPrecisePosition(newPosition));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleOrientationAngle(TCSObjectReference<Vehicle> ref,
                                            double angle)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, ref);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withOrientationAngle(angle));
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleTransportOrder(TCSObjectReference<Vehicle> vehicleRef,
                                          TCSObjectReference<TransportOrder> orderRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle;
    if (orderRef == null) {
      vehicle = objectPool.replaceObject(vehicle.withTransportOrder(null));
    }
    else {
      TransportOrder order = objectPool.getObject(TransportOrder.class, orderRef);
      vehicle = objectPool.replaceObject(vehicle.withTransportOrder(order.getReference()));
    }
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleOrderSequence(TCSObjectReference<Vehicle> vehicleRef,
                                         TCSObjectReference<OrderSequence> seqRef)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle;
    if (seqRef == null) {
      vehicle = objectPool.replaceObject(vehicle.withOrderSequence(null));
    }
    else {
      OrderSequence seq = objectPool.getObject(OrderSequence.class, seqRef);
      vehicle = objectPool.replaceObject(vehicle.withOrderSequence(seq.getReference()));
    }
    objectPool.emitObjectEvent(vehicle,
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
  public Vehicle setVehicleRouteProgressIndex(TCSObjectReference<Vehicle> vehicleRef,
                                              int index)
      throws ObjectUnknownException {
    LOG.debug("method entry");
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle;
    vehicle = objectPool.replaceObject(vehicle.withRouteProgressIndex(index));
    objectPool.emitObjectEvent(vehicle,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
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
        .withType(to.getType())
        .withMembers(members)
        .withProperties(to.getProperties());
    objectPool.addObject(newBlock);
    objectPool.emitObjectEvent(newBlock,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created block.
    return newBlock;
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
    objectPool.emitObjectEvent(newGroup,
                               null,
                               TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created group.
    return newGroup;
  }

  public PlantModelCreationTO createPlantModelCreationTO() {
    return new PlantModelCreationTO(name)
        .withProperties(getProperties())
        .withPoints(getPoints())
        .withPaths(getPaths())
        .withVehicles(getVehicles())
        .withLocationTypes(getLocationTypes())
        .withLocations(getLocations())
        .withBlocks(getBlocks())
        .withGroups(getGroups())
        .withVisualLayouts(getVisualLayouts());
  }

  /**
   * Returns a list of {@link PointCreationTO Points} for all points in a model.
   *
   * @return A list of {@link PointCreationTO Points} for all points in a model.
   */
  private List<PointCreationTO> getPoints() {
    Set<Point> points = objectPool.getObjects(Point.class);
    List<PointCreationTO> result = new ArrayList<>();

    for (Point curPoint : points) {
      result.add(
          new PointCreationTO(curPoint.getName())
              .withPosition(curPoint.getPosition())
              .withVehicleOrientationAngle(curPoint.getVehicleOrientationAngle())
              .withType(curPoint.getType())
              .withProperties(curPoint.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link PathCreationTO Paths} for all paths in a model.
   *
   * @param model The model data.
   * @return A list of {@link PathCreationTO Paths} for all paths in a model.
   */
  @SuppressWarnings("deprecation")
  private List<PathCreationTO> getPaths() {
    Set<Path> paths = objectPool.getObjects(Path.class);
    List<PathCreationTO> result = new ArrayList<>();

    for (Path curPath : paths) {
      result.add(
          new PathCreationTO(curPath.getName(),
                             curPath.getSourcePoint().getName(),
                             curPath.getDestinationPoint().getName())
              .withLength(curPath.getLength())
              .withMaxVelocity(curPath.getMaxVelocity())
              .withMaxReverseVelocity(curPath.getMaxReverseVelocity())
              .withLocked(curPath.isLocked())
              .withProperties(curPath.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link VehicleCreationTO Vehicles} for all vehicles in a model.
   *
   * @param model The model data.
   * @return A list of {@link VehicleCreationTO Vehicles} for all vehicles in a model.
   */
  private List<VehicleCreationTO> getVehicles() {
    Set<Vehicle> vehicles = objectPool.getObjects(Vehicle.class);
    List<VehicleCreationTO> result = new ArrayList<>();

    for (Vehicle curVehicle : vehicles) {
      result.add(
          new VehicleCreationTO(curVehicle.getName())
              .withLength(curVehicle.getLength())
              .withEnergyLevelGood(curVehicle.getEnergyLevelGood())
              .withEnergyLevelCritical(curVehicle.getEnergyLevelCritical())
              .withEnergyLevelFullyRecharged(curVehicle.getEnergyLevelFullyRecharged())
              .withEnergyLevelSufficientlyRecharged(curVehicle.getEnergyLevelSufficientlyRecharged())
              .withMaxVelocity(curVehicle.getMaxVelocity())
              .withMaxReverseVelocity(curVehicle.getMaxReverseVelocity())
              .withProperties(curVehicle.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link LocationTypeCreationTO LocationTypes} for all location types in a
   * model.
   *
   * @param model The model data.
   * @return A list of {@link LocationTypeCreationTO LocationTypes} for all location types in a
   * model.
   */
  private List<LocationTypeCreationTO> getLocationTypes() {
    Set<LocationType> locTypes = objectPool.getObjects(LocationType.class);
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationType curType : locTypes) {
      result.add(
          new LocationTypeCreationTO(curType.getName())
              .withAllowedOperations(curType.getAllowedOperations())
              .withProperties(curType.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link LocationCreationTO Locations} for all locations in a model.
   *
   * @param model The model data.
   * @return A list of {@link LocationCreationTO Locations} for all locations in a model.
   */
  private List<LocationCreationTO> getLocations() {
    Set<Location> locations = objectPool.getObjects(Location.class);
    List<LocationCreationTO> result = new ArrayList<>();

    for (Location curLoc : locations) {
      result.add(
          new LocationCreationTO(curLoc.getName(),
                                 curLoc.getType().getName(),
                                 curLoc.getPosition())
              .withLinks(curLoc.getAttachedLinks().stream()
                  .collect(Collectors.toMap(link -> link.getPoint().getName(),
                                            Location.Link::getAllowedOperations)))
              .withProperties(curLoc.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link BlockCreationTO Blocks} for all blocks in a model.
   *
   * @param model The model data.
   * @return A list of {@link BlockCreationTO Blocks} for all blocks in a model.
   */
  private List<BlockCreationTO> getBlocks() {
    Set<Block> blocks = objectPool.getObjects(Block.class);
    List<BlockCreationTO> result = new ArrayList<>();

    for (Block curBlock : blocks) {
      result.add(
          new BlockCreationTO(curBlock.getName())
              .withMemberNames(curBlock.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withType(curBlock.getType())
              .withProperties(curBlock.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link GroupCreationTO Groups} for all groups in a model.
   *
   * @param model The model data.
   * @return A list of {@link GroupCreationTO Groups} for all groups in a model.
   */
  private List<GroupCreationTO> getGroups() {
    Set<Group> groups = objectPool.getObjects(Group.class);
    List<GroupCreationTO> result = new ArrayList<>();

    for (Group curGroup : groups) {
      result.add(
          new GroupCreationTO(curGroup.getName())
              .withMemberNames(curGroup.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withProperties(curGroup.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link VisualLayoutCreationTO VisualLayouts} for all visual layouts in a
   * model.
   *
   * @param model The model data.
   * @return A list of {@link VisualLayoutCreationTO VisualLayouts} for all visual layouts in a
   * model.
   */
  private List<VisualLayoutCreationTO> getVisualLayouts() {
    Set<VisualLayout> layouts = objectPool.getObjects(VisualLayout.class);
    List<VisualLayoutCreationTO> result = new ArrayList<>();

    // Separate our various kinds of layout elements.
    for (VisualLayout curLayout : layouts) {
      List<ShapeLayoutElement> shapeLayoutElements = new LinkedList<>();
      Map<TCSObject<?>, ModelLayoutElement> modelLayoutElements = new HashMap<>();

      for (LayoutElement layoutElement : curLayout.getLayoutElements()) {
        if (layoutElement instanceof ShapeLayoutElement) {
          shapeLayoutElements.add((ShapeLayoutElement) layoutElement);
        }
        else if (layoutElement instanceof ImageLayoutElement) {
          // XXX Do something with these elements?
        }
        else if (layoutElement instanceof ModelLayoutElement) {
          // Map the result of getVisualizedObject() to the corresponding TCSObject, since the name
          // of the TCSObject might change but won't be changed in the reference the 
          // ModelLayoutElement holds.
          ModelLayoutElement mle = (ModelLayoutElement) layoutElement;
          TCSObject<?> vObj = objectPool.getObjectOrNull(mle.getVisualizedObject());
          // Don't persist layout elements for model elements that don't exist, but leave a log 
          // message in that case.
          if (vObj == null) {
            LOG.error("Visualized object {} does not exist (any more?), not persisting layout element",
                      mle.getVisualizedObject());
            continue;
          }
          modelLayoutElements.put(vObj, mle);
        }
        // XXX GroupLayoutElement is not implemented, yet.
//        else if (layoutElement instanceof GroupLayout)
      }

      // Persist ShapeLayoutElements
      List<ShapeLayoutElementCreationTO> slElements = new ArrayList<>();
      for (ShapeLayoutElement curSLE : shapeLayoutElements) {
        ShapeLayoutElementCreationTO slElement = new ShapeLayoutElementCreationTO("")
            .withLayer(curSLE.getLayer())
            .withProperties(curSLE.getProperties());

        slElements.add(slElement);
      }

      // Persist ModelLayoutElements
      List<ModelLayoutElementCreationTO> mlElements = new ArrayList<>();
      for (Map.Entry<TCSObject<?>, ModelLayoutElement> curMLE : modelLayoutElements.entrySet()) {
        ModelLayoutElementCreationTO mlElement = new ModelLayoutElementCreationTO(curMLE.getKey().getName())
            .withLayer(curMLE.getValue().getLayer())
            .withProperties(curMLE.getValue().getProperties());

        mlElements.add(mlElement);
      }

      result.add(
          new VisualLayoutCreationTO(curLayout.getName())
              .withScaleX(curLayout.getScaleX())
              .withScaleY(curLayout.getScaleY())
              .withModelElements(mlElements)
              .withShapeElements(slElements)
              .withProperties(curLayout.getProperties())
      );
    }
    return result;
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
    Set<Block> blocks = getObjectPool().getObjects(Block.class);
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
