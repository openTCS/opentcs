/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter;

import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;
import org.opentcs.virtualvehicle.LoopbackCommAdapterPanelFactory;

/**
 * Registers the loopback adapter's panels.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoopbackCommAdapterPanelsModule
    extends ControlCenterInjectionModule {

  // tag::documentation_createCommAdapterPanelsModule[]
  @Override
  protected void configure() {
    commAdapterPanelFactoryBinder().addBinding().to(LoopbackCommAdapterPanelFactory.class);
  }
  // end::documentation_createCommAdapterPanelsModule[]
}
