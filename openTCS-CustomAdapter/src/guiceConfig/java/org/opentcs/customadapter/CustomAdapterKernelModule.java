package org.opentcs.customadapter;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    bind(ScheduledExecutorService.class).annotatedWith(Names.named("executor1")).toInstance(
        Executors.newScheduledThreadPool(1)
    );
    bind(CustomCommunicationAdapterFactory.class).to(CustomCommunicationAdapterFactoryImpl.class);

    vehicleCommAdaptersBinder().addBinding().to(CustomCommunicationAdapterFactory.class);

    install(
        new FactoryModuleBuilder()
            .implement(CustomVehicleCommAdapter.class, ModbusTCPVehicleCommAdapter.class)
            .build(CustomAdapterComponentsFactory.class)
    );

    MapBinder<String, StrategyCreator> strategyBinder
        = MapBinder.newMapBinder(binder(), String.class, StrategyCreator.class);
    strategyBinder.addBinding("ModbusTCP").to(ModbusTCPStrategy.class);

    bind(Boolean.class).toInstance(Boolean.FALSE);
    bind(Integer.class).toInstance(0);


  }
}
