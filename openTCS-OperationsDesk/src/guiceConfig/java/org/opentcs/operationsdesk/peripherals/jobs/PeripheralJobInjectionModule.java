/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;

/**
 * A Guice module for this package.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class PeripheralJobInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(PeripheralJobsContainer.class).in(Singleton.class);
  }
}
