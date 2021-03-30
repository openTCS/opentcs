/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;

/**
 * A default Guice module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UtilInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(PanelRegistry.class).in(Singleton.class);
  }
}
