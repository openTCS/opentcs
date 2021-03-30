/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.data.model.Location;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterFactory;

/**
 * A factory for loopback communication adapters (virtual peripherals).
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackPeripheralCommAdapterFactory
    implements PeripheralCommAdapterFactory {

  /**
   * The adapter components factory.
   */
  private final LoopbackPeripheralAdapterComponentsFactory componentsFactory;
  /**
   * Indicates whether this component is initialized or not.
   */
  private boolean initialized;

  @Inject
  public LoopbackPeripheralCommAdapterFactory(
      LoopbackPeripheralAdapterComponentsFactory componentsFactory) {
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  @Override
  public PeripheralCommAdapterDescription getDescription() {
    return new LoopbackPeripheralCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(Location location) {
    requireNonNull(location, "location");
    return location.getProperties().containsKey(LocationProperties.PROPKEY_LOOPBACK_PERIPHERAL);
  }

  @Override
  public PeripheralCommAdapter getAdapterFor(Location location) {
    requireNonNull(location, "location");
    return componentsFactory.createLoopbackCommAdapter(location.getReference());
  }
}
