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

  @Nonnull
  @Override
  public OrderSequenceCreationTO setName(@Nonnull String name) {
    return (OrderSequenceCreationTO) super.setName(name);
  }

  @Nonnull
  @Override
  public OrderSequenceCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (OrderSequenceCreationTO) super.setProperties(properties);
  }

  @Nonnull
  @Override
  public OrderSequenceCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (OrderSequenceCreationTO) super.setProperty(key, value);
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
   * @return The category.
   */
  public OrderSequenceCreationTO setCategory(@Nonnull String category) {
    this.category = requireNonNull(category, "category");
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
  public OrderSequenceCreationTO setIntendedVehicleName(@Nullable String intendedVehicleName) {
    this.intendedVehicleName = intendedVehicleName;
    return this;
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
  @Nonnull
  public OrderSequenceCreationTO setFailureFatal(boolean failureFatal) {
    this.failureFatal = failureFatal;
    return this;
  }
}
