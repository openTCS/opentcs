/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.dockable;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;
import org.opentcs.guing.common.components.dockable.DockableHandlerFactory;
import org.opentcs.guing.common.components.dockable.DockingManager;

/**
 * A Guice module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DockableInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(DockableHandlerFactory.class));

    bind(DockingManagerOperating.class).in(Singleton.class);
    bind(DockingManager.class).to(DockingManagerOperating.class);
  }
}
