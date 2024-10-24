// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.components.tree.elements;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.guing.common.components.tree.elements.UserObjectFactory;
import org.opentcs.guing.common.components.tree.elements.VehicleUserObject;

/**
 * A Guice module for this package.
 */
public class TreeElementsInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public TreeElementsInjectionModule() {
  }

  @Override
  protected void configure() {
    install(
        new FactoryModuleBuilder()
            .implement(VehicleUserObject.class, VehicleUserObjectModeling.class)
            .build(UserObjectFactory.class)
    );
  }
}
