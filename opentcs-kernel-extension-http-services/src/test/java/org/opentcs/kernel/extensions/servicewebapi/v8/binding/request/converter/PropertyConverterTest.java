// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

/**
 * Tests for {@link PropertyConverter}.
 */
class PropertyConverterTest {

  private PropertyConverter propertyConverter;

  @BeforeEach
  void setUp() {
    propertyConverter = new PropertyConverter();
  }

  @Test
  void checkToProperties() {
    Map<String, String> property = Map.of("P1", "1");

    List<Property> result = propertyConverter.toProperties(property);

    assertThat(result, hasSize(1));
    assertThat(result.getFirst(), samePropertyValuesAs(new Property("P1", "1")));
  }

  @Test
  void checkToPropertyMap() {
    PropertyTO propTo = new PropertyTO("P1", "1");

    Map<String, String> result = propertyConverter.toPropertyMap(List.of(propTo));

    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasEntry("P1", "1"));
  }
}
