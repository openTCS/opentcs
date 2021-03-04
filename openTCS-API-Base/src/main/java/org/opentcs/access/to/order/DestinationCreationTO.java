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
import org.opentcs.access.to.CreationTO;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a destination of a drive order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DestinationCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * The name of the destination location (or point).
   */
  @Nonnull
  private String destLocationName;
  /**
   * The operation to be performed at the destination.
   */
  @Nonnull
  private String destOperation;

  /**
   * Creates a new instance.
   *
   * @param destLocationName The name of the destination location (or destination point).
   * @param destOperation The operation to be performed at the destination.
   */
  public DestinationCreationTO(@Nonnull String destLocationName,
                               @Nonnull String destOperation) {
    super("");
    this.destLocationName = requireNonNull(destLocationName, "destLocationName");
    this.destOperation = requireNonNull(destOperation, "destOperation");
  }

  private DestinationCreationTO(@Nonnull String destLocationName,
                                @Nonnull String destOperation,
                                @Nonnull String name,
                                @Nonnull Map<String, String> properties) {
    super(name, properties);
    this.destLocationName = requireNonNull(destLocationName, "destLocationName");
    this.destOperation = requireNonNull(destOperation, "destOperation");
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public DestinationCreationTO setName(@Nonnull String name) {
    return (DestinationCreationTO) super.setName(name);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name the new name of the instance.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public DestinationCreationTO withName(@Nonnull String name) {
    return new DestinationCreationTO(destLocationName, destOperation, name, getModifiableProperties());
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public DestinationCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (DestinationCreationTO) super.setProperties(properties);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public DestinationCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new DestinationCreationTO(destLocationName, destOperation, getName(), properties);
  }

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public DestinationCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (DestinationCreationTO) super.setProperty(key, value);
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
  public DestinationCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new DestinationCreationTO(destLocationName,
                                     destOperation,
                                     getName(),
                                     propertiesWith(key, value));
  }

  /**
   * Returns the destination location (or point) name.
   *
   * @return The destination location (or point) name.
   */
  @Nonnull
  public String getDestLocationName() {
    return destLocationName;
  }

  /**
   * Sets the destination location (or point) name.
   *
   * @param destLocationName The destination location (or point) name.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public DestinationCreationTO setDestLocationName(@Nonnull String destLocationName) {
    this.destLocationName = requireNonNull(destLocationName, "destLocationName");
    return this;
  }

  /**
   * Creates a copy of this object with the given destination location (or point) name.
   *
   * @param desLocationName The destination location (or point) name.
   * @return A copy of this object, differing in the given destination.
   */
  public DestinationCreationTO withDestLocationName(@Nonnull String desLocationName) {
    return new DestinationCreationTO(destLocationName, destOperation, getName(), getModifiableProperties());
  }

  /**
   * Returns the operation to be performed at the destination.
   *
   * @return The operation to be performed at the destination.
   */
  @Nonnull
  public String getDestOperation() {
    return destOperation;
  }

  /**
   * Sets the operation to be performed at the destination.
   *
   * @param destOperation The operation.
   * @return This instance.
   */
  @Deprecated
  @ScheduledApiChange(when="5.0")
  @Nonnull
  public DestinationCreationTO setDestOperation(@Nonnull String destOperation) {
    this.destOperation = requireNonNull(destOperation, "destOperation");
    return this;
  }
  
  /**
   * Creates a copy of this object with the given operation to be performed at the destination.
   * 
   * @param destOperation The operation.
   * @return A copy of this object, differing in the given destination operation.
   */
  public DestinationCreationTO withDestOperation(@Nonnull String destOperation) {
    return new DestinationCreationTO(destLocationName, destOperation, getName(), getModifiableProperties());
  }

}
