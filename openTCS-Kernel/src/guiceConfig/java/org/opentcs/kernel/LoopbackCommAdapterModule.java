/*
 * openTCS copyright information:
 * Copyright (c) 2016 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.virtualvehicle.LoopbackAdapterComponentsFactory;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapter;
import org.opentcs.virtualvehicle.LoopbackCommunicationAdapterFactory;

/**
 * Configures/binds the loopback communication adapters of the openTCS kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoopbackCommAdapterModule
    extends KernelInjectionModule {

  // tag::documentation_createCommAdapterModule[]
  @Override
  protected void configure() {
    configureLoopbackAdapterDependencies();
    vehicleCommAdaptersBinder().addBinding().to(LoopbackCommunicationAdapterFactory.class);
  }
  // end::documentation_createCommAdapterModule[]

  private void configureLoopbackAdapterDependencies() {
    install(new FactoryModuleBuilder().build(LoopbackAdapterComponentsFactory.class));

    ConfigurationStore configStore
        = ConfigurationStore.getStore(LoopbackCommunicationAdapter.class.getName());
    bindConstant()
        .annotatedWith(LoopbackCommunicationAdapter.CommandQueueCapacity.class)
        .to(configStore.getInt("commandQueueCapacity", 2));
    bindConstant()
        .annotatedWith(LoopbackCommunicationAdapter.RechargeOperation.class)
        .to(configStore.getString("rechargeOperation", "CHARGE"));
  }
}
