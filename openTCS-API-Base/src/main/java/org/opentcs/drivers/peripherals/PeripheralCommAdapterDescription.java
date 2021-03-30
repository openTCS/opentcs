/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Provides the description for a peripheral communication adapter.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class PeripheralCommAdapterDescription
    implements Serializable {

  /**
   * Returns the description for a peripheral communication adapter.
   *
   * @return The description for a peripheral communication adapter.
   */
  @Nonnull
  public abstract String getDescription();

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PeripheralCommAdapterDescription)) {
      return false;
    }

    return getDescription().equals(((PeripheralCommAdapterDescription) obj).getDescription());
  }

  @Override
  public int hashCode() {
    return getDescription().hashCode();
  }
}
