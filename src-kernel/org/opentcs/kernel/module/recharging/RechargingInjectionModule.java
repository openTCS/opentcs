/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.recharging;

import com.google.inject.AbstractModule;
import org.opentcs.algorithms.RechargeStrategy;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class RechargingInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(RechargeStrategy.class).to(SimpleRechargeStrategy.class);
  }
}
