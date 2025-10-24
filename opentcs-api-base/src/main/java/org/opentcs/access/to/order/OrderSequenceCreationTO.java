// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.order;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a transport order.
 */
public class OrderSequenceCreationTO
    extends
      CreationTO
    implements
      Serializable {

  /**
   * Indicates whether the name is incomplete and requires to be completed when creating the actual
   * order sequence.
   * (How exactly this is done is decided by the kernel.)
   */
  private final boolean incompleteName;
  /**
   * The type of the order sequence.
   */
  private final String type;
  /**
   * The (optional) name of the vehicle that is supposed to execute the transport order.
   */
  @Nullable
  private final String intendedVehicleName;
  /**
   * Whether failure of one transport order in the sequence makes subsequent ones fail, too.
   */
  private final boolean failureFatal;

  /**
   * Creates a new instance.
   *
   * @param name The name of this transport order.
   */
  public OrderSequenceCreationTO(
      @Nonnull
      String name
  ) {
    super(name);
    this.incompleteName = false;
    this.type = OrderConstantsTO.TYPE_NONE;
    this.intendedVehicleName = null;
    this.failureFatal = false;
  }

  private OrderSequenceCreationTO(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      boolean incompleteName,
      @Nonnull
      String type,
      @Nullable
      String intendedVehicleName,
      boolean failureFatal
  ) {
    super(name, properties);
    this.incompleteName = incompleteName;
    this.type = requireNonNull(type, "type");
    this.intendedVehicleName = intendedVehicleName;
    this.failureFatal = failureFatal;
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name the new name of the instance.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public OrderSequenceCreationTO withName(
      @Nonnull
      String name
  ) {
    return new OrderSequenceCreationTO(
        name,
        getModifiableProperties(),
        incompleteName,
        type,
        intendedVehicleName,
        failureFatal
    );
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given value.
   */
  @Override
  public OrderSequenceCreationTO withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new OrderSequenceCreationTO(
        getName(),
        properties,
        incompleteName,
        type,
        intendedVehicleName,
        failureFatal
    );
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
  public OrderSequenceCreationTO withProperty(
      @Nonnull
      String key,
      @Nonnull
      String value
  ) {
    return new OrderSequenceCreationTO(
        getName(),
        propertiesWith(key, value),
        incompleteName,
        type,
        intendedVehicleName,
        failureFatal
    );
  }

  /**
   * Indicates whether the name is incomplete and requires to be completed when creating the actual
   * order sequence.
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
   * the actual order sequence.
   *
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequenceCreationTO withIncompleteName(boolean incompleteName) {
    return new OrderSequenceCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        type,
        intendedVehicleName,
        failureFatal
    );
  }

  /**
   * Returns the (optional) type of the order sequence.
   *
   * @return The (optional) type of the order sequence.
   */
  @Nonnull
  public String getType() {
    return type;
  }

  /**
   * Creates a copy of this object with the given type.
   *
   * @param type The type.
   * @return A copy of this object, differing in the given type.
   */
  public OrderSequenceCreationTO withType(
      @Nonnull
      String type
  ) {
    return new OrderSequenceCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        type,
        intendedVehicleName,
        failureFatal
    );
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
   * @return A copy of this object, differing in the given name of the intended vehicle.
   */
  public OrderSequenceCreationTO withIntendedVehicleName(
      @Nullable
      String intendedVehicleName
  ) {
    return new OrderSequenceCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        type,
        intendedVehicleName,
        failureFatal
    );
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
   * Creates a copy of this object with the given <em>failureFatal</em> flag.
   *
   * @param failureFatal Whether failure of one transport order in the sequence makes subsequent
   * ones fail, too.
   *
   * @return A copy of this object, differing in the given value.
   */
  public OrderSequenceCreationTO withFailureFatal(boolean failureFatal) {
    return new OrderSequenceCreationTO(
        getName(),
        getModifiableProperties(),
        incompleteName,
        type,
        intendedVehicleName,
        failureFatal
    );
  }
}
