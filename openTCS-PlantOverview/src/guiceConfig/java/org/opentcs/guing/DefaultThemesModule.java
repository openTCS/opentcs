/*
 * openTCS copyright information:
 * Copyright (c) 2016 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.plugins.themes.StandardLocationTheme;
import org.opentcs.guing.plugins.themes.StandardVehicleTheme;
import org.opentcs.guing.plugins.themes.StandardVehicleTheme2;

/**
 * Configures/binds the default vehicle and location themes of the openTCS plant overview.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
// tag::documentation_createThemeModule[]
public class DefaultThemesModule
    extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {
    vehicleThemeBinder().addBinding().to(StandardVehicleTheme.class);
    vehicleThemeBinder().addBinding().to(StandardVehicleTheme2.class);

    locationThemeBinder().addBinding().to(StandardLocationTheme.class);
  }
}
// end::documentation_createThemeModule[]
