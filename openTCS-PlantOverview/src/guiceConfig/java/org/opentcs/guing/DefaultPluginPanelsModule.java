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
import org.opentcs.guing.plugins.panels.allocation.ResourceAllocationPanelFactory;
import org.opentcs.guing.plugins.panels.loadgenerator.ContinuousLoadPanelFactory;
import org.opentcs.guing.plugins.panels.statistics.StatisticsPanelFactory;

/**
 * Configures/binds the default plugin panels of the openTCS plant overview.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
// tag::documentation_createPluginPanelModule[]
public class DefaultPluginPanelsModule
    extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {
    pluggablePanelFactoryBinder().addBinding().to(ContinuousLoadPanelFactory.class);
    pluggablePanelFactoryBinder().addBinding().to(StatisticsPanelFactory.class);
    pluggablePanelFactoryBinder().addBinding().to(ResourceAllocationPanelFactory.class);
  }
}
// end::documentation_createPluginPanelModule[]
