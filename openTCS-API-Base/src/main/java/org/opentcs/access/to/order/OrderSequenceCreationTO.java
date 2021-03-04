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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.to.*;

/**
 * A transfer object describing a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OrderSequenceCreationTO
    extends CreationTO
    implements Serializable {

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
