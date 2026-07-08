// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LocationRepresentationTO;

/**
 * Tests for {@link LocationTypeConverter}.
 */
class LocationTypeConverterTest {


  private JsonBinder jsonBinder;
  private LocationTypeConverter locationTypeConverter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    locationTypeConverter = new LocationTypeConverter();
  }

  @Test
  void convert() {
    LocationType locationType = new LocationType("location-type-1")
        .withProperties(Map.of("key-1", "value-1"))
        .withAllowedOperations(List.of("operation-1"))
        .withAllowedPeripheralOperations(List.of("peripheral-operation-1"))
        .withLayout(
            new LocationType.Layout()
                .withLocationRepresentation(LocationRepresentation.LOAD_TRANSFER_GENERIC)
        );

    LocationTypeTO result = locationTypeConverter.convert(locationType);

    Approvals.verify(jsonBinder.toJson(result));
  }


  @ParameterizedTest
  @EnumSource(LocationRepresentation.class)
  void convertsLocationRepresentations(LocationRepresentation locationRepresentation) {
    LocationType locationType = new LocationType("location-type-1")
        .withLayout(new LocationType.Layout().withLocationRepresentation(locationRepresentation));

    LocationTypeTO result = locationTypeConverter.convert(locationType);

    LocationRepresentationTO expectedLocationRepresentation = switch (locationRepresentation) {
      case NONE -> LocationRepresentationTO.NONE;
      case DEFAULT -> LocationRepresentationTO.DEFAULT;
      case LOAD_TRANSFER_GENERIC -> LocationRepresentationTO.LOAD_TRANSFER_GENERIC;
      case LOAD_TRANSFER_ALT_1 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_5;
      case WORKING_GENERIC -> LocationRepresentationTO.WORKING_GENERIC;
      case WORKING_ALT_1 -> LocationRepresentationTO.WORKING_ALT_1;
      case WORKING_ALT_2 -> LocationRepresentationTO.WORKING_ALT_2;
      case RECHARGE_GENERIC -> LocationRepresentationTO.RECHARGE_GENERIC;
      case RECHARGE_ALT_1 -> LocationRepresentationTO.RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> LocationRepresentationTO.RECHARGE_ALT_2;
    };
    assertThat(result.getLayout().getLocationRepresentation())
        .isEqualTo(expectedLocationRepresentation);
  }
}
