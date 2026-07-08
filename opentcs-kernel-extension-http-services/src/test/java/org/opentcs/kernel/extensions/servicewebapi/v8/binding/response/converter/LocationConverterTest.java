// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.kernel.extensions.servicewebapi.common.JsonBinder;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LocationRepresentationTO;

/**
 * Tests for {@link LocationConverter}.
 */
class LocationConverterTest {

  private JsonBinder jsonBinder;
  private LocationConverter locationConverter;

  @BeforeEach
  void setUp() {
    jsonBinder = new JsonBinder();
    locationConverter = new LocationConverter();
  }

  @Test
  void convert() {
    Location location = new Location(
        "location-1", new LocationType("location-type-1").getReference()
    );
    location = location
        .withProperties(Map.of("key-1", "value-1"))
        .withPosition(new Triple(1, 2, 3))
        .withAttachedLinks(
            Set.of(
                new Location.Link(
                    location.getReference(),
                    new Point("point-1").getReference()
                )
                    .withAllowedOperations(Set.of("operation-1"))
            )
        )
        .withLocked(true)
        .withLayout(
            new Location.Layout(
                new Couple(4, 5),
                LocationRepresentation.LOAD_TRANSFER_GENERIC,
                6
            )
        );

    LocationTO result = locationConverter.convert(location);

    Approvals.verify(jsonBinder.toJson(result));
  }

  @ParameterizedTest
  @EnumSource(PeripheralInformation.State.class)
  void convertsPeripheralStates(PeripheralInformation.State state) {
    Location location = new Location(
        "location-1", new LocationType("location-type-1").getReference()
    )
        .withPeripheralInformation(new PeripheralInformation().withState(state));

    LocationTO result = locationConverter.convert(location);

    LocationTO.PeripheralInformationTO.StateTO expectedState = switch (state) {
      case NO_PERIPHERAL -> LocationTO.PeripheralInformationTO.StateTO.NO_PERIPHERAL;
      case UNKNOWN -> LocationTO.PeripheralInformationTO.StateTO.UNKNOWN;
      case UNAVAILABLE -> LocationTO.PeripheralInformationTO.StateTO.UNAVAILABLE;
      case ERROR -> LocationTO.PeripheralInformationTO.StateTO.ERROR;
      case IDLE -> LocationTO.PeripheralInformationTO.StateTO.IDLE;
      case EXECUTING -> LocationTO.PeripheralInformationTO.StateTO.EXECUTING;
    };
    assertThat(result.getPeripheralInformation().getState()).isEqualTo(expectedState);
  }

  @ParameterizedTest
  @EnumSource(PeripheralInformation.ProcState.class)
  void convertsPeripheralProcStates(PeripheralInformation.ProcState state) {
    Location location = new Location(
        "location-1", new LocationType("location-type-1").getReference()
    )
        .withPeripheralInformation(new PeripheralInformation().withProcState(state));

    LocationTO result = locationConverter.convert(location);

    LocationTO.PeripheralInformationTO.ProcStateTO expectedState = switch (state) {
      case IDLE -> LocationTO.PeripheralInformationTO.ProcStateTO.IDLE;
      case PROCESSING_JOB -> LocationTO.PeripheralInformationTO.ProcStateTO.PROCESSING_JOB;
    };
    assertThat(result.getPeripheralInformation().getProcState()).isEqualTo(expectedState);
  }

  @ParameterizedTest
  @EnumSource(LocationRepresentation.class)
  void convertsLocationRepresentations(LocationRepresentation locationRepresentation) {
    Location location = new Location(
        "location-1", new LocationType("location-type-1").getReference()
    )
        .withLayout(
            new Location.Layout()
                .withLocationRepresentation(locationRepresentation)
        );

    LocationTO result = locationConverter.convert(location);

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
