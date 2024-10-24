// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

/**
 * Configures/binds the default importers and exporters of the openTCS plant overview.
 */
public class DefaultImportersExportersModule
    extends
      PlantOverviewInjectionModule {

  /**
   * Creates a new instance.
   */
  public DefaultImportersExportersModule() {
  }

  @Override
  protected void configure() {
    plantModelImporterBinder();
    plantModelExporterBinder();
  }
}
