// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.components.dockable;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;
import org.opentcs.guing.common.components.dockable.DockableHandlerFactory;
import org.opentcs.guing.common.components.dockable.DockingManager;

/**
 * A Guice module for this package.
 */
public class DockableInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public DockableInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(DockableHandlerFactory.class));

    bind(DockingManagerOperating.class).in(Singleton.class);
    bind(DockingManager.class).to(DockingManagerOperating.class);
  }
}
