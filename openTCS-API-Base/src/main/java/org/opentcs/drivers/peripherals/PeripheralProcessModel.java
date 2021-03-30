/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;

/**
 * A model of a peripheral device's and its communication adapter's attributes.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralProcessModel
    implements Serializable {

  /**
   * The reference to the location that is attached to this model.
   */
  private final TCSResourceReference<Location> location;
  /**
   * Whether the communication adapter is currently enabled.
   */
  private final boolean commAdapterEnabled;
  /**
   * Whether the communication adapter is currently connected to the peripheral device.
   */
  private final boolean commAdapterConnected;
  /**
   * The peripheral device's current state.
   */
  private final PeripheralInformation.State state;

  /**
   * Creates a new instance.
   *
   * @param location The reference to the location that is attached to this model.
   */
  public PeripheralProcessModel(TCSResourceReference<Location> location) {
    this(location, false, false, PeripheralInformation.State.NO_PERIPHERAL);
  }

  protected PeripheralProcessModel(@Nonnull TCSResourceReference<Location> location,
                                   boolean commAdapterEnabled,
                                   boolean commAdapterConnected,
                                   @Nonnull PeripheralInformation.State state) {
    this.location = requireNonNull(location, "location");
    this.commAdapterEnabled = commAdapterEnabled;
    this.commAdapterConnected = commAdapterConnected;
    this.state = requireNonNull(state, "state");
  }

  /**
   * Returns the reference to the location that is attached to this model.
   *
   * @return The reference to the location that is attached to this model.
   */
  @Nonnull
  public TCSResourceReference<Location> getLocation() {
    return location;
  }

  /**
   * Creates a copy of the object, with the given location reference.
   *
   * @param location The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralProcessModel withLocation(
      @Nonnull TCSResourceReference<Location> location) {
    return new PeripheralProcessModel(location, commAdapterEnabled, commAdapterConnected, state);
  }

  /**
   * Returns whether the communication adapter is currently enabled.
   *
   * @return Whether the communication adapter is currently enabled.
   */
  public boolean isCommAdapterEnabled() {
    return commAdapterEnabled;
  }

  /**
   * Creates a copy of the object, with the given enabled state.
   *
   * @param commAdapterEnabled The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralProcessModel withCommAdapterEnabled(boolean commAdapterEnabled) {
    return new PeripheralProcessModel(location, commAdapterEnabled, commAdapterConnected, state);
  }

  /**
   * Returns whether the communication adapter is currently connected to the peripheral device.
   *
   * @return Whether the communication adapter is currently connected to the peripheral device.
   */
  public boolean isCommAdapterConnected() {
    return commAdapterConnected;
  }

  /**
   * Creates a copy of the object, with the given connected state.
   *
   * @param commAdapterConnected The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralProcessModel withCommAdapterConnected(boolean commAdapterConnected) {
    return new PeripheralProcessModel(location, commAdapterEnabled, commAdapterConnected, state);
  }

  /**
   * Returns the peripheral device's current state.
   *
   * @return The peripheral device's current state.
   */
  public PeripheralInformation.State getState() {
    return state;
  }

  /**
   * Creates a copy of the object, with the given state.
   *
   * @param state The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PeripheralProcessModel withState(PeripheralInformation.State state) {
    return new PeripheralProcessModel(location, commAdapterEnabled, commAdapterConnected, state);
  }

  /**
   * Used to describe what has changed in a process model.
   */
  public enum Attribute {
    /**
     * Indicates a change of the location property.
     */
    LOCATION,
    /**
     * Indicates a change of the comm adapter enabled property.
     */
    COMM_ADAPTER_ENABLED,
    /**
     * Indicates a change of the comm adapter connected property.
     */
    COMM_ADAPTER_CONNECTED,
    /**
     * Indicates a change of the state property.
     */
    STATE;
  }
}
