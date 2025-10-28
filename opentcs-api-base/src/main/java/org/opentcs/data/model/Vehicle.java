// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;

import jakarta.annotation.Nonnull;
import java.awt.Color;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Describes a vehicle's current state.
 */
public class Vehicle
    extends
      TCSObject<Vehicle>
    implements
      Serializable {

  /**
   * The key for a property to store the class name of the preferred communication adapter (factory)
   * for this vehicle.
   */
  public static final String PREFERRED_ADAPTER = "tcs:preferredAdapterClass";
  /**
   * The vehicle's bounding box (in mm).
   */
  private final BoundingBox boundingBox;
  /**
   * Contains information regarding the energy level threshold values of the vehicle.
   */
  private final EnergyLevelThresholdSet energyLevelThresholdSet;
  /**
   * This vehicle's remaining energy (in percent of the maximum).
   */
  private final int energyLevel;
  /**
   * This vehicle's maximum velocity (in mm/s).
   */
  private final int maxVelocity;
  /**
   * This vehicle's maximum reverse velocity (in mm/s).
   */
  private final int maxReverseVelocity;
  /**
   * The operation the vehicle's current communication adapter accepts as a command to recharge the
   * vehicle.
   */
  private final String rechargeOperation;
  /**
   * The current (state of the) load handling devices of this vehicle.
   */
  private final List<LoadHandlingDevice> loadHandlingDevices;
  /**
   * This vehicle's current state.
   */
  private final TimestampedAttribute<State> state;
  /**
   * This vehicle's current processing state.
   */
  private final TimestampedAttribute<ProcState> procState;
  /**
   * This vehicle's integration level.
   */
  private final IntegrationLevel integrationLevel;
  /**
   * Whether this vehicle is currently paused.
   */
  private final boolean paused;
  /**
   * A reference to the transport order this vehicle is currently processing.
   */
  private final TCSObjectReference<TransportOrder> transportOrder;
  /**
   * A reference to the order sequence this vehicle is currently processing.
   */
  private final TCSObjectReference<OrderSequence> orderSequence;
  /**
   * The set of transport order types this vehicle is allowed to process, and their priorities.
   */
  private final Set<AcceptableOrderType> acceptableOrderTypes;
  /**
   * The resources this vehicle has claimed for future allocation.
   */
  private final List<Set<TCSResourceReference<?>>> claimedResources;
  /**
   * The resources this vehicle has allocated.
   */
  private final List<Set<TCSResourceReference<?>>> allocatedResources;
  /**
   * A reference to the point which this vehicle currently occupies.
   */
  private final TCSObjectReference<Point> currentPosition;
  /**
   * The vehicle's pose containing its precise position and current orientation angle.
   */
  private final Pose pose;
  /**
   * The key for selecting the envelope to be used for resources the vehicle occupies.
   */
  private final String envelopeKey;
  /**
   * The information regarding the graphical representation of this vehicle.
   */
  private final Layout layout;

  /**
   * Creates a new vehicle.
   *
   * @param name The new vehicle's name.
   */
  public Vehicle(String name) {
    super(name);
    this.boundingBox = new BoundingBox(1000, 1000, 1000);
    this.energyLevelThresholdSet = new EnergyLevelThresholdSet(30, 90, 30, 90);
    this.maxVelocity = 1000;
    this.maxReverseVelocity = 1000;
    this.rechargeOperation = "CHARGE";
    this.procState = new TimestampedAttribute<>(ProcState.IDLE, Instant.now());
    this.transportOrder = null;
    this.orderSequence = null;
    this.acceptableOrderTypes = Set.of(new AcceptableOrderType(OrderConstants.TYPE_ANY, 0));
    this.claimedResources = List.of();
    this.allocatedResources = List.of();
    this.state = new TimestampedAttribute<>(State.UNKNOWN, Instant.now());
    this.integrationLevel = IntegrationLevel.TO_BE_RESPECTED;
    this.paused = false;
    this.currentPosition = null;
    this.pose = new Pose(null, Double.NaN);
    this.energyLevel = 100;
    this.loadHandlingDevices = List.of();
    this.envelopeKey = "";
    this.layout = new Layout();
  }

  private Vehicle(
      String name,
      Map<String, String> properties,
      ObjectHistory history,
      BoundingBox boundingBox,
      EnergyLevelThresholdSet energyLevelThresholdSet,
      int maxVelocity,
      int maxReverseVelocity,
      String rechargeOperation,
      TimestampedAttribute<ProcState> procState,
      TCSObjectReference<TransportOrder> transportOrder,
      TCSObjectReference<OrderSequence> orderSequence,
      Set<AcceptableOrderType> acceptableOrderTypes,
      List<Set<TCSResourceReference<?>>> claimedResources,
      List<Set<TCSResourceReference<?>>> allocatedResources,
      TimestampedAttribute<State> state,
      IntegrationLevel integrationLevel,
      boolean paused,
      TCSObjectReference<Point> currentPosition,
      Pose pose,
      int energyLevel,
      List<LoadHandlingDevice> loadHandlingDevices,
      String envelopeKey,
      Layout layout
  ) {
    super(name, properties, history);
    this.boundingBox = requireNonNull(boundingBox, "boundingBox");
    this.energyLevelThresholdSet = requireNonNull(
        energyLevelThresholdSet, "energyLevelThresholdSet"
    );
    checkArgument(
        energyLevelThresholdSet.getEnergyLevelCritical()
            <= energyLevelThresholdSet.getEnergyLevelGood(),
        "energyLevelCritical (%s) not <= energyLevelGood (%s)",
        energyLevelThresholdSet.getEnergyLevelCritical(),
        energyLevelThresholdSet.getEnergyLevelGood()
    );
    checkArgument(
        energyLevelThresholdSet.getEnergyLevelSufficientlyRecharged()
            <= energyLevelThresholdSet.getEnergyLevelFullyRecharged(),
        "energyLevelSufficientlyRecharged (%s) not <= energyLevelFullyRecharged (%s)",
        energyLevelThresholdSet.getEnergyLevelSufficientlyRecharged(),
        energyLevelThresholdSet.getEnergyLevelFullyRecharged()
    );
    this.maxVelocity = checkInRange(maxVelocity, 0, Integer.MAX_VALUE, "maxVelocity");
    this.maxReverseVelocity = checkInRange(
        maxReverseVelocity,
        0,
        Integer.MAX_VALUE,
        "maxReverseVelocity"
    );
    this.rechargeOperation = requireNonNull(rechargeOperation, "rechargeOperation");
    this.procState = requireNonNull(procState, "procState");
    this.transportOrder = transportOrder;
    this.orderSequence = orderSequence;
    this.acceptableOrderTypes = requireNonNull(acceptableOrderTypes, "acceptableOrderTypes");
    this.claimedResources = requireNonNull(claimedResources, "claimedResources");
    this.allocatedResources = requireNonNull(allocatedResources, "allocatedResources");
    this.state = requireNonNull(state, "state");
    this.integrationLevel = requireNonNull(integrationLevel, "integrationLevel");
    this.paused = paused;
    this.currentPosition = currentPosition;
    this.pose = requireNonNull(pose, "pose");
    this.energyLevel = checkInRange(energyLevel, 0, 100, "energyLevel");
    this.loadHandlingDevices = requireNonNull(loadHandlingDevices, "loadHandlingDevices");
    this.envelopeKey = requireNonNull(envelopeKey, "envelopeKey");
    this.layout = requireNonNull(layout, "layout");
  }

  @Override
  public Vehicle withProperty(String key, String value) {
    return new Vehicle(
        getName(),
        propertiesWith(key, value),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  @Override
  public Vehicle withProperties(Map<String, String> properties) {
    return new Vehicle(
        getName(),
        mapWithoutNullValues(properties),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  @Override
  public Vehicle withHistoryEntry(ObjectHistory.Entry entry) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory().withEntryAppended(entry),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  @Override
  public Vehicle withHistory(ObjectHistory history) {
    return new Vehicle(
        getName(),
        getProperties(),
        history,
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns this vehicle's remaining energy (in percent of the maximum).
   *
   * @return This vehicle's remaining energy.
   */
  public int getEnergyLevel() {
    return energyLevel;
  }

  /**
   * Creates a copy of this object, with the given energy level.
   *
   * @param energyLevel The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withEnergyLevel(int energyLevel) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Checks whether the vehicle's energy level is critical.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * critical.
   */
  public boolean isEnergyLevelCritical() {
    return energyLevel <= energyLevelThresholdSet.getEnergyLevelCritical();
  }

  /**
   * Checks whether the vehicle's energy level is degraded (not <em>good</em>
   * any more).
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * degraded.
   */
  public boolean isEnergyLevelDegraded() {
    return energyLevel <= energyLevelThresholdSet.getEnergyLevelGood();
  }

  /**
   * Checks whether the vehicle's energy level is good.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * good.
   */
  public boolean isEnergyLevelGood() {
    return energyLevel > energyLevelThresholdSet.getEnergyLevelGood();
  }

  /**
   * Checks whether the vehicle's energy level is fully recharged.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * fully recharged.
   */
  public boolean isEnergyLevelFullyRecharged() {
    return energyLevel >= energyLevelThresholdSet.getEnergyLevelFullyRecharged();
  }

  /**
   * Checks whether the vehicle's energy level is sufficiently recharged.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * sufficiently recharged.
   */
  public boolean isEnergyLevelSufficientlyRecharged() {
    return energyLevel >= energyLevelThresholdSet.getEnergyLevelSufficientlyRecharged();
  }

  /**
   * Returns this vehicle's energy level threshold set.
   *
   * @return This vehicle's energy level threshold set.
   */
  public EnergyLevelThresholdSet getEnergyLevelThresholdSet() {
    return energyLevelThresholdSet;
  }

  /**
   * Creates a copy of this object, with the given EnergyLevelThresholdSet.
   *
   * @param energyLevelThresholdSet The new EnergyLevelThresholdSet.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withEnergyLevelThresholdSet(EnergyLevelThresholdSet energyLevelThresholdSet) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the operation that the vehicle's current communication adapter
   * accepts as a command to recharge the vehicle.
   *
   * @return The operation that the vehicle's current communication adapter
   * accepts as a command to recharge the vehicle.
   */
  public String getRechargeOperation() {
    return rechargeOperation;
  }

  /**
   * Creates a copy of this object, with the given recharge operation.
   *
   * @param rechargeOperation The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withRechargeOperation(String rechargeOperation) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the current (state of the) load handling devices of this vehicle.
   *
   * @return The current (state of the) load handling devices of this vehicle.
   */
  public List<LoadHandlingDevice> getLoadHandlingDevices() {
    return loadHandlingDevices;
  }

  /**
   * Creates a copy of this object, with the given load handling devices.
   *
   * @param loadHandlingDevices The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withLoadHandlingDevices(List<LoadHandlingDevice> loadHandlingDevices) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        listWithoutNullValues(loadHandlingDevices),
        envelopeKey,
        layout
    );
  }

  /**
   * Returns this vehicle's maximum velocity (in mm/s).
   *
   * @return This vehicle's maximum velocity (in mm/s).
   */
  public int getMaxVelocity() {
    return maxVelocity;
  }

  /**
   * Creates a copy of this object, with the given maximum velocity.
   *
   * @param maxVelocity The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withMaxVelocity(int maxVelocity) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns this vehicle's maximum reverse velocity (in mm/s).
   *
   * @return This vehicle's maximum reverse velocity (in mm/s).
   */
  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  /**
   * Creates a copy of this object, with the given maximum reverse velocity.
   *
   * @param maxReverseVelocity The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withMaxReverseVelocity(int maxReverseVelocity) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns this vehicle's current state.
   *
   * @return This vehicle's current state.
   */
  public State getState() {
    return state.attribute();
  }

  /**
   * Returns the time at which the vehicle has entered its current state.
   *
   * @return The time at which the vehicle has entered its current state.
   */
  public Instant getStateTimestamp() {
    return state.timestamp();
  }

  /**
   * Checks if this vehicle's current state is equal to the given one.
   *
   * @param otherState The state to compare to this vehicle's one.
   * @return <code>true</code> if, and only if, the given state is equal to this
   * vehicle's one.
   */
  public boolean hasState(State otherState) {
    return state.attribute().equals(otherState);
  }

  /**
   * Creates a copy of this object, with the given state.
   *
   * @param state The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withState(State state) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        new TimestampedAttribute<>(state, Instant.now()),
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns this vehicle's current processing state.
   *
   * @return This vehicle's current processing state.
   */
  public ProcState getProcState() {
    return procState.attribute();
  }

  /**
   * Returns the time at which the vehicle has entered its current processing state.
   *
   * @return The time at which the vehicle has entered its current processing state.
   */
  public Instant getProcStateTimestamp() {
    return procState.timestamp();
  }

  /**
   * Returns this vehicle's integration level.
   *
   * @return This vehicle's integration level.
   */
  public IntegrationLevel getIntegrationLevel() {
    return integrationLevel;
  }

  /**
   * Creates a copy of this object, with the given integration level.
   *
   * @param integrationLevel The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withIntegrationLevel(IntegrationLevel integrationLevel) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Indicates whether this vehicle is paused.
   *
   * @return Whether this vehicle is paused.
   */
  public boolean isPaused() {
    return paused;
  }

  /**
   * Creates a copy of this object, with the given paused state.
   *
   * @param paused The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withPaused(boolean paused) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Checks if this vehicle's current processing state is equal to the given
   * one.
   *
   * @param otherState The state to compare to this vehicle's one.
   * @return <code>true</code> if, and only if, the given state is equal to this
   * vehicle's one.
   */
  public boolean hasProcState(ProcState otherState) {
    return procState.attribute().equals(otherState);
  }

  /**
   * Creates a copy of this object, with the given processing state.
   *
   * @param procState The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withProcState(ProcState procState) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        new TimestampedAttribute<>(procState, Instant.now()),
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the vehicle's current bounding box (in mm).
   * <p>
   * The bounding box is oriented so that its longitudinal axis runs parallel to the longitudinal
   * axis of the vehicle. For the reference point offset, positive x values indicate an offset in
   * the forward direction of the vehicle, positive y values an offset towards the left-hand side.
   * </p>
   *
   * @return The vehicle's current bounding box (in mm).
   */
  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  /**
   * Creates a copy of this object, with the given bounding box (in mm).
   * <p>
   * The bounding box is oriented so that its longitudinal axis runs parallel to the longitudinal
   * axis of the vehicle. For the reference point offset, positive x values indicate an offset in
   * the forward direction of the vehicle, positive y values an offset towards the left-hand side.
   * </p>
   *
   * @param boundingBox The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withBoundingBox(BoundingBox boundingBox) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns a reference to the transport order this vehicle is currently
   * processing.
   *
   * @return A reference to the transport order this vehicle is currently
   * processing, or <code>null</code>, if it is not processing any transport
   * order at the moment.
   */
  public TCSObjectReference<TransportOrder> getTransportOrder() {
    return transportOrder;
  }

  /**
   * Creates a copy of this object, with the given transport order.
   *
   * @param transportOrder The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withTransportOrder(TCSObjectReference<TransportOrder> transportOrder) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns a reference to the order sequence this vehicle is currently
   * processing.
   *
   * @return A reference to the order sequence this vehicle is currently
   * processing, or <code>null</code>, if it is not processing any order
   * sequence at the moment.
   */
  public TCSObjectReference<OrderSequence> getOrderSequence() {
    return orderSequence;
  }

  /**
   * Creates a copy of this object, with the given order sequence.
   *
   * @param orderSequence The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withOrderSequence(TCSObjectReference<OrderSequence> orderSequence) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the set of order types this vehicle is allowed to process.
   *
   * @return The set of order types this vehicle is allowed to process, and their priorities.
   */
  public Set<AcceptableOrderType> getAcceptableOrderTypes() {
    return acceptableOrderTypes;
  }

  /**
   * Creates a copy of this object, with the given set of acceptable order types.
   *
   * @param acceptableOrderTypes The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withAcceptableOrderTypes(Set<AcceptableOrderType> acceptableOrderTypes) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        setWithoutNullValues(acceptableOrderTypes),
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the resources this vehicle has claimed for future allocation.
   *
   * @return The resources this vehicle has claimed for future allocation.
   */
  public List<Set<TCSResourceReference<?>>> getClaimedResources() {
    return claimedResources;
  }

  /**
   * Creates a copy of this object, with the given claimed resources.
   *
   * @param claimedResources The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withClaimedResources(List<Set<TCSResourceReference<?>>> claimedResources) {
    requireNonNull(claimedResources, "claimedResources");

    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources.stream().map(Set::copyOf).toList(),
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the resources this vehicle has allocated.
   *
   * @return The resources this vehicle has allocated.
   */
  public List<Set<TCSResourceReference<?>>> getAllocatedResources() {
    return allocatedResources;
  }

  /**
   * Creates a copy of this object, with the given allocated resources.
   *
   * @param allocatedResources The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withAllocatedResources(List<Set<TCSResourceReference<?>>> allocatedResources) {
    requireNonNull(allocatedResources, "allocatedResources");

    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources.stream().map(Set::copyOf).toList(),
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns a reference to the point this vehicle currently occupies.
   *
   * @return A reference to the point this vehicle currently occupies, or
   * <code>null</code>, if this vehicle's position is unknown or the vehicle is
   * currently not in the system.
   */
  public TCSObjectReference<Point> getCurrentPosition() {
    return currentPosition;
  }

  /**
   * Creates a copy of this object, with the given current position.
   *
   * @param currentPosition The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withCurrentPosition(TCSObjectReference<Point> currentPosition) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the vehicle's pose containing the precise position and orientation angle.
   *
   * @return The vehicle's pose.
   */
  @Nonnull
  public Pose getPose() {
    return pose;
  }

  /**
   * Creates a copy of this object, with the given pose.
   *
   * @param pose The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withPose(
      @Nonnull
      Pose pose
  ) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the key for selecting the envelope to be used for resources the vehicle occupies.
   *
   * @return The key for selecting the envelope to be used for resources the vehicle occupies.
   */
  @Nonnull
  public String getEnvelopeKey() {
    return envelopeKey;
  }

  /**
   * Creates a copy of this object, with the given envelope key.
   *
   * @param envelopeKey The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withEnvelopeKey(
      @Nonnull
      String envelopeKey
  ) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Returns the information regarding the graphical representation of this vehicle.
   *
   * @return The information regarding the graphical representation of this vehicle.
   */
  public Layout getLayout() {
    return layout;
  }

  /**
   * Creates a copy of this object, with the given layout.
   *
   * @param layout The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withLayout(Layout layout) {
    return new Vehicle(
        getName(),
        getProperties(),
        getHistory(),
        boundingBox,
        energyLevelThresholdSet,
        maxVelocity,
        maxReverseVelocity,
        rechargeOperation,
        procState,
        transportOrder,
        orderSequence,
        acceptableOrderTypes,
        claimedResources,
        allocatedResources,
        state,
        integrationLevel,
        paused,
        currentPosition,
        pose,
        energyLevel,
        loadHandlingDevices,
        envelopeKey,
        layout
    );
  }

  /**
   * Checks if this vehicle is currently processing any transport order.
   *
   * @return <code>true</code> if, and only if, this vehicle is currently
   * processing a transport order.
   */
  public boolean isProcessingOrder() {
    return transportOrder != null;
  }

  @Override
  public String toString() {
    return "Vehicle{"
        + "name=" + getName()
        + ", currentPosition=" + currentPosition
        + ", pose=" + pose
        + ", state=" + state.attribute()
        + ", stateTimestamp=" + state.timestamp()
        + ", energyLevel=" + energyLevel
        + ", integrationLevel=" + integrationLevel
        + ", paused=" + paused
        + ", procState=" + procState.attribute()
        + ", procStateTimestamp=" + procState.timestamp()
        + ", loadHandlingDevices=" + loadHandlingDevices
        + ", boundingBox=" + boundingBox
        + ", transportOrder=" + transportOrder
        + ", claimedResources=" + claimedResources
        + ", allocatedResources=" + allocatedResources
        + ", orderSequence=" + orderSequence
        + ", energyLevelThresholdSet=" + energyLevelThresholdSet
        + ", maxVelocity=" + maxVelocity
        + ", maxReverseVelocity=" + maxReverseVelocity
        + ", rechargeOperation=" + rechargeOperation
        + ", acceptableOrderTypes=" + acceptableOrderTypes
        + ", envelopeKey=" + envelopeKey
        + ", properties=" + getProperties()
        + ", history=" + getHistory()
        + '}';
  }

  /**
   * The elements of this enumeration describe the various possible states of a
   * vehicle.
   */
  public enum State {

    /**
     * The vehicle's current state is unknown, e.g. because communication with
     * it is currently not possible for some reason.
     */
    UNKNOWN,
    /**
     * The vehicle's state is known and it's not in an error state, but it is
     * not available for receiving orders.
     */
    UNAVAILABLE,
    /**
     * There is a problem with the vehicle.
     */
    ERROR,
    /**
     * The vehicle is currently idle/available for processing movement orders.
     */
    IDLE,
    /**
     * The vehicle is processing a movement order.
     */
    EXECUTING,
    /**
     * The vehicle is currently recharging its battery/refilling fuel.
     */
    CHARGING
  }

  /**
   * A vehicle's state of integration into the system.
   */
  public enum IntegrationLevel {

    /**
     * The vehicle's reported position is ignored.
     */
    TO_BE_IGNORED,
    /**
     * The vehicle's reported position is noticed, meaning that resources will not be reserved for
     * it.
     */
    TO_BE_NOTICED,
    /**
     * The vehicle's reported position is respected, meaning that resources will be reserved for it.
     */
    TO_BE_RESPECTED,
    /**
     * The vehicle is fully integrated and may be assigned to transport orders.
     */
    TO_BE_UTILIZED
  }

  /**
   * A vehicle's processing state as seen by the dispatcher.
   */
  public enum ProcState {

    /**
     * The vehicle is currently not processing a transport order.
     */
    IDLE,
    /**
     * The vehicle is currently processing a transport order and is waiting for
     * the next drive order to be assigned to it.
     */
    AWAITING_ORDER,
    /**
     * The vehicle is currently processing a drive order.
     */
    PROCESSING_ORDER
  }

  /**
   * The elements of this enumeration represent the possible orientations of a
   * vehicle.
   */
  public enum Orientation {

    /**
     * Indicates that the vehicle is driving/standing oriented towards its
     * front.
     */
    FORWARD,
    /**
     * Indicates that the vehicle is driving/standing oriented towards its
     * back.
     */
    BACKWARD,
    /**
     * Indicates that the vehicle's orientation is undefined/unknown.
     */
    UNDEFINED
  }

  /**
   * Contains information regarding the graphical representation of a vehicle.
   */
  public static class Layout
      implements
        Serializable {

    /**
     * The color in which vehicle routes are to be emphasized.
     */
    private final Color routeColor;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(Color.RED);
    }

    /**
     * Creates a new instance.
     *
     * @param routeColor The color in which vehicle routes are to be emphasized.
     */
    public Layout(Color routeColor) {
      this.routeColor = requireNonNull(routeColor, "routeColor");
    }

    /**
     * Returns the color in which vehicle routes are to be emphasized.
     *
     * @return The color in which vehicle routes are to be emphasized.
     */
    public Color getRouteColor() {
      return routeColor;
    }

    /**
     * Creates a copy of this object, with the given color.
     *
     * @param routeColor The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withRouteColor(Color routeColor) {
      return new Layout(routeColor);
    }
  }

  /**
   * Contains information regarding the energy level threshold values of a vehicle.
   */
  public static class EnergyLevelThresholdSet
      implements
        Serializable {

    private final int energyLevelCritical;
    private final int energyLevelGood;
    private final int energyLevelSufficientlyRecharged;
    private final int energyLevelFullyRecharged;

    /**
     * Creates a new instance.
     *
     * @param energyLevelCritical The value at/below which the vehicle's energy level is considered
     * "critical".
     * @param energyLevelGood The value at/above which the vehicle's energy level is considered
     * "good".
     * @param energyLevelSufficientlyRecharged The value at/above which the vehicle's energy level
     * is considered fully recharged.
     * @param energyLevelFullyRecharged The value at/above which the vehicle's energy level is
     * considered sufficiently recharged.
     */
    public EnergyLevelThresholdSet(
        int energyLevelCritical,
        int energyLevelGood,
        int energyLevelSufficientlyRecharged,
        int energyLevelFullyRecharged
    ) {
      this.energyLevelCritical = checkInRange(
          energyLevelCritical,
          0,
          100,
          "energyLevelCritical"
      );
      this.energyLevelGood = checkInRange(
          energyLevelGood,
          0,
          100,
          "energyLevelGood"
      );
      this.energyLevelSufficientlyRecharged = checkInRange(
          energyLevelSufficientlyRecharged,
          0,
          100,
          "energyLevelSufficientlyRecharged"
      );
      this.energyLevelFullyRecharged = checkInRange(
          energyLevelFullyRecharged,
          0,
          100,
          "energyLevelFullyRecharged"
      );
    }

    /**
     * Returns the vehicle's critical energy level (in percent of the maximum).
     * <p>
     * The critical energy level is the one at/below which the vehicle should be recharged.
     * </p>
     *
     * @return The vehicle's critical energy level.
     */
    public int getEnergyLevelCritical() {
      return energyLevelCritical;
    }

    /**
     * Creates a copy of this object, with the given critical energy level.
     *
     * @param energyLevelCritical The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelCritical(int energyLevelCritical) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    /**
     * Returns the vehicle's good energy level (in percent of the maximum).
     * <p>
     * The good energy level is the one at/above which the vehicle can be dispatched again when
     * charging.
     * </p>
     *
     * @return The vehicle's good energy level.
     */
    public int getEnergyLevelGood() {
      return energyLevelGood;
    }

    /**
     * Creates a copy of this object, with the given good energy level.
     *
     * @param energyLevelGood The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelGood(int energyLevelGood) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    /**
     * Returns the vehicle's energy level for being sufficiently recharged (in percent of the
     * maximum).
     *
     * @return This vehicle's sufficiently recharged energy level.
     */
    public int getEnergyLevelSufficientlyRecharged() {
      return energyLevelSufficientlyRecharged;
    }

    /**
     * Creates a copy of this object, with the given sufficiently recharged energy level.
     *
     * @param energyLevelSufficientlyRecharged The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelSufficientlyRecharged(
        int energyLevelSufficientlyRecharged
    ) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    /**
     * Returns the vehicle's energy level for being fully recharged (in percent of the maximum).
     *
     * @return The vehicle's fully recharged threshold.
     */
    public int getEnergyLevelFullyRecharged() {
      return energyLevelFullyRecharged;
    }

    /**
     * Creates a copy of this object, with the given fully recharged energy level.
     *
     * @param energyLevelFullyRecharged The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public EnergyLevelThresholdSet withEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
      return new EnergyLevelThresholdSet(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof EnergyLevelThresholdSet other)) {
        return false;
      }

      return energyLevelCritical == other.getEnergyLevelCritical()
          && energyLevelGood == other.getEnergyLevelGood()
          && energyLevelSufficientlyRecharged == other.getEnergyLevelSufficientlyRecharged()
          && energyLevelFullyRecharged == other.getEnergyLevelFullyRecharged();
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          energyLevelCritical,
          energyLevelGood,
          energyLevelSufficientlyRecharged,
          energyLevelFullyRecharged
      );
    }

    @Override
    public String toString() {
      return "EnergyLevelThresholdSet{"
          + "energyLevelCritical=" + energyLevelCritical
          + ", energyLevelGood=" + energyLevelGood
          + ", energyLevelSufficientlyRecharged=" + energyLevelSufficientlyRecharged
          + ", energyLevelFullyRecharged=" + energyLevelFullyRecharged
          + '}';
    }
  }

  /**
   * Contains an attribute and complements it with a timestamp that refers to the time at which the
   * attribute was set.
   *
   * @param attribute The attribute.
   * @param timestamp The time at which the attribute was set.
   * @param <T> The type of the attribute
   */
  private record TimestampedAttribute<T>(T attribute, Instant timestamp)
      implements
        Serializable {}
}
