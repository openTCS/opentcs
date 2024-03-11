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
import org.opentcs.customizations.ApplicationEventBus;
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
import static org.opentcs.util.Assertions.checkArgument;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps all {@code PeripheralJobs}s and provides methods to create and manipulate them.
 * <p>
 * Note that no synchronization is done inside this class. Concurrent access of instances of this
 * class must be synchronized externally.
 * </p>
 */
public class PeripheralJobPoolManager
    extends TCSObjectManager {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PeripheralJobPoolManager.class);
  /**
   * Provides names for peripheral jobs.
   */
  private final ObjectNameProvider objectNameProvider;

  /**
   * Creates a new instance.
   *
   * @param objectRepo The object repo.
   * @param eventHandler The event handler to publish events to.
   * @param orderNameProvider Provides names for peripheral jobs.
   */
  @Inject
  public PeripheralJobPoolManager(@Nonnull TCSObjectRepository objectRepo,
                                  @Nonnull @ApplicationEventBus EventHandler eventHandler,
                                  @Nonnull ObjectNameProvider orderNameProvider) {
    super(objectRepo, eventHandler);
    this.objectNameProvider = requireNonNull(orderNameProvider, "orderNameProvider");
  }

  /**
   * Removes all peripheral jobs from this pool.
   */
  public void clear() {
    for (PeripheralJob job : getObjectRepo().getObjects(PeripheralJob.class)) {
      getObjectRepo().removeObject(job.getReference());
      emitObjectEvent(null,
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
   * @throws IllegalArgumentException If the transfer object's combination of parameters is invalid.
   */
  public PeripheralJob createPeripheralJob(PeripheralJobCreationTO to)
      throws ObjectUnknownException, ObjectExistsException, IllegalArgumentException {
    checkArgument(
        !hasCompletionRequiredAndExecutionTriggerImmediate(to),
        "Peripheral job's operation has executionTrigger 'immediate' and completionRequired set."
    );

    PeripheralJob job = new PeripheralJob(nameFor(to),
                                          to.getReservationToken(),
                                          toPeripheralOperation(to.getPeripheralOperation()))
        .withRelatedVehicle(toVehicleReference(to.getRelatedVehicleName()))
        .withRelatedTransportOrder(toTransportOrderReference(to.getRelatedTransportOrderName()))
        .withProperties(to.getProperties());

    LOG.info("Peripheral job is being created: {} -- {}",
             job.getName(),
             job.getPeripheralOperation());

    getObjectRepo().addObject(job);
    emitObjectEvent(job, null, TCSObjectEvent.Type.OBJECT_CREATED);

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
    PeripheralJob previousState = getObjectRepo().getObject(PeripheralJob.class, ref);

    checkArgument(!previousState.getState().isFinalState(),
                  "Peripheral job %s already in a final state, not changing %s -> %s.",
                  ref.getName(),
                  previousState.getState(),
                  newState);

    LOG.info("Peripheral job's state changes: {} -- {} -> {}",
             previousState.getName(),
             previousState.getState(),
             newState);

    PeripheralJob job = previousState.withState(newState);
    getObjectRepo().replaceObject(job);
    emitObjectEvent(job,
                    previousState,
                    TCSObjectEvent.Type.OBJECT_MODIFIED);
    return job;
  }

  /**
   * Removes the referenced peripheral job from the pool.
   *
   * @param ref A reference to the peripheral job to be removed.
   * @return The removed peripheral job.
   * @throws ObjectUnknownException If the referenced peripheral job is not in the pool.
   */
  public PeripheralJob removePeripheralJob(TCSObjectReference<PeripheralJob> ref)
      throws ObjectUnknownException {
    PeripheralJob job = getObjectRepo().getObject(PeripheralJob.class, ref);
    // Make sure only jobs in a final state are removed.
    checkArgument(job.getState().isFinalState(),
                  "Peripheral job %s is not in a final state.",
                  job.getName());
    getObjectRepo().removeObject(ref);
    emitObjectEvent(null,
                    job,
                    TCSObjectEvent.Type.OBJECT_REMOVED);
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
    Location location = getObjectRepo().getObject(Location.class, locationName);
    return location.getReference();
  }

  private TCSObjectReference<Vehicle> toVehicleReference(String vehicleName)
      throws ObjectUnknownException {
    if (vehicleName == null) {
      return null;
    }
    Vehicle vehicle = getObjectRepo().getObject(Vehicle.class, vehicleName);
    return vehicle.getReference();
  }

  private TCSObjectReference<TransportOrder> toTransportOrderReference(String transportOrderName)
      throws ObjectUnknownException {
    if (transportOrderName == null) {
      return null;
    }
    TransportOrder order = getObjectRepo().getObject(TransportOrder.class, transportOrderName);
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

  private boolean hasCompletionRequiredAndExecutionTriggerImmediate(PeripheralJobCreationTO to) {
    PeripheralOperationCreationTO opTo = to.getPeripheralOperation();
    return opTo.isCompletionRequired()
        && opTo.getExecutionTrigger() == PeripheralOperation.ExecutionTrigger.IMMEDIATE;
  }
}
