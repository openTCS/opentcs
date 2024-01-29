/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerGroupTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VisualLayoutTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;

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
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyList);
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
    assertThat(result.getLayers().get(0), samePropertyValuesAs(new Layer(1, 2, true, "L1", 3)));
    assertThat(result.getLayerGroups(), hasSize(1));
    assertThat(result.getLayerGroups().get(0),
               samePropertyValuesAs(new LayerGroup(1, "Lg1", true)));
    assertThat(result.getProperties(), is(aMapWithSize(1)));
    assertThat(result.getProperties(), is(propertyMap));
  }
  
  @Test
  void checkVisualLayoutTO() {
    VisualLayout vLayout = new VisualLayout("V1")
        .withScaleX(50.0)
        .withScaleY(50.0)
        .withLayers(List.of(new Layer(1, 2, true, "L1", 3)))
        .withLayerGroups(List.of(new LayerGroup(3, "G1", true)))
        .withProperties(propertyMap);
    
    VisualLayoutTO result = visualLayoutConverter.toVisualLayoutTO(Set.of(vLayout));
    
    assertThat(result.getName(), is("V1"));
    assertThat(result.getScaleX(), is(50.00));
    assertThat(result.getScaleY(), is(50.00));
    assertThat(result.getLayers(), hasSize(1));
    assertThat(result.getLayers().get(0), samePropertyValuesAs(new LayerTO(1, 2, true, "L1", 3)));
    assertThat(result.getLayerGroups(), hasSize(1));
    assertThat(result.getLayerGroups().get(0),
               samePropertyValuesAs(new LayerGroupTO(3, "G1", true)));
    assertThat(result.getProperties(), is(propertyList));
  }
  
  @Test
  void checkToVisualLayoutTOShouldCreateDefault() {
    VisualLayoutTO result = visualLayoutConverter.toVisualLayoutTO(Set.of());
    
    assertThat(result.getName(), is("default visual layout"));
  }
}
