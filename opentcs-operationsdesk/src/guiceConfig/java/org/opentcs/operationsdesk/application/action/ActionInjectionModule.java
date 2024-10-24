// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.action;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;

/**
 * An injection module for this package.
 */
public class ActionInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public ActionInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(ActionFactory.class));

    bind(ViewActionMap.class).in(Singleton.class);
  }

}
