// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.peripherals;

import jakarta.annotation.Nonnull;
import java.io.Serializable;

/**
 * Provides the description for a peripheral communication adapter.
 */
public abstract class PeripheralCommAdapterDescription
    implements
      Serializable {

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
