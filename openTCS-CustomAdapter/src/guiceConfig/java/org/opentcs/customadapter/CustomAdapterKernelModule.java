package org.opentcs.customadapter;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomAdapterKernelModule
    extends
      KernelInjectionModule {

  private static final Logger LOG = LoggerFactory.getLogger(CustomAdapterKernelModule.class);

  /**
   * A class that represents a custom adapter kernel module.
   * This module is responsible for configuring bindings for the kernel application.
   */
  public CustomAdapterKernelModule() {
  }

  @Override
  protected void configure() {
    bind(CommunicationStrategy.class).in(Singleton.class);
    bind(CustomAdapterComponentsFactory.class).to(CommunicationStrategy.class);

    MapBinder<String, StrategyCreator> strategyBinder
        = MapBinder.newMapBinder(binder(), String.class, StrategyCreator.class);
    strategyBinder.addBinding("ModbusTCP").to(ModbusTCPStrategy.class);

    bind(ModbusTCPVehicleCommAdapter.class);

    install(
        new FactoryModuleBuilder()
            .implement(CustomVehicleCommAdapter.class, ModbusTCPVehicleCommAdapter.class)
            .build(CustomCommunicationAdapterFactory.class)
    );

    vehicleCommAdaptersBinder().addBinding().to(CustomCommunicationAdapterFactory.class);
  }
}
