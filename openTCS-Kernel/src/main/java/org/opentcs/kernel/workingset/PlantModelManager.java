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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.Colors;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presents a view on the topology of a plant model contained in a
 * {@link TCSObjectRepository}.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of instances of this
 * class must be synchronized externally.
 * </p>
 */
public class PlantModelManager
    extends TCSObjectManager {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PlantModelManager.class);
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
   * @param objectRepo The object repo.
   * @param eventHandler The event handler to publish events to.
   */
  @Inject
  public PlantModelManager(@Nonnull TCSObjectRepository objectRepo,
                           @Nonnull @ApplicationEventBus EventHandler eventHandler) {
    super(objectRepo, eventHandler);
  }

  /**
   * Returns this model's name.
   *
   * @return This model's name.
   */
  public String getName() {
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
    List<TCSObject<?>> objects = new ArrayList<>();
    objects.addAll(getObjectRepo().getObjects(VisualLayout.class));
    objects.addAll(getObjectRepo().getObjects(Vehicle.class));
    objects.addAll(getObjectRepo().getObjects(org.opentcs.data.model.Group.class));
    objects.addAll(getObjectRepo().getObjects(Block.class));
    objects.addAll(getObjectRepo().getObjects(Path.class));
    objects.addAll(getObjectRepo().getObjects(Location.class));
    objects.addAll(getObjectRepo().getObjects(LocationType.class));
    objects.addAll(getObjectRepo().getObjects(Point.class));

    for (TCSObject<?> curObject : objects) {
      getObjectRepo().removeObject(curObject.getReference());
      emitObjectEvent(null,
                      curObject,
                      TCSObjectEvent.Type.OBJECT_REMOVED);
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
    LOG.info("Plant model is being created: {}", to.getName());

    clear();
    setName(to.getName());
    setProperties(to.getProperties());

    for (PointCreationTO point : to.getPoints()) {
      createPoint(point);
    }
    for (LocationTypeCreationTO locType : to.getLocationTypes()) {
      createLocationType(locType);
    }
    for (LocationCreationTO loc : to.getLocations()) {
      createLocation(loc);
    }
    for (PathCreationTO path : to.getPaths()) {
      createPath(path);
    }

    for (BlockCreationTO block : to.getBlocks()) {
      createBlock(block);
    }
    for (org.opentcs.access.to.model.GroupCreationTO group : to.getGroups()) {
      createGroup(group);
    }
    for (VehicleCreationTO vehicle : to.getVehicles()) {
      createVehicle(vehicle);
    }

    createVisualLayout(to.getVisualLayout());
    overrideLayoutData(to.getVisualLayout());
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
    Path previousState = getObjectRepo().getObject(Path.class, ref);
    Path path = previousState.withLocked(newLocked);
    getObjectRepo().replaceObject(path.withLocked(newLocked));
    emitObjectEvent(path,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return path;
  }

  /**
   * Locks/Unlocks a location.
   *
   * @param ref A reference to the location to be modified.
   * @param newLocked If {@code true}, this path will be locked when the method call returns;
   * if {@code false}, this path will be unlocked.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  public Location setLocationLocked(TCSObjectReference<Location> ref, boolean newLocked)
      throws ObjectUnknownException {
    Location previousState = getObjectRepo().getObject(Location.class, ref);
    Location location = previousState.withLocked(newLocked);
    getObjectRepo().replaceObject(location);
    emitObjectEvent(location,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Sets a location's reservation token.
   *
   * @param ref A reference to the location to be modified.
   * @param newToken The new reservation token.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  public Location setLocationReservationToken(TCSObjectReference<Location> ref, String newToken)
      throws ObjectUnknownException {
    Location previousState = getObjectRepo().getObject(Location.class, ref);
    Location location = previousState.withPeripheralInformation(
        previousState.getPeripheralInformation().withReservationToken(newToken)
    );
    getObjectRepo().replaceObject(location);
    emitObjectEvent(location,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Sets a location's processing state.
   *
   * @param ref A reference to the location to be modified.
   * @param newState The new processing state.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  public Location setLocationProcState(TCSObjectReference<Location> ref,
                                       PeripheralInformation.ProcState newState)
      throws ObjectUnknownException {
    Location previousState = getObjectRepo().getObject(Location.class, ref);
    Location location = previousState.withPeripheralInformation(
        previousState.getPeripheralInformation().withProcState(newState)
    );
    getObjectRepo().replaceObject(location);
    emitObjectEvent(location,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Sets a location's state.
   *
   * @param ref A reference to the location to be modified.
   * @param newState The new state.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  public Location setLocationState(TCSObjectReference<Location> ref,
                                   PeripheralInformation.State newState)
      throws ObjectUnknownException {
    Location previousState = getObjectRepo().getObject(Location.class, ref);
    Location location = previousState.withPeripheralInformation(
        previousState.getPeripheralInformation().withState(newState)
    );
    getObjectRepo().replaceObject(location);
    emitObjectEvent(location,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
  }

  /**
   * Sets a location's peripheral job.
   *
   * @param ref A reference to the location to be modified.
   * @param newJob The new peripheral job.
   * @return The modified location.
   * @throws ObjectUnknownException If the referenced location does not exist.
   */
  public Location setLocationPeripheralJob(TCSObjectReference<Location> ref,
                                           TCSObjectReference<PeripheralJob> newJob)
      throws ObjectUnknownException {
    Location previousState = getObjectRepo().getObject(Location.class, ref);
    Location location = previousState.withPeripheralInformation(
        previousState.getPeripheralInformation().withPeripheralJob(newJob)
    );
    getObjectRepo().replaceObject(location);
    emitObjectEvent(location,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return location;
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);
    Vehicle vehicle = previousState.withEnergyLevel(energyLevel);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.info("Vehicle's recharge operation changes: {} -- {} -> {}",
             previousState.getName(),
             previousState.getRechargeOperation(),
             rechargeOperation);

    Vehicle vehicle = previousState.withRechargeOperation(rechargeOperation);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);
    Vehicle vehicle = previousState.withLoadHandlingDevices(devices);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.debug("Vehicle's state changes: {} -- {} -> {}",
              previousState.getName(),
              previousState.getState(),
              newState);

    Vehicle vehicle = previousState.withState(newState);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's length.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param newLength The vehicle's new length.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleLength(TCSObjectReference<Vehicle> ref,
                                  int newLength)
      throws ObjectUnknownException {
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.debug("Vehicle's length changes: {} -- {} -> {}",
              previousState.getName(),
              previousState.getLength(),
              newLength);

    Vehicle vehicle = previousState.withLength(newLength);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.info("Vehicle's integration level changes: {} -- {} -> {}",
             previousState.getName(),
             previousState.getIntegrationLevel(),
             integrationLevel);

    Vehicle vehicle = previousState.withIntegrationLevel(integrationLevel);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's paused state.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param paused The vehicle's new paused state.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehiclePaused(TCSObjectReference<Vehicle> ref,
                                  boolean paused)
      throws ObjectUnknownException {
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.info("Vehicle's paused state changes: {} -- {} -> {}",
             previousState.getName(),
             previousState.isPaused(),
             paused);

    Vehicle vehicle = previousState.withPaused(paused);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.debug("Vehicle's proc state changes: {} -- {} -> {}",
              previousState.getName(),
              previousState.getProcState(),
              newState);

    Vehicle vehicle = previousState.withProcState(newState);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets the allowed order types for a given vehicle.
   *
   * @param ref Reference to the vehicle.
   * @param allowedOrderTypes Set of allowed order types.
   * @return The vehicle with the allowed order types.
   * @throws ObjectUnknownException The vehicle reference is not known.
   */
  public Vehicle setVehicleAllowedOrderTypes(TCSObjectReference<Vehicle> ref,
                                             Set<String> allowedOrderTypes)
      throws ObjectUnknownException {
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.info("Vehicle's allowed order types change: {} -- {} -> {}",
             previousState.getName(),
             previousState.getAllowedOrderTypes(),
             allowedOrderTypes);

    Vehicle vehicle = previousState.withAllowedOrderTypes(allowedOrderTypes);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's envelope key.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param envelopeKey The vehicle's new envelope key.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleEnvelopeKey(TCSObjectReference<Vehicle> ref,
                                       String envelopeKey)
      throws ObjectUnknownException {
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.info("Vehicle's envelope key change: {} -- {} -> {}",
             previousState.getName(),
             previousState.getEnvelopeKey(),
             envelopeKey);

    Vehicle vehicle = previousState.withEnvelopeKey(envelopeKey);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle vehicle = getObjectRepo().getObject(Vehicle.class, ref);

    LOG.debug("Vehicle's position changes: {} -- {} -> {}",
              vehicle.getName(),
              vehicle.getCurrentPosition() == null ? null : vehicle.getCurrentPosition().getName(),
              newPosRef == null ? null : newPosRef.getName());

    Vehicle previousVehicleState = vehicle;
    // If the vehicle was occupying a point before, clear it and send an event.
    if (vehicle.getCurrentPosition() != null) {
      Point oldVehiclePos = getObjectRepo().getObject(Point.class, vehicle.getCurrentPosition());
      Point previousPointState = oldVehiclePos;
      oldVehiclePos = oldVehiclePos.withOccupyingVehicle(null);
      getObjectRepo().replaceObject(oldVehiclePos);
      emitObjectEvent(oldVehiclePos,
                      previousPointState,
                      TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    // If the vehicle is occupying a point now, set that and send an event.
    if (newPosRef != null) {
      Point newVehiclePos = getObjectRepo().getObject(Point.class, newPosRef);
      Point previousPointState = newVehiclePos;
      newVehiclePos = newVehiclePos.withOccupyingVehicle(ref);
      getObjectRepo().replaceObject(newVehiclePos);
      emitObjectEvent(newVehiclePos,
                      previousPointState,
                      TCSObjectEvent.Type.OBJECT_MODIFIED);
    }
    vehicle = vehicle.withCurrentPosition(newPosRef);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);
    Vehicle vehicle = previousState.withNextPosition(newPosition);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);
    Vehicle vehicle = previousState.withPrecisePosition(newPosition);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, ref);
    Vehicle vehicle = previousState.withOrientationAngle(angle);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
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
    Vehicle vehicle = getObjectRepo().getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle;
    if (orderRef == null) {
      vehicle = vehicle.withTransportOrder(null);
      getObjectRepo().replaceObject(vehicle);
    }
    else {
      TransportOrder order = getObjectRepo().getObject(TransportOrder.class, orderRef);
      vehicle = vehicle.withTransportOrder(order.getReference());
      getObjectRepo().replaceObject(vehicle);
    }
    emitObjectEvent(vehicle,
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
    Vehicle vehicle = getObjectRepo().getObject(Vehicle.class, vehicleRef);
    Vehicle previousState = vehicle;
    if (seqRef == null) {
      vehicle = vehicle.withOrderSequence(null);
      getObjectRepo().replaceObject(vehicle);
    }
    else {
      OrderSequence seq = getObjectRepo().getObject(OrderSequence.class, seqRef);
      vehicle = vehicle.withOrderSequence(seq.getReference());
      getObjectRepo().replaceObject(vehicle);
    }
    emitObjectEvent(vehicle,
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
   * @deprecated Use {@link TransportOrderPoolManager#setTransportOrderCurrentRouteStepIndex(
   * org.opentcs.data.TCSObjectReference, int)} instead.
   */
  @Deprecated
  public Vehicle setVehicleRouteProgressIndex(TCSObjectReference<Vehicle> vehicleRef,
                                              int index)
      throws ObjectUnknownException {
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, vehicleRef);
    Vehicle vehicle = previousState.withRouteProgressIndex(index);
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's claimed resources.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param resources The new resources.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleClaimedResources(TCSObjectReference<Vehicle> vehicleRef,
                                            List<Set<TCSResourceReference<?>>> resources)
      throws ObjectUnknownException {
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, vehicleRef);
    Vehicle vehicle = previousState.withClaimedResources(unmodifiableCopy(resources));
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Sets a vehicle's allocated resources.
   *
   * @param vehicleRef A reference to the vehicle to be modified.
   * @param resources The new resources.
   * @return The modified vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   */
  public Vehicle setVehicleAllocatedResources(TCSObjectReference<Vehicle> vehicleRef,
                                              List<Set<TCSResourceReference<?>>> resources)
      throws ObjectUnknownException {
    Vehicle previousState = getObjectRepo().getObject(Vehicle.class, vehicleRef);
    Vehicle vehicle = previousState.withAllocatedResources(unmodifiableCopy(resources));
    getObjectRepo().replaceObject(vehicle);
    emitObjectEvent(vehicle,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return vehicle;
  }

  /**
   * Returns a PlantModelCreationTO for this model.
   *
   * @return A PlantModelCreationTO for this model.
   */
  @SuppressWarnings("deprecation")
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
        .withVisualLayout(getVisualLayout());
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
  public Set<TCSResource<?>> expandResources(@Nonnull Set<TCSResourceReference<?>> resources)
      throws ObjectUnknownException {
    requireNonNull(resources, "resources");

    Set<Block> blocks = getObjectRepo().getObjects(Block.class);

    // First, collect the given references plus references to all members of blocks that contain the
    // given references in a set.
    // We could look up all resources and add them to the result immediately, but by first
    // collecting all references, we ensure that we look up each resource only once.
    Set<TCSResourceReference<?>> refsToLookUp = new HashSet<>();
    for (TCSResourceReference<?> resourceRef : resources) {
      refsToLookUp.add(resourceRef);

      blocks.stream()
          .filter(block -> block.getMembers().contains(resourceRef))
          .flatMap(block -> block.getMembers().stream())
          .forEach(memberRef -> refsToLookUp.add(memberRef));
    }

    // Look up and return the actual resources.
    return refsToLookUp.stream()
        .map(memberRef -> (TCSResource<?>) getObjectRepo().getObject(memberRef))
        .collect(Collectors.toSet());
  }

  private List<PeripheralOperation> mapPeripheralOperationTOs(
      List<PeripheralOperationCreationTO> creationTOs) {
    return creationTOs.stream()
        .map(
            operationTO -> new PeripheralOperation(
                getObjectRepo().getObject(Location.class,
                                          operationTO.getLocationName()).getReference(),
                operationTO.getOperation(),
                operationTO.getExecutionTrigger(),
                operationTO.isCompletionRequired())
        )
        .collect(Collectors.toList());
  }

  /**
   * Returns a list of {@link PointCreationTO Points} for all points in a model.
   *
   * @return A list of {@link PointCreationTO Points} for all points in a model.
   */
  private List<PointCreationTO> getPoints() {
    Set<Point> points = getObjectRepo().getObjects(Point.class);
    List<PointCreationTO> result = new ArrayList<>();

    for (Point curPoint : points) {
      result.add(
          new PointCreationTO(curPoint.getName())
              .withPose(
                  new Pose(
                      curPoint.getPose().getPosition(),
                      curPoint.getPose().getOrientationAngle()
                  )
              )
              .withType(curPoint.getType())
              .withVehicleEnvelopes(curPoint.getVehicleEnvelopes())
              .withProperties(curPoint.getProperties())
              .withLayout(new PointCreationTO.Layout(curPoint.getLayout().getPosition(),
                                                     curPoint.getLayout().getLabelOffset(),
                                                     curPoint.getLayout().getLayerId()))
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
    Set<Path> paths = getObjectRepo().getObjects(Path.class);
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
              .withPeripheralOperations(getPeripheralOperations(curPath))
              .withVehicleEnvelopes(curPath.getVehicleEnvelopes())
              .withProperties(curPath.getProperties())
              .withLayout(new PathCreationTO.Layout(curPath.getLayout().getConnectionType(),
                                                    curPath.getLayout().getControlPoints(),
                                                    curPath.getLayout().getLayerId()))
      );
    }

    return result;
  }

  private List<PeripheralOperationCreationTO> getPeripheralOperations(Path path) {
    return path.getPeripheralOperations().stream()
        .map(op
            -> new PeripheralOperationCreationTO(op.getOperation(), op.getLocation().getName())
            .withExecutionTrigger(op.getExecutionTrigger())
            .withCompletionRequired(op.isCompletionRequired())
        )
        .collect(Collectors.toList());
  }

  /**
   * Returns a list of {@link VehicleCreationTO Vehicles} for all vehicles in a model.
   *
   * @param model The model data.
   * @return A list of {@link VehicleCreationTO Vehicles} for all vehicles in a model.
   */
  private List<VehicleCreationTO> getVehicles() {
    Set<Vehicle> vehicles = getObjectRepo().getObjects(Vehicle.class);
    List<VehicleCreationTO> result = new ArrayList<>();

    for (Vehicle vehicle : vehicles) {
      result.add(
          new VehicleCreationTO(vehicle.getName())
              .withLength(vehicle.getLength())
              .withEnergyLevelGood(vehicle.getEnergyLevelGood())
              .withEnergyLevelCritical(vehicle.getEnergyLevelCritical())
              .withEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
              .withEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged())
              .withMaxVelocity(vehicle.getMaxVelocity())
              .withMaxReverseVelocity(vehicle.getMaxReverseVelocity())
              .withEnvelopeKey(vehicle.getEnvelopeKey())
              .withProperties(vehicle.getProperties())
              .withLayout(new VehicleCreationTO.Layout(vehicle.getLayout().getRouteColor()))
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
    Set<LocationType> locTypes = getObjectRepo().getObjects(LocationType.class);
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationType curType : locTypes) {
      result.add(
          new LocationTypeCreationTO(curType.getName())
              .withAllowedOperations(curType.getAllowedOperations())
              .withAllowedPeripheralOperations(curType.getAllowedPeripheralOperations())
              .withProperties(curType.getProperties())
              .withLayout(
                  new LocationTypeCreationTO.Layout(curType.getLayout().getLocationRepresentation())
              )
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
    Set<Location> locations = getObjectRepo().getObjects(Location.class);
    List<LocationCreationTO> result = new ArrayList<>();

    for (Location curLoc : locations) {
      result.add(
          new LocationCreationTO(curLoc.getName(),
                                 curLoc.getType().getName(),
                                 curLoc.getPosition())
              .withLinks(curLoc.getAttachedLinks().stream()
                  .collect(Collectors.toMap(link -> link.getPoint().getName(),
                                            Location.Link::getAllowedOperations)))
              .withLocked(curLoc.isLocked())
              .withProperties(curLoc.getProperties())
              .withLayout(
                  new LocationCreationTO.Layout(curLoc.getLayout().getPosition(),
                                                curLoc.getLayout().getLabelOffset(),
                                                curLoc.getLayout().getLocationRepresentation(),
                                                curLoc.getLayout().getLayerId())
              )
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
    Set<Block> blocks = getObjectRepo().getObjects(Block.class);
    List<BlockCreationTO> result = new ArrayList<>();

    for (Block curBlock : blocks) {
      result.add(
          new BlockCreationTO(curBlock.getName())
              .withMemberNames(curBlock.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withType(curBlock.getType())
              .withProperties(curBlock.getProperties())
              .withLayout(new BlockCreationTO.Layout(curBlock.getLayout().getColor()))
      );
    }

    return result;
  }

  /**
   * Returns a list of GroupCreationTOs for all groups in a model.
   *
   * @param model The model data.
   * @return A list of GroupCreationTOs for all groups in a model.
   */
  @Deprecated
  private List<org.opentcs.access.to.model.GroupCreationTO> getGroups() {
    Set<org.opentcs.data.model.Group> groups
        = getObjectRepo().getObjects(org.opentcs.data.model.Group.class);
    List<org.opentcs.access.to.model.GroupCreationTO> result = new ArrayList<>();

    for (org.opentcs.data.model.Group curGroup : groups) {
      result.add(
          new org.opentcs.access.to.model.GroupCreationTO(curGroup.getName())
              .withMemberNames(curGroup.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withProperties(curGroup.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a {@link VisualLayoutCreationTO} for the visual layouts in a model.
   *
   * @param model The model data.
   * @return A {@link VisualLayoutCreationTO} for the visual layouts in a model.
   */
  private VisualLayoutCreationTO getVisualLayout() {
    Set<VisualLayout> layouts = getObjectRepo().getObjects(VisualLayout.class);
    checkState(layouts.size() == 1,
               "There has to be one, and only one, visual layout. Number of visual layouts: %d",
               layouts.size());
    VisualLayout layout = layouts.iterator().next();

    return new VisualLayoutCreationTO(layout.getName())
        .withScaleX(layout.getScaleX())
        .withScaleY(layout.getScaleY())
        .withProperties(layout.getProperties())
        .withLayers(layout.getLayers())
        .withLayerGroups(layout.getLayerGroups());
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
  private VisualLayout createVisualLayout(VisualLayoutCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    VisualLayout newLayout = new VisualLayout(to.getName())
        .withScaleX(to.getScaleX())
        .withScaleY(to.getScaleY())
        .withLayers(to.getLayers())
        .withLayerGroups(to.getLayerGroups());

    getObjectRepo().addObject(newLayout);
    emitObjectEvent(newLayout,
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
  private Point createPoint(PointCreationTO to)
      throws ObjectExistsException {
    // Get a unique ID for the new point and create an instance.
    Point newPoint = new Point(to.getName())
        .withPose(new Pose(to.getPose().getPosition(), to.getPose().getOrientationAngle()))
        .withType(to.getType())
        .withVehicleEnvelopes(to.getVehicleEnvelopes())
        .withProperties(to.getProperties())
        .withLayout(new Point.Layout(to.getLayout().getPosition(),
                                     to.getLayout().getLabelOffset(),
                                     to.getLayout().getLayerId()));
    getObjectRepo().addObject(newPoint);
    emitObjectEvent(newPoint, null, TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created point.
    return newPoint;
  }

  /**
   * Creates a new path from the given transfer object.
   *
   * @param to The transfer object from which to create the new path.
   * @return The newly created path.
   * @throws ObjectUnknownException If the referenced point does not exist.
   * @throws ObjectExistsException If an object with the same name as the path already exists.
   */
  private Path createPath(PathCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    requireNonNull(to, "to");

    Point srcPoint = getObjectRepo().getObject(Point.class, to.getSrcPointName());
    Point destPoint = getObjectRepo().getObject(Point.class, to.getDestPointName());
    Path newPath = new Path(to.getName(),
                            srcPoint.getReference(),
                            destPoint.getReference())
        .withLength(to.getLength())
        .withMaxVelocity(to.getMaxVelocity())
        .withMaxReverseVelocity(to.getMaxReverseVelocity())
        .withPeripheralOperations(mapPeripheralOperationTOs(to.getPeripheralOperations()))
        .withVehicleEnvelopes(to.getVehicleEnvelopes())
        .withProperties(to.getProperties())
        .withLocked(to.isLocked())
        .withLayout(new Path.Layout(to.getLayout().getConnectionType(),
                                    to.getLayout().getControlPoints(),
                                    to.getLayout().getLayerId()));

    getObjectRepo().addObject(newPath);

    emitObjectEvent(newPath,
                    null,
                    TCSObjectEvent.Type.OBJECT_CREATED);

    addPointOutgoingPath(srcPoint.getReference(), newPath.getReference());
    addPointIncomingPath(destPoint.getReference(), newPath.getReference());

    return newPath;
  }

  /**
   * Creates a new location type with a unique name and all other attributes set
   * to their default values.
   *
   * @param to The transfer object from which to create the new location type.
   * @return The newly created location type.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   */
  private LocationType createLocationType(LocationTypeCreationTO to)
      throws ObjectExistsException {
    LocationType newType = new LocationType(to.getName())
        .withAllowedOperations(to.getAllowedOperations())
        .withAllowedPeripheralOperations(to.getAllowedPeripheralOperations())
        .withProperties(to.getProperties())
        .withLayout(new LocationType.Layout(to.getLayout().getLocationRepresentation()));
    getObjectRepo().addObject(newType);
    emitObjectEvent(newType,
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
  private Location createLocation(LocationCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    LocationType type = getObjectRepo().getObject(LocationType.class, to.getTypeName());
    Location newLocation = new Location(to.getName(), type.getReference())
        .withPosition(to.getPosition())
        .withLocked(to.isLocked())
        .withProperties(to.getProperties())
        .withLayout(new Location.Layout(to.getLayout().getPosition(),
                                        to.getLayout().getLabelOffset(),
                                        to.getLayout().getLocationRepresentation(),
                                        to.getLayout().getLayerId()));

    Set<Location.Link> locationLinks = new HashSet<>();
    for (Map.Entry<String, Set<String>> linkEntry : to.getLinks().entrySet()) {
      Point point = getObjectRepo().getObject(Point.class, linkEntry.getKey());
      Location.Link link = new Location.Link(newLocation.getReference(), point.getReference())
          .withAllowedOperations(linkEntry.getValue());
      locationLinks.add(link);
    }
    newLocation = newLocation.withAttachedLinks(locationLinks);

    getObjectRepo().addObject(newLocation);
    emitObjectEvent(newLocation,
                    null,
                    TCSObjectEvent.Type.OBJECT_CREATED);

    // Add the location's links to the respective points, too.
    for (Location.Link link : locationLinks) {
      Point point = getObjectRepo().getObject(Point.class, link.getPoint());

      Set<Location.Link> pointLinks = new HashSet<>(point.getAttachedLinks());
      pointLinks.add(link);

      Point previousPointState = point;
      point = point.withAttachedLinks(pointLinks);
      getObjectRepo().replaceObject(point);

      emitObjectEvent(point,
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
  private Vehicle createVehicle(VehicleCreationTO to)
      throws ObjectExistsException {
    Vehicle newVehicle = new Vehicle(to.getName())
        .withLength(to.getLength())
        .withEnergyLevelGood(to.getEnergyLevelGood())
        .withEnergyLevelCritical(to.getEnergyLevelCritical())
        .withEnergyLevelFullyRecharged(to.getEnergyLevelFullyRecharged())
        .withEnergyLevelSufficientlyRecharged(to.getEnergyLevelSufficientlyRecharged())
        .withMaxVelocity(to.getMaxVelocity())
        .withMaxReverseVelocity(to.getMaxReverseVelocity())
        .withEnvelopeKey(to.getEnvelopeKey())
        .withProperties(to.getProperties())
        .withLayout(new Vehicle.Layout(to.getLayout().getRouteColor()));
    getObjectRepo().addObject(newVehicle);
    emitObjectEvent(newVehicle,
                    null,
                    TCSObjectEvent.Type.OBJECT_CREATED);
    return newVehicle;
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
  private Block createBlock(BlockCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    Set<TCSResourceReference<?>> members = new HashSet<>();
    for (String memberName : to.getMemberNames()) {
      TCSObject<?> object = getObjectRepo().getObject(memberName);
      if (!(object instanceof TCSResource)) {
        throw new ObjectUnknownException(memberName);
      }
      members.add(((TCSResource) object).getReference());
    }
    Block newBlock = new Block(to.getName())
        .withType(to.getType())
        .withMembers(members)
        .withProperties(to.getProperties())
        .withLayout(new Block.Layout(to.getLayout().getColor()));
    getObjectRepo().addObject(newBlock);
    emitObjectEvent(newBlock,
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
  @Deprecated
  private org.opentcs.data.model.Group createGroup(org.opentcs.access.to.model.GroupCreationTO to)
      throws ObjectExistsException, ObjectUnknownException {
    Set<TCSObjectReference<?>> members = new HashSet<>();
    for (String memberName : to.getMemberNames()) {
      members.add(getObjectRepo().getObject(memberName).getReference());
    }
    org.opentcs.data.model.Group newGroup = new org.opentcs.data.model.Group(to.getName())
        .withMembers(members)
        .withProperties(to.getProperties());
    getObjectRepo().addObject(newGroup);
    emitObjectEvent(newGroup,
                    null,
                    TCSObjectEvent.Type.OBJECT_CREATED);
    // Return the newly created group.
    return newGroup;
  }

  /**
   * Overrides the layout data in {@code TCSObject}s with the data stored in their respective
   * model layout element.
   *
   * @param layout The visual layout to get the layout data from.
   */
  @Deprecated
  @ScheduledApiChange(details = "Will be removed.", when = "6.0")
  private void overrideLayoutData(VisualLayoutCreationTO layout) {
    for (org.opentcs.access.to.model.ModelLayoutElementCreationTO mleTO
             : layout.getModelElements()) {
      TCSObject<?> object = getObjectRepo().getObject(mleTO.getName());
      Map<String, String> props = mleTO.getProperties();

      if (object instanceof Point) {
        overridePointLayoutData((Point) object, props);
      }
      else if (object instanceof Path) {
        overridePathLayoutData((Path) object, props);
      }
      else if (object instanceof Location) {
        overrideLocationLayoutData((Location) object, props);
      }
      else if (object instanceof Block) {
        overrideBlockLayoutData((Block) object, props);
      }
      else if (object instanceof Vehicle) {
        overrideVehicleLayoutData((Vehicle) object, props);
      }
    }
  }

  private void overridePointLayoutData(Point oldPoint, Map<String, String> properties)
      throws NumberFormatException {
    long positionX = properties.get(ElementPropKeys.POINT_POS_X) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.POINT_POS_X))
        : oldPoint.getLayout().getPosition().getX();
    long positionY = properties.get(ElementPropKeys.POINT_POS_Y) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.POINT_POS_Y))
        : oldPoint.getLayout().getPosition().getY();
    long labelOffsetX = properties.get(ElementPropKeys.POINT_LABEL_OFFSET_X) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_X))
        : oldPoint.getLayout().getLabelOffset().getX();
    long labelOffsetY = properties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y))
        : oldPoint.getLayout().getLabelOffset().getY();
    Point newPoint = oldPoint.withLayout(
        new Point.Layout(new Couple(positionX, positionY),
                         new Couple(labelOffsetX, labelOffsetY),
                         oldPoint.getLayout().getLayerId())
    );
    getObjectRepo().replaceObject(newPoint);
    emitObjectEvent(newPoint, oldPoint, TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  private void overridePathLayoutData(Path oldPath, Map<String, String> properties)
      throws IllegalArgumentException {
    String connectionTypeString
        = properties.getOrDefault(ElementPropKeys.PATH_CONN_TYPE,
                                  oldPath.getLayout().getConnectionType().name());
    Path.Layout.ConnectionType connectionType;
    switch (connectionTypeString) {
      case "DIRECT":
        connectionType = Path.Layout.ConnectionType.DIRECT;
        break;
      case "ELBOW":
        connectionType = Path.Layout.ConnectionType.ELBOW;
        break;
      case "SLANTED":
        connectionType = Path.Layout.ConnectionType.SLANTED;
        break;
      case "POLYPATH":
        connectionType = Path.Layout.ConnectionType.POLYPATH;
        break;
      case "BEZIER":
        connectionType = Path.Layout.ConnectionType.BEZIER;
        break;
      case "BEZIER_3":
        connectionType = Path.Layout.ConnectionType.BEZIER_3;
        break;
      default:
        throw new IllegalArgumentException("Unhandled connection type: " + connectionTypeString);
    }

    List<Couple> controlPoints = oldPath.getLayout().getControlPoints();
    String controlPointsString = properties.get(ElementPropKeys.PATH_CONTROL_POINTS);
    if (controlPointsString != null) {
      controlPoints = Arrays.asList(controlPointsString.split(";")).stream()
          .map(controlPointString -> {
            String[] coordinateStrings = controlPointString.split(",");
            return new Couple(Long.parseLong(coordinateStrings[0]),
                              Long.parseLong(coordinateStrings[1]));
          })
          .collect(Collectors.toList());
    }

    Path newPath = oldPath.withLayout(new Path.Layout(connectionType,
                                                      controlPoints,
                                                      oldPath.getLayout().getLayerId()));

    getObjectRepo().replaceObject(newPath);
    emitObjectEvent(newPath, oldPath, TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  private void overrideLocationLayoutData(Location oldLocation, Map<String, String> properties)
      throws NumberFormatException {
    long positionX = properties.get(ElementPropKeys.LOC_POS_X) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.LOC_POS_X))
        : oldLocation.getLayout().getPosition().getX();
    long positionY = properties.get(ElementPropKeys.LOC_POS_Y) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.LOC_POS_Y))
        : oldLocation.getLayout().getPosition().getY();
    long labelOffsetX = properties.get(ElementPropKeys.LOC_LABEL_OFFSET_X) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_X))
        : oldLocation.getLayout().getLabelOffset().getX();
    long labelOffsetY = properties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y) != null
        ? Integer.parseInt(properties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y))
        : oldLocation.getLayout().getLabelOffset().getY();
    Location newLocation = oldLocation.withLayout(
        new Location.Layout(new Couple(positionX, positionY),
                            new Couple(labelOffsetX, labelOffsetY),
                            oldLocation.getLayout().getLocationRepresentation(),
                            oldLocation.getLayout().getLayerId())
    );
    getObjectRepo().replaceObject(newLocation);
    emitObjectEvent(newLocation, oldLocation, TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  private void overrideBlockLayoutData(Block oldBlock, Map<String, String> properties)
      throws NumberFormatException {
    Color color = properties.get(ElementPropKeys.BLOCK_COLOR) != null
        ? Colors.decodeFromHexRGB(properties.get(ElementPropKeys.BLOCK_COLOR))
        : oldBlock.getLayout().getColor();
    Block newBlock = oldBlock.withLayout(new Block.Layout(color));
    getObjectRepo().replaceObject(newBlock);
    emitObjectEvent(newBlock, oldBlock, TCSObjectEvent.Type.OBJECT_MODIFIED);
  }

  private void overrideVehicleLayoutData(Vehicle oldVehicle, Map<String, String> properties)
      throws NumberFormatException {
    Color routeColor = properties.get(ElementPropKeys.VEHICLE_ROUTE_COLOR) != null
        ? Colors.decodeFromHexRGB(properties.get(ElementPropKeys.VEHICLE_ROUTE_COLOR))
        : oldVehicle.getLayout().getRouteColor();
    Vehicle newVehicle = oldVehicle.withLayout(new Vehicle.Layout(routeColor));
    getObjectRepo().replaceObject(newVehicle);
    emitObjectEvent(newVehicle, oldVehicle, TCSObjectEvent.Type.OBJECT_MODIFIED);
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
    Point point = getObjectRepo().getObject(Point.class, pointRef);
    Path path = getObjectRepo().getObject(Path.class, pathRef);
    // Check if the point really is the path's destination point.
    if (!path.getDestinationPoint().equals(point.getReference())) {
      throw new IllegalArgumentException("Point is not the path's destination.");
    }
    Path previousState = path;
    Set<TCSObjectReference<Path>> incomingPaths = new HashSet<>(point.getIncomingPaths());
    incomingPaths.add(path.getReference());
    point = point.withIncomingPaths(incomingPaths);
    getObjectRepo().replaceObject(point);
    emitObjectEvent(point,
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
    Point point = getObjectRepo().getObject(Point.class, pointRef);
    Path path = getObjectRepo().getObject(Path.class, pathRef);
    // Check if the point really is the path's source.
    if (!path.getSourcePoint().equals(point.getReference())) {
      throw new IllegalArgumentException("Point is not the path's source.");
    }
    Path previousState = path;
    Set<TCSObjectReference<Path>> outgoingPaths = new HashSet<>(point.getOutgoingPaths());
    outgoingPaths.add(path.getReference());
    point = point.withOutgoingPaths(outgoingPaths);
    getObjectRepo().replaceObject(point);
    emitObjectEvent(point,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return point;
  }

  private static List<Set<TCSResourceReference<?>>> unmodifiableCopy(
      List<Set<TCSResourceReference<?>>> resources) {
    List<Set<TCSResourceReference<?>>> result = new ArrayList<>();

    for (Set<TCSResourceReference<?>> resSet : resources) {
      result.add(Set.copyOf(resSet));
    }

    return Collections.unmodifiableList(result);
  }
}
