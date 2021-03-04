/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.customizations.plantoverview;

import com.google.inject.multibindings.Multibinder;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.components.plantoverview.OrderCategorySuggestions;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.opentcs.components.plantoverview.PropertySuggestions;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.customizations.ConfigurableInjectionModule;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A base class for Guice modules adding or customizing bindings for the plant overview application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class PlantOverviewInjectionModule
    extends ConfigurableInjectionModule {

  /**
   * Returns a multibinder that can be used to register vehicle themes.
   *
   * @return The multibinder.
   * @deprecated The theme to be used is now set directly via configuration.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  protected Multibinder<VehicleTheme> vehicleThemeBinder() {
    return Multibinder.newSetBinder(binder(), VehicleTheme.class);
  }

  /**
   * Returns a multibinder that can be used to register location themes.
   *
   * @return The multibinder.
   * @deprecated The theme to be used is now set directly via configuration.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  protected Multibinder<LocationTheme> locationThemeBinder() {
    return Multibinder.newSetBinder(binder(), LocationTheme.class);
  }

  /**
   * Returns a multibinder that can be used to register plant model importers.
   *
   * @return The multibinder.
   */
  protected Multibinder<PlantModelImporter> plantModelImporterBinder() {
    return Multibinder.newSetBinder(binder(), PlantModelImporter.class);
  }

  /**
   * Returns a multibinder that can be used to register plant model exporters.
   *
   * @return The multibinder.
   */
  protected Multibinder<PlantModelExporter> plantModelExporterBinder() {
    return Multibinder.newSetBinder(binder(), PlantModelExporter.class);
  }

  /**
   * Returns a multibinder that can be used to register factories for pluggable panels.
   *
   * @return The multibinder.
   */
  protected Multibinder<PluggablePanelFactory> pluggablePanelFactoryBinder() {
    return Multibinder.newSetBinder(binder(), PluggablePanelFactory.class);
  }

  /**
   * Returns a multibinder that can be used to register classes that provide suggested properties.
   *
   * @return The multibinder.
   */
  protected Multibinder<PropertySuggestions> propertySuggestionsBinder() {
    return Multibinder.newSetBinder(binder(), PropertySuggestions.class);
  }

  /**
   * Returns a multibinder that can be used to register classes that provide suggested order
   * categories.
   *
   * @return The multibinder.
   */
  protected Multibinder<OrderCategorySuggestions> orderCategorySuggestionsBinder() {
    return Multibinder.newSetBinder(binder(), OrderCategorySuggestions.class);
  }
}
