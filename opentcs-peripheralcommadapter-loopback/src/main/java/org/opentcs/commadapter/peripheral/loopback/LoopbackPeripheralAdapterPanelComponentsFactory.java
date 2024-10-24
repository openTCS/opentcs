// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.peripheral.loopback;

/**
 * A factory for creating various comm adapter panel-specific instances.
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
