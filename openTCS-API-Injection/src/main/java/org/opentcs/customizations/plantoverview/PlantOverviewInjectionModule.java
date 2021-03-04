/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations.plantoverview;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.opentcs.components.plantoverview.VehicleTheme;

/**
 * A base class for Guice modules adding or customizing bindings for the plant overview application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class PlantOverviewInjectionModule
    extends AbstractModule {

  /**
   * Returns a multibinder that can be used to register vehicle themes.
   *
   * @return The multibinder.
   */
  protected Multibinder<VehicleTheme> vehicleThemeBinder() {
    return Multibinder.newSetBinder(binder(), VehicleTheme.class);
  }

  /**
   * Returns a multibinder that can be used to register location themes.
   *
   * @return The multibinder.
   */
  protected Multibinder<LocationTheme> locationThemeBinder() {
    return Multibinder.newSetBinder(binder(), LocationTheme.class);
  }

  /**
   * Returns a multibinder that can be used to register factories for pluggable panels.
   *
   * @return The multibinder.
   */
  protected Multibinder<PluggablePanelFactory> pluggablePanelFactoryBinder() {
    return Multibinder.newSetBinder(binder(), PluggablePanelFactory.class);
  }
}
