// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.components.dialogs;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;

/**
 * A Guice module for this package.
 */
public class DialogsInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public DialogsInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(SingleVehicleViewFactory.class));
    install(new FactoryModuleBuilder().build(FindVehiclePanelFactory.class));

    bind(VehiclesPanel.class).in(Singleton.class);
  }
}
