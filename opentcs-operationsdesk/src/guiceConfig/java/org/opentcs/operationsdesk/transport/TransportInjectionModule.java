// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.transport;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;
import org.opentcs.guing.common.exchange.AllocatedResourcesContainer;
import org.opentcs.operationsdesk.transport.orders.TransportOrdersContainer;
import org.opentcs.operationsdesk.transport.orders.TransportViewFactory;
import org.opentcs.operationsdesk.transport.sequences.OrderSequencesContainer;

/**
 * A Guice module for this package.
 */
public class TransportInjectionModule
    extends
      AbstractModule {

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

    bind(AllocatedResourcesContainer.class)
        .in(Singleton.class);
  }
}
