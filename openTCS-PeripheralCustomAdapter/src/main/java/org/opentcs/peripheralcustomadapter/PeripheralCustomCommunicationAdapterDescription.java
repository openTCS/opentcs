package org.opentcs.peripheralcustomadapter;

import jakarta.annotation.Nonnull;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * A {@link PeripheralCustomCommunicationAdapterDescription} for no comm adapter.
 */

public class PeripheralCustomCommunicationAdapterDescription
    extends
      PeripheralCommAdapterDescription {

  /**
   * Creates a new instance.
   */
  public PeripheralCustomCommunicationAdapterDescription() {
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Peripheral Custom Communication Adapter";
  }
}
