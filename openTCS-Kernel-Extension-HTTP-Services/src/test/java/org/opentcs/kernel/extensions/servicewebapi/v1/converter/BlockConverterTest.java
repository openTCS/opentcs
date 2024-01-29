/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
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
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyList);
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);
  }

  @Test
  void checkToBlockCreationTOs() {
    BlockTO blockTO = new BlockTO("block1")
        .setType(Block.Type.SINGLE_VEHICLE_ONLY.name())
        .setMemberNames(Set.of("member1"))
        .setLayout(new BlockTO.Layout())
        .setProperties(propertyList);

    List<BlockCreationTO> result = blockConverter.toBlockCreationTOs(List.of(blockTO));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("block1"));
    assertThat(result.get(0).getType(), is(Block.Type.SINGLE_VEHICLE_ONLY));
    assertThat(result.get(0).getMemberNames(), hasSize(1));
    assertThat(result.get(0).getMemberNames(), contains("member1"));
    assertThat(result.get(0).getLayout().getColor(),
               is(Colors.decodeFromHexRGB("#FF0000")));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }

  @Test
  void checkToBlockTOs() {
    Block block1 = new Block("B1")
        .withType(Block.Type.SAME_DIRECTION_ONLY)
        .withMembers(Set.of(new Point("point1").getReference()))
        .withLayout(new Block.Layout())
        .withProperties(propertyMap);

    List<BlockTO> result = blockConverter.toBlockTOs(Set.of(block1));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("B1"));
    assertThat(result.get(0).getType(), is("SAME_DIRECTION_ONLY"));
    assertThat(result.get(0).getMemberNames(), hasSize(1));
    assertThat(result.get(0).getMemberNames(), contains("point1"));
    assertThat(result.get(0).getLayout().getColor(), is(Colors.encodeToHexRGB(Color.RED)));
    assertThat(result.get(0).getProperties(), hasSize(1));
    assertThat(result.get(0).getProperties(), is(propertyList));
  }
}
