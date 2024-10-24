// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.common.peripherals;

import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * A {@link PeripheralCommAdapterDescription} for no comm adapter.
 */
public class NullPeripheralCommAdapterDescription
    extends
      PeripheralCommAdapterDescription {

  /**
   * Creates a new instance.
   */
  public NullPeripheralCommAdapterDescription() {
  }

  @Override
  public String getDescription() {
    return "-";
  }
}
