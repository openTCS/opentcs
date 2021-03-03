/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import com.google.inject.AbstractModule;
import javax.inject.Singleton;
import org.opentcs.guing.plugins.themes.LocationThemeRegistry;
import org.opentcs.guing.plugins.themes.VehicleThemeRegistry;

/**
 * A default Guice module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class UtilInjectionModule
    extends AbstractModule {

  @Override
  protected void configure() {

    bind(ApplicationConfiguration.class).in(Singleton.class);
    
    bind(PanelRegistry.class).in(Singleton.class);

    bind(VehicleThemeRegistry.class)
        .in(Singleton.class);
    // XXX There are still some classes that need to access the singleton via
    // XXX getInstance(), so it also has to be set via setInstance() on startup
    // XXX for now.
    bind(DefaultVehicleThemeManager.class)
        .in(Singleton.class);
    bind(VehicleThemeManager.class)
        .to(DefaultVehicleThemeManager.class);

    bind(LocationThemeRegistry.class)
        .in(Singleton.class);
    // XXX There are still some classes that need to access the singleton via
    // XXX getInstance(), so it also has to be set via setInstance() on startup
    // XXX for now.
    bind(DefaultLocationThemeManager.class)
        .in(Singleton.class);
    bind(LocationThemeManager.class)
        .to(DefaultLocationThemeManager.class);
  }
}
