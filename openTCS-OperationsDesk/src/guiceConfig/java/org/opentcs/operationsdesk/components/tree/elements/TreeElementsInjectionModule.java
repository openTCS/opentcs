/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.tree.elements;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.guing.common.components.tree.elements.UserObjectFactory;
import org.opentcs.guing.common.components.tree.elements.VehicleUserObject;

/**
 * A Guice module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class TreeElementsInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
        .implement(VehicleUserObject.class, VehicleUserObjectOperating.class)
        .build(UserObjectFactory.class));
  }
}
