/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nullable;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import static org.opentcs.util.Assertions.checkArgument;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes a vehicle's current state.
 */
public class Vehicle
    extends TCSObject<Vehicle>
    implements Serializable {

  /**
   * A value indicating that no route steps have been travelled for the current drive order, yet.
   *
   * @deprecated Use {@link TransportOrder#ROUTE_STEP_INDEX_DEFAULT} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public static final int ROUTE_INDEX_DEFAULT = -1;
  /**
   * The key for a property to store the class name of the preferred communication adapter (factory)
   * for this vehicle.
   */
  public static final String PREFERRED_ADAPTER = "tcs:preferredAdapterClass";
  /**
   * This vehicle's length (in mm).
   */
  private final int length;
  /**
   * The value at/above which the vehicle's energy level is considered "good".
   */
  private final int energyLevelGood;
  /**
   * The value at/below which the vehicle's energy level is considered "critical".
   */
  private final int energyLevelCritical;
  /**
   * The value at/above which the vehicle's energy level is considered fully recharged.
   */
  private final int energyLevelFullyRecharged;
  /**
   * The value at/above which the vehicle's energy level is considered sufficiently recharged.
   */
  private final int energyLevelSufficientlyRecharged;
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
  private final State state;
  /**
   * This vehicle's current processing state.
   */
  private final ProcState procState;
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
   * The set of transport order types this vehicle is allowed to process.
   */
  private final Set<String> allowedOrderTypes;
  /**
   * The index of the last route step travelled for the current drive order of the current transport
   * order.
   */
  private final int routeProgressIndex;
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
   * A reference to the point which this vehicle is expected to be seen at next.
   */
  private final TCSObjectReference<Point> nextPosition;
  /**
   * The vehicle's precise position in world coordinates [mm], independent from logical
   * positions/point names.
   * Set to <code>null</code> if the vehicle hasn't provided a precise position.
   */
  private final Triple precisePosition;
  /**
   * The vehicle's current orientation angle (-360..360).
   * Set to <code>Double.NaN</code> if the vehicle hasn't provided an orientation angle.
   */
  private final double orientationAngle;
  /**
   * The key for selecting the envelope to be used for resources the vehicle occupies.
   */
  private final String envelopeKey;
  /**
   * The information regarding the grahical representation of this vehicle.
   */
  private final Layout layout;

  /**
   * Creates a new vehicle.
   *
   * @param name The new vehicle's name.
   */
  public Vehicle(String name) {
    super(name);
    this.length = 1000;
    this.energyLevelGood = 90;
    this.energyLevelCritical = 30;
    this.energyLevelFullyRecharged = 90;
    this.energyLevelSufficientlyRecharged = 30;
    this.maxVelocity = 1000;
    this.maxReverseVelocity = 1000;
    this.rechargeOperation = "CHARGE";
    this.procState = ProcState.IDLE;
    this.transportOrder = null;
    this.orderSequence = null;
    this.allowedOrderTypes = new HashSet<>(Arrays.asList(OrderConstants.TYPE_ANY));
    this.routeProgressIndex = ROUTE_INDEX_DEFAULT;
    this.claimedResources = List.of();
    this.allocatedResources = List.of();
    this.state = State.UNKNOWN;
    this.integrationLevel = IntegrationLevel.TO_BE_RESPECTED;
    this.paused = false;
    this.currentPosition = null;
    this.nextPosition = null;
    this.precisePosition = null;
    this.orientationAngle = Double.NaN;
    this.energyLevel = 100;
    this.loadHandlingDevices = List.of();
    this.envelopeKey = null;
    this.layout = new Layout();
  }

  private Vehicle(String name,
                  Map<String, String> properties,
                  ObjectHistory history,
                  int length,
                  int energyLevelGood,
                  int energyLevelCritical,
                  int energyLevelFullyRecharged,
                  int energyLevelSufficientlyRecharged,
                  int maxVelocity,
                  int maxReverseVelocity,
                  String rechargeOperation,
                  ProcState procState,
                  TCSObjectReference<TransportOrder> transportOrder,
                  TCSObjectReference<OrderSequence> orderSequence,
                  Set<String> allowedOrderTypes,
                  int routeProgressIndex,
                  List<Set<TCSResourceReference<?>>> claimedResources,
                  List<Set<TCSResourceReference<?>>> allocatedResources,
                  State state,
                  IntegrationLevel integrationLevel,
                  boolean paused,
                  TCSObjectReference<Point> currentPosition,
                  TCSObjectReference<Point> nextPosition,
                  Triple precisePosition,
                  double orientationAngle,
                  int energyLevel,
                  List<LoadHandlingDevice> loadHandlingDevices,
                  String envelopeKey,
                  Layout layout) {
    super(name, properties, history);
    this.length = checkInRange(length, 1, Integer.MAX_VALUE, "length");
    this.energyLevelGood = checkInRange(energyLevelGood, 0, 100, "energyLevelGood");
    this.energyLevelCritical = checkInRange(energyLevelCritical, 0, 100, "energyLevelCritical");
    this.energyLevelFullyRecharged = checkInRange(energyLevelFullyRecharged,
                                                  0,
                                                  100,
                                                  "energyLevelFullyRecharged");
    this.energyLevelSufficientlyRecharged = checkInRange(energyLevelSufficientlyRecharged,
                                                         0,
                                                         100,
                                                         "energyLevelSufficientlyRecharged");
    this.maxVelocity = checkInRange(maxVelocity, 0, Integer.MAX_VALUE, "maxVelocity");
    this.maxReverseVelocity = checkInRange(maxReverseVelocity,
                                           0,
                                           Integer.MAX_VALUE,
                                           "maxReverseVelocity");
    this.rechargeOperation = requireNonNull(rechargeOperation, "rechargeOperation");
    this.procState = requireNonNull(procState, "procState");
    this.transportOrder = transportOrder;
    this.orderSequence = orderSequence;
    this.allowedOrderTypes = requireNonNull(allowedOrderTypes, "allowedOrderTypes");
    this.routeProgressIndex = routeProgressIndex;
    this.claimedResources = requireNonNull(claimedResources, "claimedResources");
    this.allocatedResources = requireNonNull(allocatedResources, "allocatedResources");
    this.state = requireNonNull(state, "state");
    this.integrationLevel = requireNonNull(integrationLevel, "integrationLevel");
    this.paused = paused;
    this.currentPosition = currentPosition;
    this.nextPosition = nextPosition;
    this.precisePosition = precisePosition;
    checkArgument(
        Double.isNaN(orientationAngle) || (orientationAngle >= -360.0 && orientationAngle <= 360.0),
        "Illegal orientation angle: %s",
        orientationAngle
    );
    this.orientationAngle = orientationAngle;
    this.energyLevel = checkInRange(energyLevel, 0, 100, "energyLevel");
    this.loadHandlingDevices = listWithoutNullValues(requireNonNull(loadHandlingDevices,
                                                                    "loadHandlingDevices"));
    this.envelopeKey = envelopeKey;
    this.layout = requireNonNull(layout, "layout");
  }

  @Override
  public Vehicle withProperty(String key, String value) {
    return new Vehicle(getName(),
                       propertiesWith(key, value),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  @Override
  public Vehicle withProperties(Map<String, String> properties) {
    return new Vehicle(getName(),
                       properties,
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  @Override
  public TCSObject<Vehicle> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory().withEntryAppended(entry),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  @Override
  public TCSObject<Vehicle> withHistory(ObjectHistory history) {
    return new Vehicle(getName(),
                       getProperties(),
                       history,
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Checks whether the vehicle's energy level is critical.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * critical.
   */
  public boolean isEnergyLevelCritical() {
    return energyLevel <= energyLevelCritical;
  }

  /**
   * Checks whether the vehicle's energy level is degraded (not <em>good</em>
   * any more).
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * degraded.
   */
  public boolean isEnergyLevelDegraded() {
    return energyLevel <= energyLevelGood;
  }

  /**
   * Checks whether the vehicle's energy level is good.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * good.
   */
  public boolean isEnergyLevelGood() {
    return energyLevel > energyLevelGood;
  }

  /**
   * Checks whether the vehicle's energy level is fully recharged.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * fully recharged.
   */
  public boolean isEnergyLevelFullyRecharged() {
    return energyLevel >= energyLevelFullyRecharged;
  }

  /**
   * Checks whether the vehicle's energy level is sufficiently recharged.
   *
   * @return <code>true</code> if, and only if, the vehicle's energy level is
   * sufficiently recharged.
   */
  public boolean isEnergyLevelSufficientlyRecharged() {
    return energyLevel >= energyLevelSufficientlyRecharged;
  }

  /**
   * Returns this vehicle's critical energy level (in percent of the maximum).
   * The critical energy level is the one at/below which the vehicle should be
   * recharged.
   *
   * @return This vehicle's critical energy level.
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
  public Vehicle withEnergyLevelCritical(int energyLevelCritical) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns this vehicle's good energy level (in percent of the maximum).
   * The good energy level is the one at/above which the vehicle can be
   * dispatched again when charging.
   *
   * @return This vehicle's good energy level.
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
  public Vehicle withEnergyLevelGood(int energyLevelGood) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns this vehicle's energy level for being fully recharged (in percent of the maximum).
   *
   * @return This vehicle's fully recharged treshold.
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
  public Vehicle withEnergyLevelFullyRecharged(int energyLevelFullyRecharged) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns this vehicle's energy level for being sufficiently recharged (in percent of the
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
  public Vehicle withEnergyLevelSufficientlyRecharged(int energyLevelSufficientlyRecharged) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns this vehicle's current state.
   *
   * @return This vehicle's current state.
   */
  public State getState() {
    return state;
  }

  /**
   * Checks if this vehicle's current state is equal to the given one.
   *
   * @param otherState The state to compare to this vehicle's one.
   * @return <code>true</code> if, and only if, the given state is equal to this
   * vehicle's one.
   */
  public boolean hasState(State otherState) {
    return state.equals(otherState);
  }

  /**
   * Creates a copy of this object, with the given state.
   *
   * @param state The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withState(State state) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns this vehicle's current processing state.
   *
   * @return This vehicle's current processing state.
   */
  public ProcState getProcState() {
    return procState;
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return procState.equals(otherState);
  }

  /**
   * Creates a copy of this object, with the given processing state.
   *
   * @param procState The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withProcState(ProcState procState) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns this vehicle's current length.
   *
   * @return this vehicle's current length.
   */
  public int getLength() {
    return length;
  }

  /**
   * Creates a copy of this object, with the given length.
   *
   * @param length The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withLength(int length) {
    checkInRange(length, 1, Integer.MAX_VALUE, "length");
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns the set of order types this vehicle is allowed to process.
   *
   * @return The set of order types this vehicle is allowed to process.
   */
  public Set<String> getAllowedOrderTypes() {
    return allowedOrderTypes;
  }

  /**
   * Creates a copy of this object, with the given set of allowed order types.
   *
   * @param allowedOrderTypes The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withAllowedOrderTypes(Set<String> allowedOrderTypes) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns the index of the last route step travelled for the current drive
   * order of the current transport order.
   *
   * @return The index of the last route step travelled for the current drive
   * order of the current transport order.
   * @deprecated Use {@link TransportOrder#getCurrentRouteStepIndex()} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public int getRouteProgressIndex() {
    return routeProgressIndex;
  }

  /**
   * Creates a copy of this object, with the given route progress index.
   *
   * @param routeProgressIndex The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   * @deprecated Use {@link TransportOrder#withCurrentRouteStepIndex(int)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public Vehicle withRouteProgressIndex(int routeProgressIndex) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns a reference to the point this vehicle is expected to be seen at
   * next.
   *
   * @return A reference to the point this vehicle is expected to be seen at
   * next, or <code>null</code>, if this vehicle's next position is unknown.
   */
  public TCSObjectReference<Point> getNextPosition() {
    return nextPosition;
  }

  /**
   * Creates a copy of this object, with the given next position.
   *
   * @param nextPosition The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withNextPosition(TCSObjectReference<Point> nextPosition) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns the vehicle's position in world coordinates [mm], independent
   * from logical positions/point names. May be <code>null</code> if the vehicle
   * hasn't provided a precise position.
   *
   * @return The vehicle's precise position in mm.
   */
  public Triple getPrecisePosition() {
    return precisePosition;
  }

  /**
   * Creates a copy of this object, with the given precise position.
   *
   * @param precisePosition The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withPrecisePosition(Triple precisePosition) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns the vehicle's current orientation angle (-360..360).
   * May be <code>Double.NaN</code> if the vehicle hasn't provided an
   * orientation angle.
   *
   * @return The vehicle's current orientation angle.
   */
  public double getOrientationAngle() {
    return orientationAngle;
  }

  /**
   * Creates a copy of this object, with the given orientation angle.
   *
   * @param orientationAngle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withOrientationAngle(double orientationAngle) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns the key for selecting the envelope to be used for resources the vehicle occupies.
   *
   * @return The key for selecting the envelope to be used for resources the vehicle occupies.
   */
  @Nullable
  public String getEnvelopeKey() {
    return envelopeKey;
  }

  /**
   * Creates a copy of this object, with the given envelope key.
   *
   * @param envelopeKey The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Vehicle withEnvelopeKey(@Nullable String envelopeKey) {
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
  }

  /**
   * Returns the information regarding the grahical representation of this vehicle.
   *
   * @return The information regarding the grahical representation of this vehicle.
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
    return new Vehicle(getName(),
                       getProperties(),
                       getHistory(),
                       length,
                       energyLevelGood,
                       energyLevelCritical,
                       energyLevelFullyRecharged,
                       energyLevelSufficientlyRecharged,
                       maxVelocity,
                       maxReverseVelocity,
                       rechargeOperation,
                       procState,
                       transportOrder,
                       orderSequence,
                       allowedOrderTypes,
                       routeProgressIndex,
                       claimedResources,
                       allocatedResources,
                       state,
                       integrationLevel,
                       paused,
                       currentPosition,
                       nextPosition,
                       precisePosition,
                       orientationAngle,
                       energyLevel,
                       loadHandlingDevices,
                       envelopeKey,
                       layout);
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
        + ", procState=" + procState
        + ", integrationLevel=" + integrationLevel
        + ", paused=" + paused
        + ", state=" + state
        + ", energyLevel=" + energyLevel
        + ", currentPosition=" + currentPosition
        + ", precisePosition=" + precisePosition
        + ", orientationAngle=" + orientationAngle
        + ", nextPosition=" + nextPosition
        + ", loadHandlingDevices=" + loadHandlingDevices
        + ", length=" + length
        + ", transportOrder=" + transportOrder
        + ", routeProgressIndex=" + routeProgressIndex
        + ", claimedResources=" + claimedResources
        + ", allocatedResources=" + allocatedResources
        + ", orderSequence=" + orderSequence
        + ", energyLevelGood=" + energyLevelGood
        + ", energyLevelCritical=" + energyLevelCritical
        + ", energyLevelFullyRecharged=" + energyLevelFullyRecharged
        + ", energyLevelSufficientlyRecharged=" + energyLevelSufficientlyRecharged
        + ", maxVelocity=" + maxVelocity
        + ", maxReverseVelocity=" + maxReverseVelocity
        + ", rechargeOperation=" + rechargeOperation
        + ", allowedOrderTypes=" + allowedOrderTypes
        + ", envelopeKey=" + envelopeKey
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
   * Contains information regarding the grahical representation of a vehicle.
   */
  public static class Layout
      implements Serializable {

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
}
