/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import java.io.Serializable;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;

/**
 * The process model for the loopback peripheral communication adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackPeripheralProcessModel
    extends PeripheralProcessModel
    implements Serializable {

  /**
   * Whether the peripheral device is manual mode (i.e. the user triggers the job execution).
   */
  private final boolean manualModeEnabled;

  public LoopbackPeripheralProcessModel(TCSResourceReference<Location> location) {
    this(location, false, false, PeripheralInformation.State.UNKNOWN, false);
  }

  private LoopbackPeripheralProcessModel(TCSResourceReference<Location> location,
                                         boolean commAdapterEnabled,
                                         boolean commAdapterConnected,
                                         PeripheralInformation.State state,
                                         boolean manualModeEnabled) {
    super(location, commAdapterEnabled, commAdapterConnected, state);
    this.manualModeEnabled = manualModeEnabled;
  }

  @Override
  public LoopbackPeripheralProcessModel withLocation(TCSResourceReference<Location> location) {
    return new LoopbackPeripheralProcessModel(location,
                                              isCommAdapterEnabled(),
                                              isCommAdapterConnected(),
                                              getState(),
                                              manualModeEnabled);
  }

  @Override
  public LoopbackPeripheralProcessModel withCommAdapterEnabled(boolean commAdapterEnabled) {
    return new LoopbackPeripheralProcessModel(getLocation(),
                                              commAdapterEnabled,
                                              isCommAdapterConnected(),
                                              getState(),
                                              manualModeEnabled);
  }

  @Override
  public LoopbackPeripheralProcessModel withCommAdapterConnected(boolean commAdapterConnected) {
    return new LoopbackPeripheralProcessModel(getLocation(),
                                              isCommAdapterEnabled(),
                                              commAdapterConnected,
                                              getState(),
                                              manualModeEnabled);
  }

  @Override
  public LoopbackPeripheralProcessModel withState(PeripheralInformation.State state) {
    return new LoopbackPeripheralProcessModel(getLocation(),
                                              isCommAdapterEnabled(),
                                              isCommAdapterConnected(),
                                              state,
                                              manualModeEnabled);
  }

  /**
   * Returns whether the peripheral device is manual mode (i.e. the user triggers the job
   * execution).
   *
   * @return Whether the peripheral device is manual mode.
   */
  public boolean isManualModeEnabled() {
    return manualModeEnabled;
  }

  /**
   * Creates a copy of the object, with the given value.
   *
   * @param manualModeEnabled The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public LoopbackPeripheralProcessModel withManualModeEnabled(boolean manualModeEnabled) {
    return new LoopbackPeripheralProcessModel(getLocation(),
                                              isCommAdapterEnabled(),
                                              isCommAdapterConnected(),
                                              getState(),
                                              manualModeEnabled);
  }

  /**
   * Used to describe what has changed in a process model.
   */
  public enum Attribute {
    /**
     * Indicates a change of the manual mode enabled property.
     */
    MANUAL_MODE_ENABLED,
  }
}
