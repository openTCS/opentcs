/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.transport;

import javax.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

/**
 * A Guice module for the transport order category suggestions.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderCategorySuggestionsModule
    extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {
    orderCategorySuggestionsBinder().addBinding()
        .to(DefaultOrderCategorySuggestions.class)
        .in(Singleton.class);

    bind(OrderCategorySuggestionsPool.class).in(Singleton.class);
  }
}
