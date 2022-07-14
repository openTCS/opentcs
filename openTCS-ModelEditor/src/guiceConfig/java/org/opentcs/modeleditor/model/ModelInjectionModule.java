/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.model;

import com.google.inject.AbstractModule;
import org.opentcs.guing.common.model.StandardSystemModel;
import org.opentcs.guing.common.model.SystemModel;

/**
 * A Guice module for the model package.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(SystemModel.class).to(StandardSystemModel.class);
  }

}
