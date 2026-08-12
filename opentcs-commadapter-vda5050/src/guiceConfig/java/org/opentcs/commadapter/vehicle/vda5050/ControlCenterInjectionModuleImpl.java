// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050;

import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 * A custom Guice module for project-specific configuration.
 */
public class ControlCenterInjectionModuleImpl
    extends
      ControlCenterInjectionModule {

  /**
   * Creates a new instance.
   */
  public ControlCenterInjectionModuleImpl() {
  }

  @Override
  protected void configure() {
    install(new org.opentcs.commadapter.vehicle.vda5050.v1_1.ControlCenterInjectionModuleImpl());
    install(new org.opentcs.commadapter.vehicle.vda5050.v2_0.ControlCenterInjectionModuleImpl());
  }
}
