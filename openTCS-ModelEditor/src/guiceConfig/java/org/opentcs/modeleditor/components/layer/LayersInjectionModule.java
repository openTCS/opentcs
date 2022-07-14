/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.components.layer;

import javax.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.common.components.layer.LayerGroupManager;
import org.opentcs.guing.common.components.layer.LayerManager;

/**
 * A Guice module for this package.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LayersInjectionModule
    extends PlantOverviewInjectionModule {

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
