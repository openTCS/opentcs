/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module;

import com.google.inject.AbstractModule;
import org.opentcs.algorithms.RecoveryEvaluator;
import org.opentcs.kernel.module.dispatching.DispatcherInjectionModule;
import org.opentcs.kernel.module.parking.ParkingInjectionModule;
import org.opentcs.kernel.module.recharging.RechargingInjectionModule;
import org.opentcs.kernel.module.routing.RouterInjectionModule;
import org.opentcs.kernel.module.scheduling.SchedulingInjectionModule;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * A Guice module for the openTCS kernel modules.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModuleInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    ConfigurationStore evalConfigStore
        = ConfigurationStore.getStore(NESRecoveryEvaluator.class.getName());
    bindConstant()
        .annotatedWith(NESRecoveryEvaluator.Threshold.class)
        .to(evalConfigStore.getDouble("threshold", 0.7));
    bind(RecoveryEvaluator.class).to(NESRecoveryEvaluator.class);

    install(new RouterInjectionModule());
    install(new ParkingInjectionModule());
    install(new RechargingInjectionModule());
    install(new SchedulingInjectionModule());
    install(new DispatcherInjectionModule());
  }
}
