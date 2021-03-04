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
import javax.inject.Singleton;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.DefaultPortalManager;
import org.opentcs.common.PortalManager;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.event.SimpleEventBus;

/**
 * A Guice configuration module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ExchangeInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {

    bind(PortalManager.class)
        .to(DefaultPortalManager.class)
        .in(Singleton.class);

    bind(KernelEventFetcher.class)
        .in(Singleton.class);

    EventBus eventBus = new SimpleEventBus();
    bind(EventSource.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(eventBus);
    bind(EventHandler.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(eventBus);
    bind(EventBus.class)
        .annotatedWith(ApplicationEventBus.class)
        .toInstance(eventBus);

    bind(SharedKernelServicePortalProvider.class)
        .to(ApplicationPortalProvider.class)
        .in(Singleton.class);

    bind(AttributeAdapterRegistry.class)
        .in(Singleton.class);
    bind(OpenTCSEventDispatcher.class)
        .in(Singleton.class);
  }
}
