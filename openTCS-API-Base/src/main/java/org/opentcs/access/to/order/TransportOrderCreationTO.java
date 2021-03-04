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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.to.*;
import org.opentcs.data.order.OrderConstants;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TransportOrderCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The destinations that need to be travelled to.
   */
  @Nonnull
  private List<DestinationCreationTO> destinations;
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
   * The category of the transport order.
   */
  @Nonnull
  private String category = OrderConstants.CATEGORY_NONE;
  /**
   * The point of time at which execution of the transport order is supposed to be finished.
   */
  @Nonnull
  private Instant deadline = Instant.ofEpochMilli(Long.MAX_VALUE);
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
    this.destinations = requireNonNull(destinations, "destinations");
  }

  private TransportOrderCreationTO(@Nonnull String name,
                                   @Nonnull Map<String, String> properties,
                                   @Nonnull List<DestinationCreationTO> destinations,
                                   @Nullable String wrappingSequence,
                                   @Nonnull Set<String> dependencyNames,
                                   @Nullable String intendedVehicleName,
                                   @Nonnull String category,
                                   @Nonnull Instant deadline,
                                   boolean dispensable) {
    super(name, properties);
    this.destinations = requireNonNull(destinations, "destinations");
    this.wrappingSequence = wrappingSequence;
    this.dependencyNames = requireNonNull(dependencyNames, "dependencyNames");
    this.intendedVehicleName = intendedVehicleName;
    this.category = requireNonNull(category, "category");
    this.deadline = requireNonNull(deadline, "deadline");
    this.dispensable = dispensable;
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public TransportOrderCreationTO setName(@Nonnull String name) {
    return (TransportOrderCreationTO) super.setName(name);
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
                                        deadline,
                                        dispensable);
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public TransportOrderCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (TransportOrderCreationTO) super.setProperties(properties);
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
                                        deadline,
                                        dispensable);
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public TransportOrderCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (TransportOrderCreationTO) super.setProperty(key, value);
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
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
   * Sets the destinations that need to be travelled to.
   *
   * @param destinations The destinations.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public TransportOrderCreationTO setDestinations(@Nonnull List<DestinationCreationTO> destinations) {
    this.destinations = requireNonNull(destinations, "destinations");
    return this;
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
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
   * Sets the (optional) name of the order sequence the transport order belongs to.
   *
   * @param wrappingSequence The name of the sequence.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public TransportOrderCreationTO setWrappingSequence(@Nullable String wrappingSequence) {
    this.wrappingSequence = wrappingSequence;
    return this;
  }

  /**
   *
   * Creates a copy of this object with the given
   * (optional) name of the order sequence the transport order belongs to. .
   *
   * @param wrappingSequence The name of the sequence.
   * @return A copy of this object, differing in the given name of the sequence.
   */
  public TransportOrderCreationTO withWrappingSequence(@Nullable String wrappingSequence) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
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
   * Sets the (optional) names of transport orders the transport order depends on.
   *
   * @param dependencyNames The dependency names.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public TransportOrderCreationTO setDependencyNames(@Nonnull Set<String> dependencyNames) {
    this.dependencyNames = requireNonNull(dependencyNames, "dependencyNames");
    return this;
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
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
   * Sets the (optional) name of the vehicle that is supposed to execute the transport order.
   *
   * @param intendedVehicleName The vehicle name.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public TransportOrderCreationTO setIntendedVehicleName(@Nullable String intendedVehicleName) {
    this.intendedVehicleName = intendedVehicleName;
    return this;
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the (optional) category of the transport order.
   *
   * @return The (optional) category of the transport order.
   */
  @Nonnull
  public String getCategory() {
    return category;
  }

  /**
   * Sets the (optional) category of the transport order.
   *
   * @param category The category.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public TransportOrderCreationTO setCategory(@Nonnull String category) {
    this.category = requireNonNull(category, "category");
    return this;
  }

  /**
   * Creates a copy of this object with the given (optional) category of the transport order.
   *
   * @param category The category.
   * @return A copy of this object, differing in the given category.
   */
  public TransportOrderCreationTO withCategory(@Nonnull String category) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
                                        deadline,
                                        dispensable);
  }

  /**
   * Returns the point of time at which execution of the transport order is supposed to be finished.
   *
   * @return The point of time at which execution of the transport order is supposed to be finished.
   */
  @Nonnull
  @ScheduledApiChange(when = "5.0", details = "Will return an Instant instead.")
  public ZonedDateTime getDeadline() {
    return ZonedDateTime.ofInstant(deadline, ZoneId.systemDefault());
  }

  /**
   * Sets the point of time at which execution of the transport order is supposed to be finished.
   *
   * @param deadline The deadline.
   * @return This instance.
   * @deprecated Use {@link #withDeadline(java.time.Instant)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public TransportOrderCreationTO setDeadline(@Nonnull ZonedDateTime deadline) {
    this.deadline = requireNonNull(deadline, "deadline").toInstant();
    return this;
  }

  /**
   * Creates a copy of this object with the given
   * point of time at which execution of the transport order is supposed to be finished.
   *
   * @param deadline The deadline.
   * @return A copy of this object, differing in the given deadline.
   * @deprecated Use {@link #withDeadline(java.time.Instant)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public TransportOrderCreationTO withDeadline(@Nonnull ZonedDateTime deadline) {
    return new TransportOrderCreationTO(getName(),
                                        getModifiableProperties(),
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
                                        deadline.toInstant(),
                                        dispensable);
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
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
   * Sets whether the transport order is dispensable or not.
   *
   * @param dispensable The dispensable flag.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public TransportOrderCreationTO setDispensable(boolean dispensable) {
    this.dispensable = dispensable;
    return this;
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
                                        destinations,
                                        wrappingSequence,
                                        dependencyNames,
                                        intendedVehicleName,
                                        category,
                                        deadline,
                                        dispensable);
  }
}
