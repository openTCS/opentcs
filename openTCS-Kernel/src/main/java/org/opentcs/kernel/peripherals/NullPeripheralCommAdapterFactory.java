/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.peripherals;

import org.opentcs.common.peripherals.NullPeripheralCommAdapterDescription;
import org.opentcs.data.model.Location;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterFactory;

/**
 * A factory for {@link NullPeripheralCommAdapter}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class NullPeripheralCommAdapterFactory
    implements PeripheralCommAdapterFactory {

  /**
   * Creates a new NullPeripheralCommAdapterFactory.
   */
  public NullPeripheralCommAdapterFactory() {
  }

  @Override
  public PeripheralCommAdapterDescription getDescription() {
    return new NullPeripheralCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(Location location) {
    return true;
  }

  @Override
  public PeripheralCommAdapter getAdapterFor(Location location) {
    return new NullPeripheralCommAdapter(location.getReference());
  }

  @Override
  public void initialize() {
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public void terminate() {
  }
}
