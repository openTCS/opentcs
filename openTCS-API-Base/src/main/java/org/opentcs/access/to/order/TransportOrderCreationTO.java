/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.order;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.to.CreationTO;
import org.opentcs.data.order.OrderConstants;

/**
 * A transfer object describing a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * Indicates whether the name is incomplete and requires to be completed when creating the actual
   * transport order.
   * (How exactly this is done is decided by the kernel.)
   */
  private final boolean incompleteName;
  /**
   * The destinations that need to be travelled to.
   */
  @Nonnull
  private List<DestinationCreationTO> destinations;
  /**
   * An optional token for reserving peripheral devices while processing this transport order.
   */
  @Nullable
  private String peripheralReservationToken;
  /**
   * The (optional) name of the order sequence the transport order belongs to.
   */
  @Nullable
  private String wrappingSequence;
  /**
   * The (optional) names of transport orders the transport order depends on.
   */
  @Nonnull
  private Set<String> dependencyNames = new HashSet<>();
  /**
   * The (optional) name of the vehicle that is supposed to execute the transport order.
   */
  @Nullable
  private String intendedVehicleName;
  /**
   * The type of the transport order.
   */
  @Nonnull
  private String type = OrderConstants.TYPE_NONE;
  /**
   * The point of time at which execution of the transport order is supposed to be finished.
   */
  @Nonnull
  private Instant deadline = Instant.MAX;
  /**
   * Whether the transport order is dispensable or not.
   */
  private boolean dispensable;

  /**
   * Creates a new instance.
   *
   * @param name The name of this transport order.
   * @param destinations The destinations that need to be travelled to.
   */
  public TransportOrderCreationTO(@Nonnull String name,
                                  @Nonnull List<DestinationCreationTO> destinations) {
    super(name);
    this.incompleteName = false;
    this.destinations = requireNonNull(destinations, "destinations");
  }

  private TransportOrderCreationTO(@Nonnull String name,
                                   @Nonnull Map<String, String> properties,
                                   boolean incompleteName,
                                   @Nonnull List<DestinationCreationTO> destinations,
                                   @Nullable String peripheralReservationToken,
                                   @Nullable String wrappingSequence,
                                   @Nonnull Set<String> dependencyNames,
                                   @Nullable String intendedVehicleName,
                                   @Nonnull String type,
                                   @Nonnull Instant deadline,
                                   boolean dispensable) {
    super(name, properties);
    this.incompleteName = incompleteName;
    this.destinations = requireNonNull(destinations, "destinations");
    this.peripheralReservationToken = peripheralReservationToken;
    this.wrappingSequence = wrappingSequence;
    this.dependencyNames = requireNonNull(dependencyNames, "dependencyNames");
    this.intendedVehicleName = intendedVehicleName;
    this.type = requireNonNull(type, "type");
    this.deadline = requireNonNull(deadline, "deadline");
    this.dispensable = dispensable;
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name of the instance.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public TransportOrderCreationTO withName(@Nonnull String name) {
    return new TransportOrderCreationTO(name,
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public TransportOrderCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new TransportOrderCreationTO(getName(),
                                        properties,
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Creates a copy of this object and adds the given property.
   * If value == null, then the key-value pair is removed from the properties.
   *
   * @param key the key.
   * @param value the value
   * @return A copy of this object that either
   * includes the given entry in it's current properties, if value != null or
   * excludes the entry otherwise.
   */
  @Override
  public TransportOrderCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new TransportOrderCreationTO(getName(),
                                        propertiesWith(key, value),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Indicates whether the name is incomplete and requires to be completed when creating the actual
   * transport order.
   * (How exactly this is done is decided by the kernel.)
   *
   * @return <code>true</code> if, and only if, the name is incomplete and requires to be completed
   * by the kernel.
   */
  public boolean hasIncompleteName() {
    return incompleteName;
  }

  /**
   * Creates a copy of this object with the given <em>nameIncomplete</em> flag.
   *
   * @param incompleteName Whether the name is incomplete and requires to be completed when creating
   * the actual transport order.
   *
   * @return A copy of this object, differing in the given value.
   */
  public TransportOrderCreationTO withIncompleteName(boolean incompleteName) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the destinations that need to be travelled to.
   *
   * @return The destinations that need to be travelled to.
   */
  @Nonnull
  public List<DestinationCreationTO> getDestinations() {
    return Collections.unmodifiableList(destinations);
  }

  /**
   * Creates a copy of this object with the given destinations that need to be travelled to.
   *
   * @param destinations The destinations.
   * @return A copy of this object, differing in the given derstinations.
   */
  public TransportOrderCreationTO withDestinations(@Nonnull List<DestinationCreationTO> destinations) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns an optional token for reserving peripheral devices while processing this transport
   * order.
   *
   * @return An optional token for reserving peripheral devices while processing this transport
   * order.
   */
  @Nullable
  public String getPeripheralReservationToken() {
    return peripheralReservationToken;
  }

  /**
   * Creates a copy of this object with the given (optional) peripheral reservation token.
   *
   * @param peripheralReservationToken The token.
   * @return A copy of this object, differing in the given peripheral reservation token.
   */
  public TransportOrderCreationTO withPeripheralReservationToken(
      @Nullable String peripheralReservationToken) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the (optional) name of the order sequence the transport order belongs to.
   *
   * @return The (optional) name of the order sequence the transport order belongs to.
   */
  @Nullable
  public String getWrappingSequence() {
    return wrappingSequence;
  }

  /**
   * Creates a copy of this object with the given
   * (optional) name of the order sequence the transport order belongs to.
   *
   * @param wrappingSequence The name of the sequence.
   * @return A copy of this object, differing in the given name of the sequence.
   */
  public TransportOrderCreationTO withWrappingSequence(@Nullable String wrappingSequence) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the (optional) names of transport orders the transport order depends on.
   *
   * @return The (optional) names of transport orders the transport order depends on.
   */
  @Nonnull
  public Set<String> getDependencyNames() {
    return Collections.unmodifiableSet(dependencyNames);
  }

  /**
   * Creates a copy of this object with the given
   * (optional) names of transport orders the transport order depends on.
   *
   * @param dependencyNames The dependency names.
   * @return A copy of this object, differing in the given dependency names.
   */
  public TransportOrderCreationTO withDependencyNames(@Nonnull Set<String> dependencyNames) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the (optional) name of the vehicle that is supposed to execute the transport order.
   *
   * @return The (optional) name of the vehicle that is supposed to execute the transport order.
   */
  @Nullable
  public String getIntendedVehicleName() {
    return intendedVehicleName;
  }

  /**
   * Creates a copy of this object with the given
   * (optional) name of the vehicle that is supposed to execute the transport order.
   *
   * @param intendedVehicleName The vehicle name.
   * @return A copy of this object, differing in the given vehicle's name.
   */
  public TransportOrderCreationTO withIntendedVehicleName(@Nullable String intendedVehicleName) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the (optional) type of the transport order.
   *
   * @return The (optional) type of the transport order.
   */
  @Nonnull
  public String getType() {
    return type;
  }

  /**
   * Creates a copy of this object with the given (optional) type of the transport order.
   *
   * @param type The type.
   * @return A copy of this object, differing in the given type.
   */
  public TransportOrderCreationTO withType(@Nonnull String type) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the point of time at which execution of the transport order is supposed to be finished.
   *
   * @return The point of time at which execution of the transport order is supposed to be finished.
   */
  @Nonnull
  public Instant getDeadline() {
    return deadline;
  }

  /**
   * Creates a copy of this object with the given
   * point of time at which execution of the transport order is supposed to be finished.
   *
   * @param deadline The deadline.
   * @return A copy of this object, differing in the given deadline.
   */
  public TransportOrderCreationTO withDeadline(@Nonnull Instant deadline) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns whether the transport order is dispensable or not.
   *
   * @return Whether the transport order is dispensable or not.
   */
  public boolean isDispensable() {
    return dispensable;
  }

  /**
   * Creates a copy of this object with the
   * given indication whether the transport order is dispensable or not.
   *
   * @param dispensable The dispensable flag.
   * @return A copy of this object, differing in the given dispensable flag.
   */
  public TransportOrderCreationTO withDispensable(boolean dispensable) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        incompleteName,
                                        destinations,
                                        peripheralReservationToken,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        type,
                                        deadline,
                                        dispensable);
  }
}
