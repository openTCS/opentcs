/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.order;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
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
public class OrderSequenceCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The category of the order sequence.
   */
  private String category = OrderConstants.CATEGORY_NONE;
  /**
   * The (optional) name of the vehicle that is supposed to execute the transport order.
   */
  @Nullable
  private String intendedVehicleName;
  /**
   * Whether failure of one transport order in the sequence makes subsequent ones fail, too.
   */
  private boolean failureFatal;

  /**
   * Creates a new instance.
   *
   * @param name The name of this transport order.
   */
  public OrderSequenceCreationTO(@Nonnull String name) {
    super(name);
  }

  public OrderSequenceCreationTO(@Nonnull String name,
                                 @Nonnull Map<String, String> properties,
                                 @Nonnull String category,
                                 @Nullable String intendedVehicleName,
                                 boolean failureFatal) {
    super(name, properties);
    this.category = requireNonNull(category, "category");
    this.intendedVehicleName = intendedVehicleName;
    this.failureFatal = failureFatal;
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public OrderSequenceCreationTO setName(@Nonnull String name) {
    return (OrderSequenceCreationTO) super.setName(name);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name the new name of the instance.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public OrderSequenceCreationTO withName(@Nonnull String name) {
    return new OrderSequenceCreationTO(name,
                                       getModifiableProperties(),
                                       category,
                                       intendedVehicleName,
                                       failureFatal);
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public OrderSequenceCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (OrderSequenceCreationTO) super.setProperties(properties);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public OrderSequenceCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new OrderSequenceCreationTO(getName(),
                                       properties,
                                       category,
                                       intendedVehicleName,
                                       failureFatal);
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public OrderSequenceCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (OrderSequenceCreationTO) super.setProperty(key, value);
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
  public OrderSequenceCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new OrderSequenceCreationTO(getName(),
                                       propertiesWith(key, value),
                                       category,
                                       intendedVehicleName,
                                       failureFatal);
  }

  /**
   * Returns the (optional) category of the order sequence.
   *
   * @return The (optional) category of the order sequence.
   */
  @Nonnull
  public String getCategory() {
    return category;
  }

  /**
   * Sets the (optional) category of the order sequence.
   *
   * @param category The category.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public OrderSequenceCreationTO setCategory(@Nonnull String category) {
    this.category = requireNonNull(category, "category");
    return this;
  }

  /**
   * Creates a copy of this object with the given category.
   *
   * @param category The category.
   * @return A copy of this object, differing in the given category.
   */
  public OrderSequenceCreationTO withCategory(@Nonnull String category) {
    return new OrderSequenceCreationTO(getName(),
                                       getModifiableProperties(),
                                       category,
                                       intendedVehicleName,
                                       failureFatal);
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
  public OrderSequenceCreationTO setIntendedVehicleName(@Nullable String intendedVehicleName) {
    this.intendedVehicleName = intendedVehicleName;
    return this;
  }

  /**
   * Creates a copy of this object with the given
   * (optional) name of the vehicle that is supposed to execute the transport order.
   *
   * @param intendedVehicleName The vehicle name.
   * @return A copy of this object, differing in the given name of the intended vehicle.
   */
  public OrderSequenceCreationTO withIntendedVehicleName(@Nullable String intendedVehicleName) {
    return new OrderSequenceCreationTO(getName(),
                                       getModifiableProperties(),
                                       category,
                                       intendedVehicleName,
                                       failureFatal);
  }

  /**
   * Returns whether failure of one transport order in the sequence makes subsequent ones fail, too.
   *
   * @return Whether failure of one transport order in the sequence makes subsequent ones fail, too.
   */
  public boolean isFailureFatal() {
    return failureFatal;
  }

  /**
   * Sets whether failure of one transport order in the sequence makes subsequent ones fail, too.
   *
   * @param failureFatal The failure-fatal flag.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public OrderSequenceCreationTO setFailureFatal(boolean failureFatal) {
    this.failureFatal = failureFatal;
    return this;
  }

  public OrderSequenceCreationTO withFailureFatal(boolean failureFatal) {
    return new OrderSequenceCreationTO(getName(),
                                       getModifiableProperties(),
                                       category,
                                       intendedVehicleName,
                                       failureFatal);
  }
}
