/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.menus;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class MenusInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(MenuFactory.class));
  }

}
