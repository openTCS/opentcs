/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkState;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a sequence of movements and operations that are to be executed by a {@link Vehicle}.
 * <p>
 * A TransportOrder basically encapsulates a list of {@link DriveOrder} instances.
 * </p>
 * <p>
 * Transport orders may depend on other transport orders in the systems, which means they may not be
 * processed before the orders they depend on have been processed.
 * </p>
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class TransportOrder
    extends TCSObject<TransportOrder>
    implements Serializable,
               Cloneable {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TransportOrder.class);
  /**
   * The category of this transport order.
   */
  @Nonnull
  private String category = OrderConstants.CATEGORY_NONE;
  /**
   * A set of TransportOrders that must have been finished before this one may
   * be processed.
   */
  @Nonnull
  private Set<TCSObjectReference<TransportOrder>> dependencies = new LinkedHashSet<>();
  /**
   * A list of rejections for this transport order.
   */
  @Nonnull
  private List<Rejection> rejections = new LinkedList<>();

  @Nonnull
  private List<DriveOrder> driveOrders = new ArrayList<>();

  private int currentDriveOrderIndex = -1;
  /**
   * This transport order's current state.
   */
  @Nonnull
  private State state = State.RAW;
  /**
   * The point of time at which this transport order was created.
   */
  private final Instant creationTime;
  /**
   * The point of time at which processing of this transport order must be finished.
   */
  private Instant deadline = Instant.ofEpochMilli(Long.MAX_VALUE);
  /**
   * The point of time at which processing of this transport order was finished.
   */
  private Instant finishedTime = Instant.ofEpochMilli(Long.MAX_VALUE);
  /**
   * A reference to the vehicle that is intended to process this transport
   * order. If this order is free to be processed by any vehicle, this is
   * <code>null</code>.
   */
  @Nullable
  private TCSObjectReference<Vehicle> intendedVehicle;
  /**
   * A reference to the vehicle currently processing this transport order. If
   * this transport order is not being processed at the moment, this is
   * <code>null</code>.
   */
  @Nullable
  private TCSObjectReference<Vehicle> processingVehicle;
  /**
   * The order sequence this transport order belongs to. May be
   * <code>null</code> in case this order isn't part of any sequence.
   */
  @Nullable
  private TCSObjectReference<OrderSequence> wrappingSequence;
  /**
   * Whether this order is dispensable (may be withdrawn automatically).
   */
  private boolean dispensable;

  /**
   * Creates a new TransportOrder.
   *
   * @param objectID This transport order's ID.
   * @param name This transport order's name.
   * @param destinations A list of destinations that are to be travelled to
   * when processing this transport order.
   * @param creationTime The creation time stamp to be set.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder(int objectID,
                        String name,
                        List<DriveOrder.Destination> destinations,
                        long creationTime) {
    super(objectID, name);
    this.driveOrders = createDriveOrders(destinations);
    this.currentDriveOrderIndex = -1;
    this.creationTime = Instant.ofEpochMilli(creationTime);
    this.intendedVehicle = null;
    this.deadline = Instant.ofEpochMilli(Long.MAX_VALUE);
    this.dispensable = false;
    this.wrappingSequence = null;
    this.dependencies = new LinkedHashSet<>();
  }

  /**
   * Creates a new TransportOrder.
   *
   * @param objectID This transport order's ID.
   * @param name This transport order's name.
   * @param driveOrders A list of drive orders to be processed when processing this transport
   * order.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder(int objectID, String name, List<DriveOrder> driveOrders) {
    super(objectID, name);
    this.driveOrders = requireNonNull(driveOrders, "driveOrders");
    this.currentDriveOrderIndex = -1;
    this.creationTime = Instant.EPOCH;
    this.intendedVehicle = null;
    this.deadline = Instant.ofEpochMilli(Long.MAX_VALUE);
    this.dispensable = false;
    this.wrappingSequence = null;
    this.dependencies = new LinkedHashSet<>();
  }

  /**
   * Creates a new TransportOrder.
   *
   * @param name This transport order's name.
   * @param driveOrders A list of drive orders to be processed when processing this transport
   * order.
   */
  public TransportOrder(String name, List<DriveOrder> driveOrders) {
    super(name);
    this.driveOrders = requireNonNull(driveOrders, "driveOrders");
    this.currentDriveOrderIndex = -1;
    this.creationTime = Instant.EPOCH;
    this.intendedVehicle = null;
    this.deadline = Instant.ofEpochMilli(Long.MAX_VALUE);
    this.dispensable = false;
    this.wrappingSequence = null;
    this.dependencies = new LinkedHashSet<>();
  }

  /**
   * Creates a new TransportOrder.
   *
   * @param objectID This transport order's ID.
   * @param name This transport order's name.
   * @param destinations A list of destinations that are to be travelled to
   * when processing this transport order.
   * @param creationTime The creation time stamp to be set.
   */
  @SuppressWarnings("deprecation")
  private TransportOrder(int objectID,
                         String name,
                         Map<String, String> properties,
                         String category,
                         List<DriveOrder> driveOrders,
                         int currentDriveOrderIndex,
                         Instant creationTime,
                         TCSObjectReference<Vehicle> intendedVehicle,
                         Instant deadline,
                         boolean dispensable,
                         TCSObjectReference<OrderSequence> wrappingSequence,
                         Set<TCSObjectReference<TransportOrder>> dependencies,
                         List<Rejection> rejections,
                         TCSObjectReference<Vehicle> processingVehicle,
                         State state,
                         Instant finishedTime) {
    super(objectID, name, properties);
    this.category = requireNonNull(category, "category");
    requireNonNull(driveOrders, "driveOrders");
    this.driveOrders = new LinkedList<>();
    for (DriveOrder driveOrder : driveOrders) {
      this.driveOrders.add(driveOrder.withTransportOrder(this.getReference()));
    }

    this.currentDriveOrderIndex = currentDriveOrderIndex;
    this.creationTime = requireNonNull(creationTime, "creationTime");
    this.intendedVehicle = intendedVehicle;
    this.deadline = requireNonNull(deadline, "deadline");
    this.dispensable = dispensable;
    this.wrappingSequence = wrappingSequence;
    this.dependencies = requireNonNull(dependencies, "dependencies");
    this.rejections = requireNonNull(rejections, "rejections");
    this.processingVehicle = processingVehicle;
    this.state = requireNonNull(state, "state");
    this.finishedTime = requireNonNull(finishedTime, "finishedTime");
  }

  @Override
  public TransportOrder withProperty(String key, String value) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              propertiesWith(key, value), category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  @Override
  public TransportOrder withProperties(Map<String, String> properties) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              properties,
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Retruns this transport order's category.
   *
   * @return This transport order's category.
   */
  @Nonnull
  public String getCategory() {
    return category;
  }

  /**
   * Creates a copy of this obejct, with the given category.
   *
   * @param category The category to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withCategory(@Nonnull String category) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns this transport order's current state.
   *
   * @return This transport order's current state.
   */
  public State getState() {
    return state;
  }

  /**
   * Checks if this transport order's current state is equal to the given one.
   *
   * @param otherState The state to compare to this transport order's one.
   * @return <code>true</code> if, and only if, the given state is equal to this
   * transport order's one.
   */
  public boolean hasState(State otherState) {
    requireNonNull(otherState, "otherState");
    return this.state.equals(otherState);
  }

  /**
   * Changes this transport order's current state.
   *
   * @param newState The new state this transport order is supposed to be in.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setState(State newState) {
    requireNonNull(newState, "newState");
    checkState(!state.isFinalState(),
               "Trying to modify state %s to %s",
               state,
               newState);
    state = newState;
    if (state.equals(State.FINISHED)) {
      finishedTime = Instant.now();
    }
  }

  /**
   * Creates a copy of this object, with the given state.
   *
   * @param state The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withState(@Nonnull State state) {
    // XXX Finished time should probably not be set implicitly.
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              state == State.FINISHED ? Instant.now() : finishedTime);
  }

  /**
   * Returns this transport order's creation time.
   *
   * @return This transport order's creation time.
   */
  @ScheduledApiChange(when = "5.0", details = "Will return an Instant instead.")
  public long getCreationTime() {
    return creationTime.toEpochMilli();
  }

  /**
   * Creates a copy of this object, with the given creation time.
   *
   * @param creationTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withCreationTime(java.time.Instant)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder withCreationTime(long creationTime) {
    return withCreationTime(Instant.ofEpochMilli(creationTime));
  }

  /**
   * Creates a copy of this object, with the given creation time.
   *
   * @param creationTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withCreationTime(Instant creationTime) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns this transport order's deadline. If the value of transport order's
   * deadline was not changed, the initial value <code>Long.MAX_VALUE</code>
   * is returned.
   *
   * @return This transport order's deadline or the initial deadline value.
   * <code>Long.MAX_VALUE</code>, if the deadline was not changed.
   */
  @ScheduledApiChange(when = "5.0", details = "Will return an Instant instead.")
  public long getDeadline() {
    return deadline.toEpochMilli();
  }

  /**
   * Sets this transport order's deadline.
   *
   * @param newDeadline This transport order's new deadline.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setDeadline(long newDeadline) {
    deadline = Instant.ofEpochMilli(newDeadline);
  }

  /**
   * Creates a copy of this object, with the given deadline.
   *
   * @param deadline The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withDeadline(java.time.Instant)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder withDeadline(long deadline) {
    return withDeadline(Instant.ofEpochMilli(deadline));
  }

  /**
   * Creates a copy of this object, with the given deadline.
   *
   * @param deadline The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDeadline(Instant deadline) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the point of time at which this transport order was finished.
   * If the transport order has not been finished, yet,
   * <code>Long.MIN_VALUE</code> is returned.
   *
   * @return The point of time at which this transport order was finished, or
   * <code>Long.MIN_VALUE</code>, if the transport order has not been finished,
   * yet.
   */
  @ScheduledApiChange(when = "5.0", details = "Will return an Instant instead.")
  public long getFinishedTime() {
    return finishedTime.toEpochMilli();
  }

  /**
   * Creates a copy of this object, with the given finished time.
   *
   * @param finishedTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link #withFinishedTime(java.time.Instant)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder withFinishedTime(long finishedTime) {
    return withFinishedTime(Instant.ofEpochMilli(finishedTime));
  }

  /**
   * Creates a copy of this object, with the given finished time.
   *
   * @param finishedTime The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withFinishedTime(Instant finishedTime) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns a reference to the vehicle that is intended to process this
   * transport order.
   *
   * @return A reference to the vehicle that is intended to process this
   * transport order. If this order is free to be processed by any vehicle,
   * <code>null</code> is returned.
   */
  @Nullable
  public TCSObjectReference<Vehicle> getIntendedVehicle() {
    return intendedVehicle;
  }

  /**
   * Sets a reference to the vehicle that is intended to process this transport
   * order.
   *
   * @param vehicle The reference to the vehicle intended to process this order.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setIntendedVehicle(@Nullable TCSObjectReference<Vehicle> vehicle) {
    intendedVehicle = vehicle;
  }

  /**
   * Creates a copy of this object, with the given intended vehicle.
   *
   * @param intendedVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withIntendedVehicle(@Nullable TCSObjectReference<Vehicle> intendedVehicle) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns a reference to the vehicle currently processing this transport
   * order.
   *
   * @return A reference to the vehicle currently processing this transport
   * order. If this transport order is not currently being processed,
   * <code>null</code> is returned.
   */
  @Nullable
  public TCSObjectReference<Vehicle> getProcessingVehicle() {
    return processingVehicle;
  }

  /**
   * Sets a reference to the vehicle currently processing this transport order.
   *
   * @param vehicle The reference to the vehicle currently processing this
   * transport order.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setProcessingVehicle(@Nullable TCSObjectReference<Vehicle> vehicle) {
    processingVehicle = vehicle;
  }

  /**
   * Creates a copy of this object, with the given processing vehicle.
   *
   * @param processingVehicle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withProcessingVehicle(
      @Nullable TCSObjectReference<Vehicle> processingVehicle) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the set of transport orders this order depends on.
   *
   * @return The set of transport orders this order depends on.
   */
  public Set<TCSObjectReference<TransportOrder>> getDependencies() {
    return dependencies;
  }

  /**
   * Adds a dependency on another transport order.
   *
   * @param newDep A reference to the transport order that must be finished
   * before this one may be started.
   * @return <code>true</code> if, and only if, the given transport order was
   * not already a dependency for this one.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean addDependency(@Nonnull TCSObjectReference<TransportOrder> newDep) {
    requireNonNull(newDep, "newDep");
    return dependencies.add(newDep);
  }

  /**
   * Removes a dependency on another transport order.
   *
   * @param rmDep A reference to the transport order that is no longer a
   * dependency for this one.
   * @return <code>true</code> if, and only if, the given transport order was
   * a dependency for this one.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean removeDependency(@Nonnull TCSObjectReference<TransportOrder> rmDep) {
    requireNonNull(rmDep, "rmDep");
    return dependencies.remove(rmDep);
  }

  /**
   * Creates a copy of this object, with the given dependencies.
   *
   * @param dependencies The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDependencies(
      @Nonnull Set<TCSObjectReference<TransportOrder>> dependencies) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns a list of rejections for this transport order.
   *
   * @return A list of rejections for this transport order.
   */
  @Nonnull
  public List<Rejection> getRejections() {
    return Collections.unmodifiableList(rejections);
  }

  /**
   * Adds a rejection for this transport order.
   *
   * @param newRejection The new rejection.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void addRejection(@Nonnull Rejection newRejection) {
    requireNonNull(newRejection, "newRejection");
    rejections.add(newRejection);
  }

  /**
   * Creates a copy of this object, with the given rejection.
   *
   * @param rejection The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withRejection(@Nonnull Rejection rejection) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejectionsWithAppended(rejection),
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns a list of DriveOrders that have been processed already.
   *
   * @return A list of DriveOrders that have been processed already.
   */
  @Nonnull
  public List<DriveOrder> getPastDriveOrders() {
    List<DriveOrder> result = new ArrayList<>();
    for (int i = 0; i < currentDriveOrderIndex; i++) {
      result.add(driveOrders.get(i));
    }
    return result;
  }

  /**
   * Returns a list of DriveOrders that still need to be processed.
   *
   * @return A list of DriveOrders that still need to be processed.
   */
  @Nonnull
  public List<DriveOrder> getFutureDriveOrders() {
    List<DriveOrder> result = new ArrayList<>();
    for (int i = currentDriveOrderIndex + 1; i < driveOrders.size(); i++) {
      result.add(driveOrders.get(i));
    }
    return result;
  }

  /**
   * Copies drive order data from a list of drive orders to this transport
   * order's future drive orders.
   *
   * @param newOrders The drive orders containing the data to be copied into
   * this transport order's drive orders.
   * @throws IllegalArgumentException If the destinations of the given drive
   * orders do not match the destinations of the drive orders in this transport
   * order.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setFutureDriveOrders(@Nonnull List<DriveOrder> newOrders)
      throws IllegalArgumentException {
    requireNonNull(newOrders, "newOrders");

    checkState(currentDriveOrderIndex < 0,
               "Already processing drive order with index %s",
               currentDriveOrderIndex);

    int orderCount = newOrders.size();
    checkArgument(orderCount == driveOrders.size(),
                  "newOrders has wrong size: %s, should be %s",
                  orderCount,
                  driveOrders.size());
    // Check if the destinations of the given drive orders are equivalent to the
    // ones we have.
    for (int i = 0; i < orderCount; i++) {
      DriveOrder myOrder = driveOrders.get(i);
      DriveOrder newOrder = newOrders.get(i);
      checkArgument(myOrder.getDestination().equals(newOrder.getDestination()),
                    "newOrders' destinations do not equal mine");
    }
    // Copy the given drive orders' data to ours.
    for (int i = 0; i < orderCount; i++) {
      DriveOrder newOrder = newOrders.get(i);
      driveOrders.set(i,
                      driveOrders.get(i)
                          .withRoute(newOrder.getRoute())
                          .withState(newOrder.getState()));
    }
  }

  /**
   * Creates a copy of this object, with the given drive orders.
   *
   * @param driveOrders The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDriveOrders(@Nonnull List<DriveOrder> driveOrders) {
    requireNonNull(driveOrders, "driveOrders");

    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the current drive order, or <code>null</code>, if no drive order is
   * currently being processed.
   *
   * @return the current drive order, or <code>null</code>, if no drive order is
   * currently being processed.
   */
  @Nullable
  public DriveOrder getCurrentDriveOrder() {
    return (currentDriveOrderIndex >= 0 && currentDriveOrderIndex < driveOrders.size())
        ? driveOrders.get(currentDriveOrderIndex)
        : null;
  }

  /**
   * Returns a list of all drive orders, i.e. the past, current and future drive
   * orders.
   *
   * @return A list of all drive orders, i.e. the past, current and future drive
   * orders. If no drive orders exist, the returned list is empty.
   */
  @Nonnull
  public List<DriveOrder> getAllDriveOrders() {
    return new ArrayList<>(driveOrders);
  }

  /**
   * Makes the first of the future drive orders the current one.
   * Fails if there already is a current drive order or if the list of future
   * drive orders is empty.
   *
   * @throws IllegalStateException If there already is a current drive order or
   * if the list of future drive orders is empty.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setInitialDriveOrder()
      throws IllegalStateException {
    checkState(currentDriveOrderIndex < 0, "currentDriveOrder already set");
    checkState(!driveOrders.isEmpty(), "driveOrders is empty");

    currentDriveOrderIndex = 0;
  }

  /**
   * Marks the current drive order as finished, adds it to the list of past
   * drive orders and sets the current drive order to the next one of the list
   * of future drive orders (or <code>null</code>, if that list is empty).
   * If the current drive order is <code>null</code> because all drive orders
   * have been finished already or none has been started, yet, nothing happens.
   *
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setNextDriveOrder() {
    currentDriveOrderIndex++;
  }

  public int getCurrentDriveOrderIndex() {
    return currentDriveOrderIndex;
  }

  /**
   * Creates a copy of this object, with the given drive order index.
   *
   * @param currentDriveOrderIndex The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withCurrentDriveOrderIndex(int currentDriveOrderIndex) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Sets the current drive order's state to the given one.
   *
   * @param newState The current drive order's new state.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setCurrentDriveOrderState(@Nonnull DriveOrder.State newState) {
    requireNonNull(newState, "newState");
    if (currentDriveOrderIndex < 0 || currentDriveOrderIndex >= driveOrders.size()) {
      LOG.warn("currentDriveOrder is null");
      return;
    }

    driveOrders.set(currentDriveOrderIndex,
                    driveOrders.get(currentDriveOrderIndex).withState(newState));
  }

  /**
   * Creates a copy of this object, with the given current drive order state.
   *
   * @param driveOrderState The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withCurrentDriveOrderState(@Nonnull DriveOrder.State driveOrderState) {
    requireNonNull(driveOrderState, "driveOrderState");

    List<DriveOrder> driveOrders = new ArrayList<>(this.driveOrders);
    driveOrders.set(currentDriveOrderIndex,
                    driveOrders.get(currentDriveOrderIndex).withState(driveOrderState));

    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Returns the order sequence this order belongs to, or <code>null</code>, if
   * it doesn't belong to any sequence.
   *
   * @return The order sequence this order belongs to, or <code>null</code>, if
   * it doesn't belong to any sequence.
   */
  @Nullable
  public TCSObjectReference<OrderSequence> getWrappingSequence() {
    return wrappingSequence;
  }

  /**
   * Sets the order sequence this transport order belongs to.
   *
   * @param sequence The order sequence this order belongs to. May be
   * <code>null</code> to indicate that this order does not belong to any
   * sequence.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setWrappingSequence(@Nullable TCSObjectReference<OrderSequence> sequence) {
    wrappingSequence = sequence;
  }

  /**
   * Creates a copy of this object, with the given wrapping sequence.
   *
   * @param wrappingSequence The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withWrappingSequence(
      @Nullable TCSObjectReference<OrderSequence> wrappingSequence) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * Checks if this order is dispensable.
   *
   * @return <code>true</code> if, and only if, this order is dispensable.
   */
  public boolean isDispensable() {
    return dispensable;
  }

  /**
   * Sets this order's <em>dispensable</em> flag.
   *
   * @param dispensable This order's new <em>dispensable</em> flag.
   * @deprecated Will become immutable.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setDispensable(boolean dispensable) {
    this.dispensable = dispensable;
  }

  /**
   * Creates a copy of this object, with the given dispensable flag.
   *
   * @param dispensable The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrder withDispensable(boolean dispensable) {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrder clone() {
    return new TransportOrder(getIdWithoutDeprecationWarning(),
                              getName(),
                              getProperties(),
                              category,
                              driveOrders,
                              currentDriveOrderIndex,
                              creationTime,
                              intendedVehicle,
                              deadline,
                              dispensable,
                              wrappingSequence,
                              dependencies,
                              rejections,
                              processingVehicle,
                              state,
                              finishedTime);
  }

  @Override
  public String toString() {
    return "TransportOrder{"
        + "name=" + getName()
        + ", state=" + state
        + ", intendedVehicle=" + intendedVehicle
        + ", processingVehicle=" + processingVehicle
        + ", creationTime=" + creationTime
        + ", deadline=" + deadline
        + ", finishedTime=" + finishedTime
        + ", wrappingSequence=" + wrappingSequence
        + ", dispensable=" + dispensable
        + ", category=" + category
        + ", dependencies=" + dependencies
        + ", driveOrders=" + driveOrders
        + ", currentDriveOrderIndex=" + currentDriveOrderIndex
        + ", rejections=" + rejections
        + '}';
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
  }

  private List<Rejection> rejectionsWithAppended(@Nonnull Rejection rejection) {
    List<Rejection> result = new ArrayList<>(rejections.size() + 1);
    result.addAll(rejections);
    result.add(rejection);
    return result;
  }

  private static List<DriveOrder> createDriveOrders(List<DriveOrder.Destination> destinations) {
    List<DriveOrder> result = new ArrayList<>(destinations.size());
    for (DriveOrder.Destination curDest : destinations) {
      result.add(new DriveOrder(curDest));
    }
    return result;
  }

  /**
   * This enumeration defines the various states a transport order may be in.
   */
  public enum State {

    /**
     * A transport order's initial state.
     * A transport order remains in this state until its parameters have been
     * set up completely.
     */
    RAW,
    /**
     * Set (by a user/client) when a transport order's parameters have been set
     * up completely and the kernel should dispatch it when possible.
     */
    ACTIVE,
    /**
     * Marks a transport order as ready to be dispatched to a vehicle (i.e. all
     * its dependencies have been finished).
     */
    DISPATCHABLE,
    /**
     * Marks a transport order as being processed by a vehicle.
     */
    BEING_PROCESSED,
    /**
     * Indicates the transport order is withdrawn from a processing vehicle but
     * not yet in its final state (which will be FAILED), as the vehicle has not
     * yet finished/cleaned up.
     */
    WITHDRAWN,
    /**
     * Marks a transport order as successfully completed.
     */
    FINISHED,
    /**
     * General failure state that marks a transport order as failed.
     */
    FAILED,
    /**
     * Failure state that marks a transport order as unroutable, i.e. it is
     * impossible to find a route that would allow a vehicle to process the
     * transport order completely.
     */
    UNROUTABLE;

    /**
     * Checks if this state is a final state for a transport order.
     *
     * @return <code>true</code> if, and only if, this state is a final state
     * for a transport order - i.e. FINISHED, FAILED or UNROUTABLE.
     */
    public boolean isFinalState() {
      return this.equals(FINISHED)
          || this.equals(FAILED)
          || this.equals(UNROUTABLE);
    }
  }
}
