/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LocationProperties {

  /**
   * The key of the location property indicating that the location represents a peripheral device
   * and that it should be attached to the loopback peripheral communication adapter.
   */
  String PROPKEY_LOOPBACK_PERIPHERAL = "tcs:loopbackPeripheral";
}
