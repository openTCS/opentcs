/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Tests for {@link LocationHandler}.
 */
class LocationHandlerTest {

  private PlantModelService plantModelService;
  private KernelExecutorWrapper executorWrapper;

  private LocationHandler handler;

  private Location location;

  @BeforeEach
  void setUp() {
    plantModelService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new LocationHandler(plantModelService, executorWrapper);

    location = new Location("some-location",
                            new LocationType("some-location-type").getReference());
    given(plantModelService.fetchObject(Location.class, "some-location"))
        .willReturn(location);
  }

  @Test
  void lockLocation() {
    handler.updateLocationLock("some-location", "true");

    then(plantModelService).should().updateLocationLock(location.getReference(), true);
  }

  @ParameterizedTest
  @ValueSource(strings = {"false", "flase", "some-value-that-is-not-true"})
  void unlockLocationOnAnyNontrueValue(String value) {
    handler.updateLocationLock("some-location", value);

    then(plantModelService).should().updateLocationLock(location.getReference(), false);
  }

  @ParameterizedTest
  @ValueSource(strings = {"true", "false"})
  void throwOnLockUnknownLocation(String value) {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.updateLocationLock("some-unknown-location", value));
  }
}
