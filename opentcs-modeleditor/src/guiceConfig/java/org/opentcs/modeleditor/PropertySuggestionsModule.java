// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor;

import jakarta.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

/**
 * This module configures the multibinder used to suggest key value properties in the editor.
 */
public class PropertySuggestionsModule
    extends
      PlantOverviewInjectionModule {

  /**
   * Creates a new instance.
   */
  public PropertySuggestionsModule() {
  }

  @Override
  protected void configure() {
    propertySuggestionsBinder().addBinding()
        .to(DefaultPropertySuggestions.class)
        .in(Singleton.class);
  }
}
