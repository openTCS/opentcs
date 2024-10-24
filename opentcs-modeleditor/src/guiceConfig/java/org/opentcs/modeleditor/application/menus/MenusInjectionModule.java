// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.menus;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 */
public class MenusInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public MenusInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(MenuFactory.class));
  }

}
