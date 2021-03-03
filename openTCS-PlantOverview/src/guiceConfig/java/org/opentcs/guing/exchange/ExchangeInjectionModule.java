/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.guing.exchange.adapter.ProcessAdapterFactory;

/**
 * A Guice configuration module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ExchangeInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {

    bind(KernelProxyManager.class)
        .to(DefaultKernelProxyManager.class)
        .in(Singleton.class);

    bind(SharedKernelProvider.class)
        .to(ApplicationKernelProvider.class)
        .in(Singleton.class);

    bind(Object.class)
        .annotatedWith(ApplicationKernelClient.class)
        .to(Object.class)
        .in(Singleton.class);

    bind(EventDispatcher.class).to(OpenTCSEventDispatcher.class);
    
    install(new FactoryModuleBuilder().build(ProcessAdapterFactory.class));
  }
}
