// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050;

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
    install(new org.opentcs.commadapter.vehicle.vda5050.v1_1.ModelEditorInjectionModule());
    install(new org.opentcs.commadapter.vehicle.vda5050.v2_0.ModelEditorInjectionModule());
  }
}
