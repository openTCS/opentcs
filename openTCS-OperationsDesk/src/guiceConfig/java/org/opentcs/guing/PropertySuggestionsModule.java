/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing;

import javax.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

/**
 * This module configures the multibinder used to suggest key value properties in the editor.
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class PropertySuggestionsModule
    extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {
    propertySuggestionsBinder().addBinding()
        .to(DefaultPropertySuggestions.class)
        .in(Singleton.class);
  }
}
