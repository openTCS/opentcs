/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.scheduling;

import com.google.inject.AbstractModule;
import org.opentcs.algorithms.DeadlockPredictor;
import org.opentcs.algorithms.Scheduler;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SchedulingInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(DeadlockPredictor.class).to(DeadlockPredictorDummy.class);
    bind(Scheduler.class).to(BasicScheduler.class);
  }
}
