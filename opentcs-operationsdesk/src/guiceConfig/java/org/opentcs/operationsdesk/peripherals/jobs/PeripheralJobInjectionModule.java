// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.peripherals.jobs;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;

/**
 * A Guice module for this package.
 */
public class PeripheralJobInjectionModule
    extends
      AbstractModule {

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
