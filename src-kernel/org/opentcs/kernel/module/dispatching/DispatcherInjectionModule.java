/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.dispatching;

import com.google.inject.AbstractModule;
import org.opentcs.algorithms.Dispatcher;
import org.opentcs.util.configuration.ConfigurationStore;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DispatcherInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
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
}
