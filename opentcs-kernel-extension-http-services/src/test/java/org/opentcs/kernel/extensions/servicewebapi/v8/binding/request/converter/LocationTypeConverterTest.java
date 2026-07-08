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
import static org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationRepresentationTO.RECHARGE_ALT_1;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

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
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);
  }

  @Test
  void checkToLocationTypeCreationTOs() {
    LocationTypeTO locTypeTo = new LocationTypeTO("LT1")
        .setAllowedOperations(List.of("O1"))
        .setAllowedPeripheralOperations(List.of("PO1"))
        .setLayout(
            new LocationTypeTO.Layout()
                .setLocationRepresentation(RECHARGE_ALT_1)
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
    assertThat(
        result.get(0).getLayout().getLocationRepresentation(),
        is(LocationRepresentationTO.RECHARGE_ALT_1)
    );
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }
}
