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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;

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
  void checkToPropertyTos() {
    Map<String, String> property = Map.of("P1", "1");

    List<PropertyTO> result = propertyConverter.toPropertyTOs(property);

    assertThat(result, hasSize(1));
    assertThat(result.get(0), samePropertyValuesAs(new PropertyTO("P1", "1")));
  }

  @Test
  void checkToPropertyMap() {
    PropertyTO propTo = new PropertyTO("P1", "1");

    Map<String, String> result = propertyConverter.toPropertyMap(List.of(propTo));

    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasEntry("P1", "1"));
  }
}
