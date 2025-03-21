// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

/**
 * An event emitted by a communication adapter.
 */
public class VehicleCommAdapterEvent
    implements
      Serializable {

  /**
   * The name of the adapter that emitted this event.
   */
  private final String adapterName;
  /**
   * An optional appendix containing additional arbitrary information about the
   * event.
   */
  private final Serializable appendix;

  /**
   * Creates a new instance.
   *
   * @param adapterName The name of the adapter that emitted this event.
   * @param appendix An optional appendix containing additional arbitrary
   * information about the event.
   */
  public VehicleCommAdapterEvent(String adapterName, Serializable appendix) {
    this.adapterName = requireNonNull(adapterName, "adapterName");
    this.appendix = appendix;
  }

  /**
   * Creates a new instance without an appendix.
   *
   * @param adapterName The name of the adapter that emitted this event.
   */
  public VehicleCommAdapterEvent(String adapterName) {
    this(adapterName, null);
  }

  /**
   * Returns the name of the adapter that emitted this event.
   *
   * @return The name of the adapter that emitted this event.
   */
  public String getAdapterName() {
    return adapterName;
  }

  /**
   * Returns the (optional) appendix containing additional arbitrary information
   * about the event.
   *
   * @return The (optional) appendix containing additional arbitrary information
   * about the event.
   */
  public Serializable getAppendix() {
    return appendix;
  }

  @Override
  public String toString() {
    return "VehicleCommAdapterEvent{"
        + "adapterName=" + adapterName
        + ", appendix=" + appendix
        + '}';
  }

}
