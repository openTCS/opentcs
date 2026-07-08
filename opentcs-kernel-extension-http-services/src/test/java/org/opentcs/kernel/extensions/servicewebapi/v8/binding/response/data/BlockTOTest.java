// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ColorTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ResourceTO;

/**
 * Tests for {@link BlockTO}.
 */
class BlockTOTest {

  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createBlockMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createBlockFull()));
  }

  private BlockTO createBlockMinimal() {
    return new BlockTO()
        .setName("some-block")
        .setProperties(Map.of())
        .setType(BlockTO.TypeTO.SINGLE_VEHICLE_ONLY)
        .setMembers(List.of())
        .setLayout(
            new BlockTO.LayoutTO()
                .setColor(new ColorTO().setRed(1).setGreen(2).setBlue(3))
        );
  }

  private BlockTO createBlockFull() {
    return new BlockTO()
        .setName("some-block")
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "some-other-key", "some-other-value"
            )
        )
        .setType(BlockTO.TypeTO.SINGLE_VEHICLE_ONLY)
        .setMembers(
            List.of(
                new ResourceTO()
                    .setName("some-point")
                    .setType(ResourceTO.ResourceTypeTO.POINT),
                new ResourceTO()
                    .setName("some-path")
                    .setType(ResourceTO.ResourceTypeTO.PATH),
                new ResourceTO()
                    .setName("some-location")
                    .setType(ResourceTO.ResourceTypeTO.LOCATION)
            )
        )
        .setLayout(
            new BlockTO.LayoutTO()
                .setColor(new ColorTO().setRed(1).setGreen(2).setBlue(3))
        );
  }
}
