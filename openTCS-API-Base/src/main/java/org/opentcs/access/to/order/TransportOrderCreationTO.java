/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.order;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.to.*;

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
   * The point of time at which execution of the transport order is supposed to be finished.
   */
  private ZonedDateTime deadline
      = ZonedDateTime.of(2099, 12, 31, 23, 59, 59, 0, ZoneId.systemDefault());
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

  @Nonnull
  @Override
  public TransportOrderCreationTO setName(@Nonnull String name) {
    return (TransportOrderCreationTO) super.setName(name);
  }

  @Nonnull
  @Override
  public TransportOrderCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (TransportOrderCreationTO) super.setProperties(properties);
  }

  @Nonnull
  @Override
  public TransportOrderCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (TransportOrderCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the destinations that need to be travelled to.
   *
   * @return The destinations that need to be travelled to.
   */
  @Nonnull
  public List<DestinationCreationTO> getDestinations() {
    return destinations;
  }

  /**
   * Sets the destinations that need to be travelled to.
   *
   * @param destinations The destinations.
   * @return This instance.
   */
  @Nonnull
  public TransportOrderCreationTO setDestinations(@Nonnull List<DestinationCreationTO> destinations) {
    this.destinations = requireNonNull(destinations, "destinations");
    return this;
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
  @Nonnull
  public TransportOrderCreationTO setWrappingSequence(@Nullable String wrappingSequence) {
    this.wrappingSequence = wrappingSequence;
    return this;
  }

  /**
   * Returns the (optional) names of transport orders the transport order depends on.
   *
   * @return The (optional) names of transport orders the transport order depends on.
   */
  @Nonnull
  public Set<String> getDependencyNames() {
    return dependencyNames;
  }

  /**
   * Sets the (optional) names of transport orders the transport order depends on.
   *
   * @param dependencyNames The dependency names.
   * @return This instance.
   */
  @Nonnull
  public TransportOrderCreationTO setDependencyNames(@Nonnull Set<String> dependencyNames) {
    this.dependencyNames = requireNonNull(dependencyNames, "dependencyNames");
    return this;
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
  @Nonnull
  public TransportOrderCreationTO setIntendedVehicleName(@Nullable String intendedVehicleName) {
    this.intendedVehicleName = intendedVehicleName;
    return this;
  }

  /**
   * Returns the point of time at which execution of the transport order is supposed to be finished.
   *
   * @return The point of time at which execution of the transport order is supposed to be finished.
   */
  @Nonnull
  public ZonedDateTime getDeadline() {
    return deadline;
  }

  /**
   * Sets the point of time at which execution of the transport order is supposed to be finished.
   *
   * @param deadline The deadline.
   * @return This instance.
   */
  @Nonnull
  public TransportOrderCreationTO setDeadline(@Nonnull ZonedDateTime deadline) {
    this.deadline = requireNonNull(deadline, "deadline");
    return this;
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
  @Nonnull
  public TransportOrderCreationTO setDispensable(boolean dispensable) {
    this.dispensable = dispensable;
    return this;
  }
}
