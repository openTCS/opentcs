/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling;

import com.google.inject.multibindings.Multibinder;
import javax.inject.Singleton;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.opentcs.strategies.basic.scheduling.modules.SameDirectionBlockModule;
import org.opentcs.strategies.basic.scheduling.modules.SingleVehicleBlockModule;

/**
 * Guice configuration for the default scheduler.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultSchedulerModule
    extends KernelInjectionModule {

  @Override
  protected void configure() {
    configureSchedulerDependencies();
    bindScheduler(DefaultScheduler.class);
  }

  private void configureSchedulerDependencies() {
    bind(ReservationPool.class).in(Singleton.class);

    Multibinder<Scheduler.Module> moduleBinder = Multibinder.newSetBinder(binder(),
                                                                          Scheduler.Module.class);
    moduleBinder.addBinding().to(SingleVehicleBlockModule.class);
    moduleBinder.addBinding().to(SameDirectionBlockModule.class);
  }
}
