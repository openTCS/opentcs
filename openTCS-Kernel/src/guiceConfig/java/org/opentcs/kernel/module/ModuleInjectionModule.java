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
import com.google.inject.Singleton;
import org.opentcs.algorithms.DeadlockPredictor;
import org.opentcs.algorithms.Dispatcher;
import org.opentcs.algorithms.ParkingStrategy;
import org.opentcs.algorithms.RechargeStrategy;
import org.opentcs.algorithms.RecoveryEvaluator;
import org.opentcs.algorithms.Scheduler;
import org.opentcs.kernel.module.routing.RouterInjectionModule;
import org.opentcs.strategies.basic.dispatching.OrderSequenceDispatcher;
import org.opentcs.strategies.basic.parking.OffRouteParkingStrategy;
import org.opentcs.strategies.basic.recharging.SimpleRechargeStrategy;
import org.opentcs.strategies.basic.scheduling.BasicScheduler;
import org.opentcs.strategies.basic.scheduling.DeadlockPredictorDummy;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 * A Guice module for the openTCS kernel modules/strategies.
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
    configureParking();
    configureRecharging();
    configureScheduler();
    configureDispatcher();
  }

  private void configureParking() {
    bind(ParkingStrategy.class).to(OffRouteParkingStrategy.class);
  }

  protected void configureRecharging() {
    bind(RechargeStrategy.class).to(SimpleRechargeStrategy.class);
  }
  private void configureDispatcher() {
    ConfigurationStore configStore
        = ConfigurationStore.getStore(OrderSequenceDispatcher.class.getName());
    bindConstant()
        .annotatedWith(OrderSequenceDispatcher.ParkWhenIdle.class)
        .to(configStore.getBoolean("parkIdleVehicles", false));
    bindConstant()
        .annotatedWith(OrderSequenceDispatcher.RechargeWhenIdle.class)
        .to(configStore.getBoolean("rechargeVehiclesWhenIdle", false));
    bindConstant()
        .annotatedWith(OrderSequenceDispatcher.RechargeWhenEnergyCritical.class)
        .to(configStore.getBoolean("rechargeVehiclesWhenEnergyCritical", false));

    bind(Dispatcher.class).to(OrderSequenceDispatcher.class);
  }

  private void configureScheduler() {
    bind(DeadlockPredictor.class)
        .to(DeadlockPredictorDummy.class)
        .in(Singleton.class);
    bind(Scheduler.class)
        .to(BasicScheduler.class)
        .in(Singleton.class);
  }
}
