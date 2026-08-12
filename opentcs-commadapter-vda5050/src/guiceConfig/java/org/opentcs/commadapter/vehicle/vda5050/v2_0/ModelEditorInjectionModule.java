// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import jakarta.inject.Singleton;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.modeleditor.ModelEditorPropertySuggestions;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

public class ModelEditorInjectionModule
    extends
      PlantOverviewInjectionModule {

  /**
   * Creates a new instance.
   */
  public ModelEditorInjectionModule() {

  }

  @Override
  protected void configure() {
    propertySuggestionsBinder().addBinding()
        .to(ModelEditorPropertySuggestions.class)
        .in(Singleton.class);
  }
}
