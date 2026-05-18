// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PlantModelCreationTO}.
 */
class PlantModelCreationTOTest {

  private LayerCreationTO defaultLayer;
  private LayerGroupCreationTO defaultLayerGroup;

  @BeforeEach
  void setUp() {
    defaultLayer = new LayerCreationTO(
        ModelConstantsTO.DEFAULT_LAYER_ID,
        ModelConstantsTO.DEFAULT_LAYER_ORDINAL,
        true,
        ModelConstantsTO.DEFAULT_LAYER_NAME,
        ModelConstantsTO.DEFAULT_LAYER_GROUP_ID
    );

    defaultLayerGroup = new LayerGroupCreationTO(
        ModelConstantsTO.DEFAULT_LAYER_GROUP_ID,
        ModelConstantsTO.DEFAULT_LAYER_GROUP_NAME,
        true
    );
  }

  @Test
  void addMissingLayerAndLayerGroupWhenAddingVisualLayout() {
    VisualLayoutCreationTO visualLayout = new VisualLayoutCreationTO("V1");
    PlantModelCreationTO plantModel = new PlantModelCreationTO("name")
        .withVisualLayout(visualLayout);

    assertThat(plantModel.getVisualLayout().getLayers(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayerGroups(), hasSize(1));
    assertThat(
        plantModel.getVisualLayout().getLayers().getFirst(),
        samePropertyValuesAs(defaultLayer)
    );
    assertThat(
        plantModel.getVisualLayout().getLayerGroups().getFirst(),
        samePropertyValuesAs(defaultLayerGroup)
    );
  }

  @Test
  void addMissingLayerWhenAddingVisualLayout() {
    VisualLayoutCreationTO visualLayout = new VisualLayoutCreationTO("V1")
        .withLayerGroup(defaultLayerGroup);
    PlantModelCreationTO plantModel = new PlantModelCreationTO("name")
        .withVisualLayout(visualLayout);

    assertThat(plantModel.getVisualLayout().getLayers(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayerGroups(), hasSize(1));
    assertThat(
        plantModel.getVisualLayout().getLayers().getFirst(),
        samePropertyValuesAs(defaultLayer)
    );
    assertThat(
        plantModel.getVisualLayout().getLayerGroups().getFirst(),
        is(sameInstance(defaultLayerGroup))
    );
  }

  @Test
  void addMissingLayerGroupWhenAddingVisualLayout() {
    VisualLayoutCreationTO visualLayout = new VisualLayoutCreationTO("V1")
        .withLayer(defaultLayer);
    PlantModelCreationTO plantModel = new PlantModelCreationTO("name")
        .withVisualLayout(visualLayout);

    assertThat(plantModel.getVisualLayout().getLayers(), hasSize(1));
    assertThat(plantModel.getVisualLayout().getLayerGroups(), hasSize(1));
    assertThat(
        plantModel.getVisualLayout().getLayers().getFirst(),
        is(sameInstance(defaultLayer))
    );
    assertThat(
        plantModel.getVisualLayout().getLayerGroups().getFirst(),
        samePropertyValuesAs(defaultLayerGroup)
    );
  }
}
