/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import org.opentcs.access.KernelRuntimeException;

/**
 * Declares the methods of transport order generation triggers.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
interface OrderGenerationTrigger {

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
