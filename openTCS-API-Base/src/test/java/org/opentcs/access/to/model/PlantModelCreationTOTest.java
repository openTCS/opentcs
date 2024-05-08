/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.ModelConstants;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;

/**
 * Tests for {@link PlantModelCreationTO}.
 */
class PlantModelCreationTOTest {

  private Layer defaultLayer;
  private LayerGroup defaultLayerGroup;

  @BeforeEach
  void setUp() {
    defaultLayer = new Layer(ModelConstants.DEFAULT_LAYER_ID,
                             ModelConstants.DEFAULT_LAYER_ORDINAL,
                             true,
                             ModelConstants.DEFAULT_LAYER_NAME,
                             ModelConstants.DEFAULT_LAYER_GROUP_ID);

    defaultLayerGroup = new LayerGroup(ModelConstants.DEFAULT_LAYER_GROUP_ID,
                                       ModelConstants.DEFAULT_LAYER_GROUP_NAME,
                                       true);
  }

  @Test
  void addMissingLayerAndLayerGroupWhenAddingVisualLayout() {
    VisualLayoutCreationTO visualLayout = new VisualLayoutCreationTO("V1");
    PlantModelCreationTO plantModel = new PlantModelCreationTO("name")
        .withVisualLayout(visualLayout);

    assertThat(plantModel.getVisualLayout().getLayers(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayerGroups(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayers().get(0),
               samePropertyValuesAs(defaultLayer));
    assertThat(plantModel.getVisualLayout().getLayerGroups().get(0),
               samePropertyValuesAs(defaultLayerGroup));
  }

  @Test
  void addMissingLayerWhenAddingVisualLayout() {
    VisualLayoutCreationTO visualLayout = new VisualLayoutCreationTO("V1")
        .withLayerGroup(defaultLayerGroup);
    PlantModelCreationTO plantModel = new PlantModelCreationTO("name")
        .withVisualLayout(visualLayout);

    assertThat(plantModel.getVisualLayout().getLayers(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayerGroups(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayers().get(0),
               samePropertyValuesAs(defaultLayer));
    assertThat(plantModel.getVisualLayout().getLayerGroups().get(0),
               is(sameInstance(defaultLayerGroup)));
  }

  @Test
  void addMissingLayerGroupWhenAddingVisualLayout() {
    VisualLayoutCreationTO visualLayout = new VisualLayoutCreationTO("V1")
        .withLayer(defaultLayer);
    PlantModelCreationTO plantModel = new PlantModelCreationTO("name")
        .withVisualLayout(visualLayout);

    assertThat(plantModel.getVisualLayout().getLayers(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayerGroups(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayers().get(0),
               is(sameInstance(defaultLayer)));
    assertThat(plantModel.getVisualLayout().getLayerGroups().get(0),
               samePropertyValuesAs(defaultLayerGroup));
  }
}
