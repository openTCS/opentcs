// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.components.layer;

import jakarta.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.common.components.layer.LayerGroupManager;
import org.opentcs.guing.common.components.layer.LayerManager;

/**
 * A Guice module for this package.
 */
public class LayersInjectionModule
    extends
      PlantOverviewInjectionModule {

  /**
   * Creates a new instance.
   */
  public LayersInjectionModule() {
  }

  @Override
  protected void configure() {
    bind(LayerManagerModeling.class).in(Singleton.class);
    bind(LayerManager.class).to(LayerManagerModeling.class);
    bind(LayerEditorModeling.class).to(LayerManagerModeling.class);
    bind(ActiveLayerProvider.class).to(LayerManagerModeling.class);
    bind(LayersPanel.class).in(Singleton.class);

    bind(LayerGroupManager.class).to(LayerManagerModeling.class);
    bind(LayerGroupEditorModeling.class).to(LayerManagerModeling.class);
    bind(LayerGroupsPanel.class).in(Singleton.class);
  }
}
