// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;
import org.opentcs.util.Colors;

/**
 * Tests for {@link BlockConverter}.
 */
class BlockConverterTest {

  private BlockConverter blockConverter;
  private PropertyConverter propertyConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyList;

  @BeforeEach
  void setUp() {
    propertyConverter = mock();
    blockConverter = new BlockConverter(propertyConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyList = List.of(new PropertyTO("some-key", "some-value"));
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);
  }

  @Test
  void checkToBlockCreationTOs() {
    BlockTO blockTO = new BlockTO("block1")
        .setType(BlockTO.Type.SINGLE_VEHICLE_ONLY)
        .setMemberNames(Set.of("member1"))
        .setLayout(new BlockTO.Layout())
        .setProperties(propertyList);

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
