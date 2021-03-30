/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

/**
 * A factory for creating various comm adapter panel-specific instances.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface LoopbackPeripheralAdapterPanelComponentsFactory {

  /**
   * Creates a {@link LoopbackPeripheralCommAdapterPanel} representing the given process model's
   * content.
   *
   * @param processModel The process model to represent.
   * @return The comm adapter panel.
   */
  LoopbackPeripheralCommAdapterPanel createPanel(LoopbackPeripheralProcessModel processModel);
}
