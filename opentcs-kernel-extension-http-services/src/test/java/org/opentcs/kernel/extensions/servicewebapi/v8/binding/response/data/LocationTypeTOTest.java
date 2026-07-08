// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LocationRepresentationTO;

/**
 * Tests for {@link LocationTypeTO}.
 */
class LocationTypeTOTest {
  private JsonBinder jsonBinder;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
  }

  @Test
  void jsonSampleMinimal() {
    Approvals.verify(jsonBinder.toJson(createLocationTypeMinimal()));
  }

  @Test
  void jsonSampleFull() {
    Approvals.verify(jsonBinder.toJson(createLocationTypeFull()));
  }

  private LocationTypeTO createLocationTypeMinimal() {
    return new LocationTypeTO()
        .setName("some-location")
        .setProperties(Map.of())
        .setAllowedOperations(List.of())
        .setAllowedPeripheralOperations(List.of())
        .setLayout(
            new LocationTypeTO.LayoutTO()
                .setLocationRepresentation(LocationRepresentationTO.DEFAULT)
        );
  }

  private LocationTypeTO createLocationTypeFull() {
    return new LocationTypeTO()
        .setName("some-location")
        .setProperties(
            Map.of(
                "some-key", "some-value",
                "some-other-key", "some-other-value"
            )
        )
        .setAllowedOperations(List.of("some-vehicle-operation"))
        .setAllowedPeripheralOperations(List.of("some-peripheral-operation"))
        .setLayout(
            new LocationTypeTO.LayoutTO()
                .setLocationRepresentation(LocationRepresentationTO.DEFAULT)
        );
  }
}
