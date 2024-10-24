// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.plugins.panels.loadgenerator.trigger;

import org.opentcs.access.KernelRuntimeException;

/**
 * Declares the methods of transport order generation triggers.
 */
public interface OrderGenerationTrigger {

  /**
   * Enables or disabled order generation.
   *
   * @param enabled true to enable, false to disable
   */
  void setTriggeringEnabled(boolean enabled);

  /**
   * Triggers order generation.
   *
   * @throws KernelRuntimeException In case the kernel threw an exception when
   * creating the transport orders.
   */
  void triggerOrderGeneration()
      throws KernelRuntimeException;
}
