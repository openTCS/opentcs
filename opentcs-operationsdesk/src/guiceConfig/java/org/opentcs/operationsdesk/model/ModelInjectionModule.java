// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.model;

import com.google.inject.AbstractModule;
import org.opentcs.guing.common.model.SystemModel;

/**
 * A Guice module for the model package.
 */
public class ModelInjectionModule
    extends
      AbstractModule {

  /**
   * Creates a new instance.
   */
  public ModelInjectionModule() {
  }

  @Override
  protected void configure() {
    bind(SystemModel.class).to(CachedSystemModel.class);
  }

}
