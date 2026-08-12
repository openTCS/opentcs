// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import jakarta.inject.Singleton;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.operationsdesk.VDA5050VehicleCommAdapterMessageSuggestions;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

public class OperationsDeskInjectionModule
    extends
      PlantOverviewInjectionModule {
  /**
   * Creates a new instance.
   */
  public OperationsDeskInjectionModule() {
  }

  @Override
  protected void configure() {
    vehicleCommAdapterMessageSuggestionsBinder().addBinding()
        .to(VDA5050VehicleCommAdapterMessageSuggestions.class)
        .in(Singleton.class);
  }
}
