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
 * Tests for {@link CoordinateSystemTransformation}.
 */
@Deprecated
class CoordinateSystemTransformationTest {

  @Test
  public void parseValidPropertyValues() {
    Vehicle vehicle = new Vehicle("v1")
        .withProperties(
            Map.of(
                "tcs:offsetTransformer.x", "10",
                "tcs:offsetTransformer.y", "20",
                "tcs:offsetTransformer.z", "30",
                "tcs:offsetTransformer.orientation", "40"
            )
        );
    Optional<CoordinateSystemTransformation> transformation
        = CoordinateSystemTransformation.fromVehicle(vehicle);

    assertTrue(transformation.isPresent());
    assertThat(transformation.get().getOffsetX(), equalTo(10));
    assertThat(transformation.get().getOffsetY(), equalTo(20));
    assertThat(transformation.get().getOffsetZ(), equalTo(30));
    assertThat(transformation.get().getOffsetOrientation(), equalTo(40.0));
  }

  @Test
  public void ignoreMissingPropertyValues() {
    Vehicle vehicle = new Vehicle("v1").withProperties(Map.of());
    Optional<CoordinateSystemTransformation> transformation
        = CoordinateSystemTransformation.fromVehicle(vehicle);

    assertTrue(transformation.isPresent());
    assertThat(transformation.get().getOffsetX(), equalTo(0));
    assertThat(transformation.get().getOffsetY(), equalTo(0));
    assertThat(transformation.get().getOffsetZ(), equalTo(0));
    assertThat(transformation.get().getOffsetOrientation(), equalTo(0.0));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
          "tcs:offsetTransformer.x",
          "tcs:offsetTransformer.y",
          "tcs:offsetTransformer.z",
          "tcs:offsetTransformer.orientation",
      }
  )
  void returnEmptyOptionalOnInvalidPropertyValue(String propertyKey) {
    Vehicle vehicle = new Vehicle("v1")
        .withProperties(Map.of(propertyKey, "none"));
    Optional<CoordinateSystemTransformation> transformation
        = CoordinateSystemTransformation.fromVehicle(vehicle);

    assertFalse(transformation.isPresent());
  }
}
