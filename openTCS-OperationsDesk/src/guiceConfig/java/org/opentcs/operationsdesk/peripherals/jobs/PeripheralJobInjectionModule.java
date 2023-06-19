/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.peripherals.jobs;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;

/**
 * A Guice module for this package.
 */
public class PeripheralJobInjectionModule
    extends AbstractModule {

  /**
   * Creates a new instance.
   */
  public PeripheralJobInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(PeripheralJobViewFactory.class));
    bind(PeripheralJobsContainer.class).in(Singleton.class);
  }
}
