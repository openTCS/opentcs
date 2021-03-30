/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import java.util.ResourceBundle;
import static org.opentcs.commadapter.peripheral.loopback.I18nLoopbackPeripheralCommAdapter.BUNDLE_PATH;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;

/**
 * A {@link PeripheralCommAdapterDescription} for no comm adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackPeripheralCommAdapterDescription
    extends PeripheralCommAdapterDescription {

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle(BUNDLE_PATH)
        .getString("loopbackPeripheralCommAdapterDescription.description");
  }
}
