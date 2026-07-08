// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.util.Map;
import java.util.Set;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.BlockTO;

/**
 * Tests for {@link BlockConverter}.
 */
class BlockConverterTest {

  private JsonBinder jsonBinder;
  private BlockConverter blockConverter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    blockConverter = new BlockConverter();
  }

  @Test
  void convert() {
    Block block = new Block("block-1")
        .withProperties(Map.of("key-1", "value-1"))
        .withType(Block.Type.SAME_DIRECTION_ONLY)
        .withMembers(
            Set.of(
                new Point("point-1").getReference(),
                createPath("path-1").getReference(),
                createLocation("location-1").getReference()
            )
        )
        .withLayout(new Block.Layout().withColor(new Color(1, 2, 3)));

    BlockTO result = blockConverter.convert(block);

    Approvals.verify(jsonBinder.toJson(result));
  }

  @ParameterizedTest
  @EnumSource(Block.Type.class)
  void convertsTypes(Block.Type type) {
    Block block = new Block("block-1").withType(type);

    BlockTO result = blockConverter.convert(block);

    BlockTO.TypeTO expectedType = switch (type) {
      case SINGLE_VEHICLE_ONLY -> BlockTO.TypeTO.SINGLE_VEHICLE_ONLY;
      case SAME_DIRECTION_ONLY -> BlockTO.TypeTO.SAME_DIRECTION_ONLY;
    };
    assertThat(result.getType()).isEqualTo(expectedType);
  }

  private Path createPath(String name) {
    return new Path(
        name,
        new Point("dummy-source").getReference(),
        new Point("dummy-destination").getReference()
    );
  }

  private Location createLocation(String name) {
    return new Location(name, new LocationType("dummy-type").getReference());
  }
}
