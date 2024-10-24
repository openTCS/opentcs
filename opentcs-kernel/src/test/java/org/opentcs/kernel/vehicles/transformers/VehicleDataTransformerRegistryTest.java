// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleDataTransformerFactory;

/**
 * Tests for {@link VehicleDataTransformerRegistry}.
 */
public class VehicleDataTransformerRegistryTest {

  private VehicleDataTransformerRegistry vehicleDataTransformerRegistry;
  private VehicleDataTransformerFactory dummyFactory;
  private Vehicle vehicle;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle("v1")
        .withProperties(
            Map.of(
                "tcs:vehicleDataTransformer", "dummyFactory"
            )
        );
    dummyFactory = mock(VehicleDataTransformerFactory.class);
    vehicleDataTransformerRegistry = new VehicleDataTransformerRegistry(
        Set.of(new DefaultVehicleDataTransformerFactory(), dummyFactory)
    );
    when(dummyFactory.getName()).thenReturn("dummyFactory");
  }

  @Test
  void fallBackToDefaultFactoryForInvalidFactoryName() {
    vehicle = vehicle.withProperties(
        Map.of(
            "tcs:vehicleDataTransformer", "newFactory"
        )
    );

    assertThat(
        vehicleDataTransformerRegistry.findFactoryFor(vehicle),
        is(instanceOf(DefaultVehicleDataTransformerFactory.class))
    );
  }

  @Test
  void fallBackToDefaultFactoryForMissingProperties() {
    vehicle = vehicle.withProperties(Map.of());
    assertThat(
        vehicleDataTransformerRegistry.findFactoryFor(vehicle),
        is(instanceOf(DefaultVehicleDataTransformerFactory.class))
    );
  }

  @Test
  void provideAcceptableFactoryForVehicle() {
    when(dummyFactory.providesTransformersFor(vehicle)).thenReturn(true);
    assertThat(vehicleDataTransformerRegistry.findFactoryFor(vehicle), is(dummyFactory));
  }

  @Test
  void fallBackToDefaultFactoryForUnacceptableSelectedFactory() {
    when(dummyFactory.providesTransformersFor(vehicle)).thenReturn(false);
    assertThat(vehicleDataTransformerRegistry.findFactoryFor(vehicle).getClass(),
               is(DefaultVehicleDataTransformerFactory.class));
  }
}
