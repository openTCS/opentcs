// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VisualLayoutTO;

/**
 * Tests for {@link GetPlantModelResponseTO}.
 */
class GetPlantModelResponseTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createPlantModelMinimal()));
  }

  private GetPlantModelResponseTO createPlantModelMinimal() {
    return new GetPlantModelResponseTO()
        .setName("some-plant-model")
        .setProperties(Map.of())
        .setPoints(List.of())
        .setPaths(List.of())
        .setLocationTypes(List.of())
        .setLocations(List.of())
        .setBlocks(List.of())
        .setVehicles(List.of())
        .setVisualLayout(
            new VisualLayoutTO()
                .setName("some-visual-layout")
                .setProperties(Map.of())
                .setScaleX(1.2)
                .setScaleY(3.4)
                .setLayers(List.of())
                .setLayerGroups(List.of())
        );
  }
}
