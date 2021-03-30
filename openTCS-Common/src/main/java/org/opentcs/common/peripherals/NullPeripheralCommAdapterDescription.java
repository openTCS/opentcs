/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common.peripherals;

import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * A {@link PeripheralCommAdapterDescription} for no comm adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class NullPeripheralCommAdapterDescription
    extends PeripheralCommAdapterDescription {

  @Override
  public String getDescription() {
    return "-";
  }
}
