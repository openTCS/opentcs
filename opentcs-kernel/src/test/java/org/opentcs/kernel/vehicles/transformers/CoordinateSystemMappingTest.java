// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link CoordinateSystemMapping}.
 */
class CoordinateSystemMappingTest {

  @Test
  public void parseValidPropertyValues() {
    Vehicle vehicle = new Vehicle("v1")
        .withProperties(
            Map.of(
                CoordinateSystemMapping.PROPKEY_TRANSLATION_X, "10",
                CoordinateSystemMapping.PROPKEY_TRANSLATION_Y, "20",
                CoordinateSystemMapping.PROPKEY_TRANSLATION_Z, "30",
                CoordinateSystemMapping.PROPKEY_ROTATION_Z, "40"
            )
        );

    Optional<CoordinateSystemMapping> mapping = CoordinateSystemMapping.fromVehicle(vehicle);

    assertTrue(mapping.isPresent());
    assertThat(mapping.get().getTranslationX(), equalTo(10));
    assertThat(mapping.get().getTranslationY(), equalTo(20));
    assertThat(mapping.get().getTranslationZ(), equalTo(30));
    assertThat(mapping.get().getRotationZ(), equalTo(40.0));
  }

  @Test
  public void defaultForMissingPropertyValues() {
    Vehicle vehicle = new Vehicle("v1").withProperties(Map.of());

    Optional<CoordinateSystemMapping> mapping = CoordinateSystemMapping.fromVehicle(vehicle);

    assertTrue(mapping.isPresent());
    assertThat(mapping.get().getTranslationX(), equalTo(0));
    assertThat(mapping.get().getTranslationY(), equalTo(0));
    assertThat(mapping.get().getTranslationZ(), equalTo(0));
    assertThat(mapping.get().getRotationZ(), equalTo(0.0));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
          CoordinateSystemMapping.PROPKEY_TRANSLATION_X,
          CoordinateSystemMapping.PROPKEY_TRANSLATION_Y,
          CoordinateSystemMapping.PROPKEY_TRANSLATION_Z,
          CoordinateSystemMapping.PROPKEY_ROTATION_Z
      }
  )
  void returnEmptyOptionalOnInvalidPropertyValue(String propertyKey) {
    Vehicle vehicle = new Vehicle("v1")
        .withProperties(Map.of(propertyKey, "invalid-value"));

    Optional<CoordinateSystemMapping> mapping = CoordinateSystemMapping.fromVehicle(vehicle);

    assertFalse(mapping.isPresent());
  }
}
