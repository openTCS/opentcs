// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernelcontrolcenter;

import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;
import org.opentcs.virtualvehicle.LoopbackCommAdapterPanelFactory;

/**
 * Registers the loopback adapter's panels.
 */
public class LoopbackCommAdapterPanelsModule
    extends
      ControlCenterInjectionModule {

  /**
   * Creates a new instance.
   */
  public LoopbackCommAdapterPanelsModule() {
  }

  // tag::documentation_createCommAdapterPanelsModule[]
  @Override
  protected void configure() {
    commAdapterPanelFactoryBinder().addBinding().to(LoopbackCommAdapterPanelFactory.class);
  }
  // end::documentation_createCommAdapterPanelsModule[]
}
