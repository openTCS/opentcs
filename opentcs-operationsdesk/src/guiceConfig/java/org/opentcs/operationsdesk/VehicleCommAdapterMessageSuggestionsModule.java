// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk;

import jakarta.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.operationsdesk.vehicles.MergedVehicleCommAdapterMessageSuggestions;

/**
 * This module configures the multibinder used to suggest {@link VehicleCommAdapterMessage}
 * parameters.
 */
public class VehicleCommAdapterMessageSuggestionsModule
    extends
      PlantOverviewInjectionModule {

  /**
   * Creates a new instance.
   */
  public VehicleCommAdapterMessageSuggestionsModule() {
  }

  @Override
  protected void configure() {
    vehicleCommAdapterMessageSuggestionsBinder().addBinding()
        .to(DefaultVehicleCommAdapterMessageSuggestions.class)
        .in(Singleton.class);

    bind(MergedVehicleCommAdapterMessageSuggestions.class).in(Singleton.class);
  }
}
