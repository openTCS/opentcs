/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import com.google.inject.AbstractModule;

/**
 * A Guice module for the persistence module of the kernel.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class PersistenceInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {
    bind(ModelPersister.class).to(XMLFileModelPersister.class);
    bind(OrderPersister.class).to(XMLFileOrderPersister.class);
    bind(XMLModelReader.class).to(XMLModel002Builder.class);
    bind(XMLModelWriter.class).to(XMLModel002Builder.class);
  }
}
