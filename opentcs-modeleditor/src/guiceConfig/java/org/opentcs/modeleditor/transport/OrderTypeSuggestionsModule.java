// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.transport;

import jakarta.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.common.transport.OrderTypeSuggestionsPool;

/**
 * A Guice module for the transport order type suggestions.
 */
public class OrderTypeSuggestionsModule
    extends
      PlantOverviewInjectionModule {

  /**
   * Creates a new instance.
   */
  public OrderTypeSuggestionsModule() {
  }

  @Override
  protected void configure() {
    orderTypeSuggestionsBinder().addBinding()
        .to(DefaultOrderTypeSuggestions.class)
        .in(Singleton.class);

    bind(OrderTypeSuggestionsPool.class).in(Singleton.class);
  }
}
