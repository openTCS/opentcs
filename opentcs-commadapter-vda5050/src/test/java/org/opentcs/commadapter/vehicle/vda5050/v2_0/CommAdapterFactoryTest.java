// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link CommAdapterFactory}.
 */
class CommAdapterFactoryTest {

  private CommAdapterImpl commAdapter;
  private CommAdapterComponentsFactory componentsFactory;
  private CommAdapterFactory commAdapterFactory;

  @BeforeEach
  void setUp() {
    commAdapter = mock(CommAdapterImpl.class);

    componentsFactory = mock(CommAdapterComponentsFactory.class);
    when(componentsFactory.createCommAdapterImpl(any(), any(), any())).thenReturn(commAdapter);

    commAdapterFactory = new CommAdapterFactory(
        new VehicleHasRequiredProperties(),
        componentsFactory
    );
  }

  @Test
  void provideAdapterForVehicleWithAllProperties() {
    assertThat(
        commAdapterFactory.getAdapterFor(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "2.0")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    )
        .isNotNull();
  }

  @Test
  void refuseForVehicleMissingProperties() {
    assertThat(
        commAdapterFactory.getAdapterFor(
            new Vehicle("Some vehicle")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_VERSION, "2.0")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
                .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
        )
    )
        .isNull();
  }
}
