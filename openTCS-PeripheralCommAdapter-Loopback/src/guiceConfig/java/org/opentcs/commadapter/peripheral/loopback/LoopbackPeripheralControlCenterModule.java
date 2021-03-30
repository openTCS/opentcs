/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 * Loopback adapter-specific Gucie configuration for the Kernel Control Center.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LoopbackPeripheralControlCenterModule
    extends ControlCenterInjectionModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(LoopbackPeripheralAdapterPanelComponentsFactory.class));

    peripheralCommAdapterPanelFactoryBinder()
        .addBinding().to(LoopbackPeripheralCommAdapterPanelFactory.class);
  }
}
