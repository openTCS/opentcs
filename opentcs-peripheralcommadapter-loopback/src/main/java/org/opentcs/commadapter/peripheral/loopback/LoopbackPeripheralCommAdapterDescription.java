// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.peripheral.loopback;

import static org.opentcs.commadapter.peripheral.loopback.I18nLoopbackPeripheralCommAdapter.BUNDLE_PATH;

import java.util.ResourceBundle;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * A {@link PeripheralCommAdapterDescription} for no comm adapter.
 */
public class LoopbackPeripheralCommAdapterDescription
    extends
      PeripheralCommAdapterDescription {

  /**
   * Creates a new instance.
   */
  public LoopbackPeripheralCommAdapterDescription() {
  }

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle(BUNDLE_PATH)
        .getString("loopbackPeripheralCommAdapterDescription.description");
  }
}
