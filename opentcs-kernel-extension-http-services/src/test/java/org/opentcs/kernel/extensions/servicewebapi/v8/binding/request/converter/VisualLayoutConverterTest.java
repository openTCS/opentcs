// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.LayerCreationTO;
import org.opentcs.access.to.model.LayerGroupCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LayerGroupTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LayerTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.VisualLayoutTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

/**
 * Tests for {@link VisualLayoutConverter}.
 */
class VisualLayoutConverterTest {

  private VisualLayoutConverter visualLayoutConverter;
  private PropertyConverter propertyConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyList;

  @BeforeEach
  void setUp() {
    propertyConverter = mock();
    visualLayoutConverter = new VisualLayoutConverter(propertyConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyList = List.of(new PropertyTO("some-key", "some-value"));
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);
  }

  @Test
  void checkToVisualLayoutCreationTO() {
    VisualLayoutTO vLayout = new VisualLayoutTO("V1")
        .setScaleX(50.0)
        .setScaleY(50.0)
        .setLayers(List.of(new LayerTO(1, 2, true, "L1", 3)))
        .setLayerGroups(List.of(new LayerGroupTO(1, "Lg1", true)))
        .setProperties(propertyList);

    VisualLayoutCreationTO result = visualLayoutConverter.toVisualLayoutCreationTO(vLayout);

    assertThat(result.getName(), is("V1"));
    assertThat(result.getScaleX(), is(50.0));
    assertThat(result.getScaleY(), is(50.0));
    assertThat(result.getLayers(), hasSize(1));
    assertThat(
        result.getLayers().get(0),
        samePropertyValuesAs(new LayerCreationTO(1, 2, true, "L1", 3))
    );
    assertThat(result.getLayerGroups(), hasSize(1));
    assertThat(
        result.getLayerGroups().get(0),
        samePropertyValuesAs(new LayerGroupCreationTO(1, "Lg1", true))
    );
    assertThat(result.getProperties(), is(aMapWithSize(1)));
    assertThat(result.getProperties(), is(propertyMap));
  }
}
