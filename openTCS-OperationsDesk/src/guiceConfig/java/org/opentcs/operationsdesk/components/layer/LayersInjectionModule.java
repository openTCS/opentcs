/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.layer;

import javax.inject.Singleton;
import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;
import org.opentcs.guing.common.components.layer.DefaultLayerManager;
import org.opentcs.guing.common.components.layer.LayerEditor;
import org.opentcs.guing.common.components.layer.LayerGroupEditor;
import org.opentcs.guing.common.components.layer.LayerGroupManager;
import org.opentcs.guing.common.components.layer.LayerManager;

/**
 * A Guice module for this package.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LayersInjectionModule
    extends PlantOverviewInjectionModule {

  @Override
  protected void configure() {
    bind(DefaultLayerManager.class).in(Singleton.class);
    bind(LayerManager.class).to(DefaultLayerManager.class);
    bind(LayerEditor.class).to(DefaultLayerManager.class);
    bind(LayersPanel.class).in(Singleton.class);
    
    bind(LayerGroupManager.class).to(DefaultLayerManager.class);
    bind(LayerGroupEditor.class).to(DefaultLayerManager.class);
    bind(LayerGroupsPanel.class).in(Singleton.class);
  }
}
