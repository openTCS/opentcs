// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.peripherals.management;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;

/**
 * Instances of this class represent events emitted by/for changes on
 * {@link PeripheralProcessModel}s.
 */
public class PeripheralProcessModelEvent
    extends
      PeripheralCommAdapterEvent
    implements
      Serializable {

  /**
   * The location assiciated with the peripheral device.
   */
  private final TCSResourceReference<Location> location;
  /**
   * The name of the attribute that has changed in the process model.
   */
  private final String attributeChanged;
  /**
   * The process model with its current/changed state.
   */
  private final PeripheralProcessModel processModel;

  /**
   * Creates a new instance.
   *
   * @param location The location assiciated with the peripheral device.
   * @param attributeChanged The name of the attribute that has changed in the process model.
   * @param processModel The process model with its current/changed state.
   */
  public PeripheralProcessModelEvent(
      @Nonnull
      TCSResourceReference<Location> location,
      @Nonnull
      String attributeChanged,
      @Nonnull
      PeripheralProcessModel processModel
  ) {
    this.location = requireNonNull(location, "location");
    this.attributeChanged = requireNonNull(attributeChanged, "attributeChanged");
    this.processModel = requireNonNull(processModel, "processModel");
  }

  /**
   * Returns the location assiciated with the peripheral device.
   *
   * @return The location.
   */
  public TCSResourceReference<Location> getLocation() {
    return location;
  }

  /**
   * Returns the name of the attribute that has changed in the process model.
   *
   * @return The name of the attribute that has changed in the process model.
   */
  public String getAttributeChanged() {
    return attributeChanged;
  }

  /**
   * Returns the process model with its current/changed state.
   *
   * @return The process model with its current/changed state.
   */
  public PeripheralProcessModel getProcessModel() {
    return processModel;
  }
}
