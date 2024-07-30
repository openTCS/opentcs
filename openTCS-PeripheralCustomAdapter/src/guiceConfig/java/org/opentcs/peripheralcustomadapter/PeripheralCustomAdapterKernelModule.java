package org.opentcs.peripheralcustomadapter;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeripheralCustomAdapterKernelModule
    extends
      KernelInjectionModule {

  private static final Logger LOG = LoggerFactory.getLogger(
      PeripheralCustomAdapterKernelModule.class
  );

  /**
   * A class that represents a custom adapter kernel module.
   * This module is responsible for configuring bindings for the kernel application.
   */
  public PeripheralCustomAdapterKernelModule() {
  }

  @Override
  protected void configure() {
    bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(1));

    install(
        new FactoryModuleBuilder()
            .implement(
                PeripheralCommunicationAdapter.class, ModbusTCPPeripheralCommunicationAdapter.class
            )
            .build(PeripheralCustomAdapterComponentsFactory.class)
    );

    MapBinder<String, StrategyCreator> strategyBinder
        = MapBinder.newMapBinder(binder(), String.class, StrategyCreator.class);
    strategyBinder.addBinding("ModbusTCP").to(PeripheralModbusTCPStrategy.class);

    bind(Boolean.class).toInstance(Boolean.FALSE);
    bind(Integer.class).toInstance(0);
  }
}
