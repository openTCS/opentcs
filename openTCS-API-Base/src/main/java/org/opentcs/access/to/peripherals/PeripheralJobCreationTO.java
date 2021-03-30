/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.peripherals;

import java.io.Serializable;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a peripheral job.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralJobCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * Indicates whether the name is incomplete and requires to be completed when creating the actual
   * peripheral job.
   * (How exactly this is done is decided by the kernel.)
   */
  private final boolean incompleteName;
  /**
   * A token that may be used to reserve a peripheral device.
   * A peripheral device that is reserved for a specific token can only process jobs which match
   * that reservation token.
   * This string may not be empty.
   */
  @Nonnull
  private final String reservationToken;
  /**
   * The name of the vehicle for which this peripheral job is to be created.
   */
  @Nullable
  private final String relatedVehicleName;
  /**
   * The name of the transport order for which this peripheral job is to be created.
   */
  @Nullable
  private final String relatedTransportOrderName;
  /**
   * The operation that is to be perfromed by the pripheral device.
   */
  @Nonnull
  private final PeripheralOperationCreationTO peripheralOperation;

  /**
   * Creates a new instance.
   *
   * @param name The name of this peripheral job.
   * @param reservationToken The reservation token to be used.
   * @param peripheralOperation The peripheral operation to be performed.
   */
  public PeripheralJobCreationTO(@Nonnull String name,
                                 @Nonnull String reservationToken,
                                 @Nonnull PeripheralOperationCreationTO peripheralOperation) {
    super(name);
    this.incompleteName = false;
    this.reservationToken = requireNonNull(reservationToken, "reservationToken");
    this.relatedVehicleName = null;
    this.relatedTransportOrderName = null;
    this.peripheralOperation = requireNonNull(peripheralOperation, "peripheralOperation");
  }

  private PeripheralJobCreationTO(@Nonnull String name,
                                  @Nonnull Map<String, String> properties,
                                  boolean incompleteName,
                                  @Nonnull String reservationToken,
                                  @Nullable String relatedVehicleName,
                                  @Nullable String relatedTransportOrderName,
                                  @Nonnull PeripheralOperationCreationTO peripheralOperation) {
    super(name, properties);
    this.incompleteName = incompleteName;
    this.reservationToken = requireNonNull(reservationToken, "reservationToken");
    this.relatedVehicleName = relatedVehicleName;
    this.relatedTransportOrderName = relatedTransportOrderName;
    this.peripheralOperation = requireNonNull(peripheralOperation, "peripheralOperation");
  }

  @Override
  public PeripheralJobCreationTO withName(@Nonnull String name) {
    return new PeripheralJobCreationTO(name,
                                       getModifiableProperties(),
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }

  @Override
  public PeripheralJobCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new PeripheralJobCreationTO(getName(),
                                       properties,
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }

  @Override
  public PeripheralJobCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new PeripheralJobCreationTO(getName(),
                                       propertiesWith(key, value),
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }

  /**
   * Indicates whether the name is incomplete and requires to be completed when creating the actual
   * transport order.
   * (How exactly this is done is decided by the kernel.)
   *
   * @return {@code true} if, and only if, the name is incomplete and requires to be completed
   * by the kernel.
   */
  public boolean hasIncompleteName() {
    return incompleteName;
  }

  /**
   * Creates a copy of this object, with the given incomplete name flag.
   *
   * @param incompleteName The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJobCreationTO withIncompleteName(boolean incompleteName) {
    return new PeripheralJobCreationTO(getName(),
                                       getModifiableProperties(),
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }

  /**
   * Returns the token that may be used to reserve a peripheral device.
   *
   * @return The token that may be used to reserve a peripheral device.
   */
  public String getReservationToken() {
    return reservationToken;
  }

  /**
   * Creates a copy of this object, with the given reservation token.
   *
   * @param reservationToken The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJobCreationTO withReservationToken(String reservationToken) {
    return new PeripheralJobCreationTO(getName(),
                                       getModifiableProperties(),
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }

  /**
   * Returns the name of the vehicle for which this peripheral job is to be created.
   *
   * @return The name of the vehicle for which this peripheral job is to be created.
   */
  @Nullable
  public String getRelatedVehicleName() {
    return relatedVehicleName;
  }

  /**
   * Creates a copy of this object, with the given related vehicle name.
   *
   * @param relatedVehicleName The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJobCreationTO withRelatedVehicleName(@Nullable String relatedVehicleName) {
    return new PeripheralJobCreationTO(getName(),
                                       getModifiableProperties(),
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }

  /**
   * Returns the name of the transport order for which this peripheral job is to be created.
   *
   * @return The name of the transport order for which this peripheral job is to be created.
   */
  @Nullable
  public String getRelatedTransportOrderName() {
    return relatedTransportOrderName;
  }

  /**
   * Creates a copy of this object, with the given related transport order name.
   *
   * @param relatedTransportOrderName The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJobCreationTO withRelatedTransportOrderName(
      @Nullable String relatedTransportOrderName) {
    return new PeripheralJobCreationTO(getName(),
                                       getModifiableProperties(),
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }

  /**
   * Returns the operation that is to be performed by the pripheral device.
   *
   * @return The operation that is to be performed by the pripheral device.
   */
  public PeripheralOperationCreationTO getPeripheralOperation() {
    return peripheralOperation;
  }

  /**
   * Creates a copy of this object, with the given peripheral operation.
   *
   * @param peripheralOperation The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralJobCreationTO withPeripheralOperation(
      PeripheralOperationCreationTO peripheralOperation) {
    return new PeripheralJobCreationTO(getName(),
                                       getModifiableProperties(),
                                       incompleteName,
                                       reservationToken,
                                       relatedVehicleName,
                                       relatedTransportOrderName,
                                       peripheralOperation);
  }
}
