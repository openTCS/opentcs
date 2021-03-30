/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.ObjectNameProvider;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code PeripheralJobPool} keeps all {@code PeripheralJobs}s for an openTCS kernel and provides
 * methods to create and manipulate them.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of instances of this
 * class must be synchronized externally.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralJobPool {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralJobPool.class);
  /**
   * The system's global object pool.
   */
  private final TCSObjectPool objectPool;
  /**
   * Provides names for peripheral jobs.
   */
  private final ObjectNameProvider objectNameProvider;

  /**
   * Creates a new instance.
   *
   * @param objectPool The object pool serving as the container for this peripheral job pool's data.
   * @param orderNameProvider Provides names for peripheral jobs.
   */
  @Inject
  public PeripheralJobPool(TCSObjectPool objectPool,
                           ObjectNameProvider orderNameProvider) {
    this.objectPool = requireNonNull(objectPool, "objectPool");
    this.objectNameProvider = requireNonNull(orderNameProvider, "orderNameProvider");
  }

  /**
   * Removes all peripheral jobs from this pool.
   */
  public void clear() {
    for (PeripheralJob job : objectPool.getObjects(PeripheralJob.class)) {
      objectPool.removeObject(job.getReference());
      objectPool.emitObjectEvent(null,
                                 job,
                                 TCSObjectEvent.Type.OBJECT_REMOVED);
    }
  }

  /**
   * Adds a new peripheral job to the pool.
   *
   * @param to The transfer object from which to create the new peripheral job.
   * @return The newly created peripheral job.
   * @throws ObjectExistsException If an object with the new object's name already exists.
   * @throws ObjectUnknownException If any object referenced in the TO does not exist.
   */
  public PeripheralJob createPeripheralJob(PeripheralJobCreationTO to)
      throws ObjectUnknownException, ObjectExistsException {
    PeripheralJob job = new PeripheralJob(nameFor(to),
                                          to.getReservationToken(),
                                          toPeripheralOperation(to.getPeripheralOperation()))
        .withRelatedVehicle(toVehicleReference(to.getRelatedVehicleName()))
        .withRelatedTransportOrder(toTransportOrderReference(to.getRelatedTransportOrderName()))
        .withProperties(to.getProperties());
    objectPool.addObject(job);
    objectPool.emitObjectEvent(job, null, TCSObjectEvent.Type.OBJECT_CREATED);
    LOG.debug("Created peripheral job {}...", job.getName());

    return job;
  }

  /**
   * Sets a peripheral jobs's state.
   *
   * @param ref A reference to the peripheral job to be modified.
   * @param newState The peripheral job's new state.
   * @return The modified peripheral job.
   * @throws ObjectUnknownException If the referenced peripheral job is not in this pool.
   */
  public PeripheralJob setPeripheralJobState(TCSObjectReference<PeripheralJob> ref,
                                             PeripheralJob.State newState)
      throws ObjectUnknownException {
    LOG.debug("Updating state of peripheral job {} to {}...", ref.getName(), newState);
    PeripheralJob job = objectPool.getObject(PeripheralJob.class, ref);
    PeripheralJob previousState = job;
    job = objectPool.replaceObject(job.withState(newState));
    objectPool.emitObjectEvent(job,
                               previousState,
                               TCSObjectEvent.Type.OBJECT_MODIFIED);
    return job;
  }

  private PeripheralOperation toPeripheralOperation(PeripheralOperationCreationTO to)
      throws ObjectUnknownException {
    return new PeripheralOperation(toLocationReference(to.getLocationName()),
                                   to.getOperation(),
                                   to.getExecutionTrigger(),
                                   to.isCompletionRequired());
  }

  private TCSResourceReference<Location> toLocationReference(String locationName)
      throws ObjectUnknownException {
    Location location = objectPool.getObject(Location.class, locationName);
    return location.getReference();
  }

  private TCSObjectReference<Vehicle> toVehicleReference(String vehicleName)
      throws ObjectUnknownException {
    if (vehicleName == null) {
      return null;
    }
    Vehicle vehicle = objectPool.getObject(Vehicle.class, vehicleName);
    return vehicle.getReference();
  }

  private TCSObjectReference<TransportOrder> toTransportOrderReference(String transportOrderName)
      throws ObjectUnknownException {
    if (transportOrderName == null) {
      return null;
    }
    TransportOrder order = objectPool.getObject(TransportOrder.class, transportOrderName);
    return order.getReference();
  }

  @Nonnull
  private String nameFor(@Nonnull PeripheralJobCreationTO to) {
    if (to.hasIncompleteName()) {
      return objectNameProvider.apply(to);
    }
    else {
      return to.getName();
    }
  }
}
