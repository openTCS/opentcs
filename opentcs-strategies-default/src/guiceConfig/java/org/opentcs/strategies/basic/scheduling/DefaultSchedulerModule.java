// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling;

import com.google.inject.multibindings.Multibinder;
import jakarta.inject.Singleton;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.strategies.basic.scheduling.modules.PausedVehicleModule;
import org.opentcs.strategies.basic.scheduling.modules.SameDirectionBlockModule;
import org.opentcs.strategies.basic.scheduling.modules.SingleVehicleBlockModule;
import org.opentcs.strategies.basic.scheduling.modules.areaAllocation.AreaAllocationModule;
import org.opentcs.strategies.basic.scheduling.modules.areaAllocation.AreaProvider;
import org.opentcs.strategies.basic.scheduling.modules.areaAllocation.CachingAreaProvider;

/**
 * Guice configuration for the default scheduler.
 */
public class DefaultSchedulerModule
    extends
      KernelInjectionModule {

  /**
   * Creates a new instance.
   */
  public DefaultSchedulerModule() {
  }

  @Override
  protected void configure() {
    configureSchedulerDependencies();
    bindScheduler(DefaultScheduler.class);
  }

  private void configureSchedulerDependencies() {
    bind(ReservationPool.class).in(Singleton.class);

    Multibinder<Scheduler.Module> moduleBinder = schedulerModuleBinder();
    moduleBinder.addBinding().to(SingleVehicleBlockModule.class);
    moduleBinder.addBinding().to(SameDirectionBlockModule.class);
    moduleBinder.addBinding().to(PausedVehicleModule.class);

    moduleBinder.addBinding().to(AreaAllocationModule.class);
    bind(AreaProvider.class)
        .to(CachingAreaProvider.class)
        .in(Singleton.class);
  }
}
