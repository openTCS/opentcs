/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * A Guice module for process adapters.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ProcessAdapterInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
//    bind(ProcessAdapterFactory.class)
//        .toInstance(ProcessAdapterFactory.instance());
    bind(ProcessAdapterFactory.class).in(Singleton.class);
  }
}
