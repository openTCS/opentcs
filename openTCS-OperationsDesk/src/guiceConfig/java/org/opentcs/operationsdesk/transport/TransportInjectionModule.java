/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.transport;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;
import org.opentcs.operationsdesk.transport.orders.TransportOrdersContainer;
import org.opentcs.operationsdesk.transport.orders.TransportViewFactory;
import org.opentcs.operationsdesk.transport.sequences.OrderSequencesContainer;

/**
 * A Guice module for this package.
 */
public class TransportInjectionModule
    extends AbstractModule {

  /**
   * Creates a new instance.
   */
  public TransportInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(TransportViewFactory.class));

    bind(TransportOrdersContainer.class)
        .in(Singleton.class);

    bind(OrderSequencesContainer.class)
        .in(Singleton.class);
  }
}
