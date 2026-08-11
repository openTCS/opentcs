// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.BlockTO;
import org.opentcs.util.Colors;

/**
 * Tests for {@link BlockConverter}.
 */
class BlockConverterTest {

  private BlockConverter blockConverter;

  private Map<String, String> propertyMap;

  @BeforeEach
  void setUp() {
    blockConverter = new BlockConverter();

    propertyMap = Map.of("some-key", "some-value");
  }

  @Test
  void checkToBlockCreationTOs() {
    BlockTO blockTO = new BlockTO("block1")
        .setType(BlockTO.Type.SINGLE_VEHICLE_ONLY)
        .setMemberNames(Set.of("member1"))
        .setLayout(new BlockTO.Layout())
        .setProperties(propertyMap);

    List<BlockCreationTO> result = blockConverter.toBlockCreationTOs(List.of(blockTO));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("block1"));
    assertThat(result.get(0).getType(), is(BlockCreationTO.Type.SINGLE_VEHICLE_ONLY));
    assertThat(result.get(0).getMemberNames(), hasSize(1));
    assertThat(result.get(0).getMemberNames(), contains("member1"));
    assertThat(
        result.get(0).getLayout().getColor(),
        is(Colors.decodeFromHexRGB("#FF0000"))
    );
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }
}
