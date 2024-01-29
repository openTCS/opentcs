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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;

/**
 * Tests for {@link LocationTypeConverter}.
 */
class LocationTypeConverterTest {

  private LocationTypeConverter locationTypeConverter;
  private PropertyConverter propertyConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyList;

  @BeforeEach
  void setUp() {
    propertyConverter = mock();
    locationTypeConverter = new LocationTypeConverter(propertyConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyList = List.of(new PropertyTO("some-key", "some-value"));
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyList);
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);
  }

  @Test
  void checkToLocationTypeCreationTOs() {
    LocationTypeTO locTypeTo = new LocationTypeTO("LT1")
        .setAllowedOperations(List.of("O1"))
        .setAllowedPeripheralOperations(List.of("PO1"))
        .setLayout(
            new LocationTypeTO.Layout()
                .setLocationRepresentation(LocationRepresentation.RECHARGE_ALT_1.name())
        )
        .setProperties(propertyList);

    List<LocationTypeCreationTO> result
        = locationTypeConverter.toLocationTypeCreationTOs(List.of(locTypeTo));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("LT1"));
    assertThat(result.get(0).getAllowedOperations(), hasSize(1));
    assertThat(result.get(0).getAllowedOperations(), contains("O1"));
    assertThat(result.get(0).getAllowedPeripheralOperations(), hasSize(1));
    assertThat(result.get(0).getAllowedPeripheralOperations(), contains("PO1"));
    assertThat(result.get(0).getLayout().getLocationRepresentation(),
               is(LocationRepresentation.RECHARGE_ALT_1));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }

  @Test
  void checkToLocationTypeTOs() {
    LocationType locType = new LocationType("LT1")
        .withAllowedOperations(List.of("O1"))
        .withAllowedPeripheralOperations(List.of("PO1"))
        .withLayout(
            new LocationType.Layout()
                .withLocationRepresentation(LocationRepresentation.LOAD_TRANSFER_GENERIC)
        )
        .withProperties(propertyMap);

    List<LocationTypeTO> result = locationTypeConverter.toLocationTypeTOs(Set.of(locType));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("LT1"));
    assertThat(result.get(0).getAllowedOperations(), hasSize(1));
    assertThat(result.get(0).getAllowedOperations(), contains("O1"));
    assertThat(result.get(0).getAllowedPeripheralOperations(), hasSize(1));
    assertThat(result.get(0).getAllowedPeripheralOperations(), contains("PO1"));
    assertThat(result.get(0).getLayout().getLocationRepresentation(),
               is(LocationRepresentation.LOAD_TRANSFER_GENERIC.name()));
    assertThat(result.get(0).getProperties(), hasSize(1));
    assertThat(result.get(0).getProperties(), is(propertyList));
  }
}
