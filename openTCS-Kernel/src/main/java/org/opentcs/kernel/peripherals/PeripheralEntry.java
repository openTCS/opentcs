/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.peripherals;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralCommAdapter;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterFactory;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;

/**
 * An entry for a peripheral device represented by a {@link Location}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralEntry {

  /**
   * The peripheral comm adapter factory that created this entry's comm adapter instance.
   */
  private PeripheralCommAdapterFactory commAdapterFactory = new NullPeripheralCommAdapterFactory();
  /**
   * The comm adapter instance for this entry.
   */
  private PeripheralCommAdapter commAdapter;

  /**
   * Creates a new instance.
   *
   * @param location The location representing the pripheral device.
   */
  public PeripheralEntry(@Nonnull Location location) {
    requireNonNull(location, "location");
    this.commAdapter = commAdapterFactory.getAdapterFor(location);
  }

  @Nonnull
  public PeripheralProcessModel getProcessModel() {
    return commAdapter.getProcessModel();
  }

  @Nonnull
  public TCSResourceReference<Location> getLocation() {
    return getProcessModel().getLocation();
  }

  @Nonnull
  public PeripheralCommAdapterFactory getCommAdapterFactory() {
    return commAdapterFactory;
  }

  public void setCommAdapterFactory(@Nonnull PeripheralCommAdapterFactory commAdapterFactory) {
    this.commAdapterFactory = requireNonNull(commAdapterFactory, "commAdapterFactory");
  }

  @Nonnull
  public PeripheralCommAdapter getCommAdapter() {
    return commAdapter;
  }

  public void setCommAdapter(@Nonnull PeripheralCommAdapter commAdapter) {
    this.commAdapter = requireNonNull(commAdapter, "commAdapter");
  }

  @Override
  public String toString() {
    return "PeripheralEntry{"
        + "commAdapterFactory=" + commAdapterFactory + ", "
        + "commAdapter=" + commAdapter + '}';
  }
}
