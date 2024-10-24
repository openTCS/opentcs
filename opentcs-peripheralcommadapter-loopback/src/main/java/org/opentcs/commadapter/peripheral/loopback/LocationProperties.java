// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.peripheral.loopback;

/**
 */
public interface LocationProperties {

  /**
   * The key of the location property indicating that the location represents a peripheral device
   * and that it should be attached to the loopback peripheral communication adapter.
   */
  String PROPKEY_LOOPBACK_PERIPHERAL = "tcs:loopbackPeripheral";
}
