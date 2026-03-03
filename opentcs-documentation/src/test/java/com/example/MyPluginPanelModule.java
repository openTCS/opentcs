// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
// tag::tutorial_gettingstarted_MyPluginPanelModule[]
package com.example;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

/**
 * An exemplary injection module.
 */
public class MyPluginPanelModule
    extends
      PlantOverviewInjectionModule {

  public MyPluginPanelModule() {
  }

  @Override
  protected void configure() {
    pluggablePanelFactoryBinder().addBinding().to(MyPluginPanelFactory.class);
  }
}
// end::tutorial_gettingstarted_MyPluginPanelModule[]
