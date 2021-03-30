/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import com.google.inject.AbstractModule;

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
