/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.util.Colors;

/**
 * Tests for {@link VehicleConverter}.
 */
class VehicleConverterTest {

  private VehicleConverter vehicleConverter;
  private PropertyConverter propertyConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyList;

  @BeforeEach
  void setUp() {
    propertyConverter = mock();
    vehicleConverter = new VehicleConverter(propertyConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyList = List.of(new PropertyTO("some-key", "some-value"));
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyList);
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);
  }

  @Test
  void checkToVehicleCreationTOs() {
    VehicleTO vehicleTo = new VehicleTO("V1")
        .setLength(1000)
        .setEnergyLevelGood(90)
        .setEnergyLevelCritical(30)
        .setEnergyLevelFullyRecharged(90)
        .setEnergyLevelSufficientlyRecharged(30)
        .setMaxVelocity(1000)
        .setMaxReverseVelocity(1000)
        .setLayout(new VehicleTO.Layout())
        .setProperties(propertyList);

    List<VehicleCreationTO> result = vehicleConverter.toVehicleCreationTOs(List.of(vehicleTo));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("V1"));
    assertThat(result.get(0).getLength(), is(1000));
    assertThat(result.get(0).getEnergyLevelGood(), is(90));
    assertThat(result.get(0).getEnergyLevelCritical(), is(30));
    assertThat(result.get(0).getEnergyLevelFullyRecharged(), is(90));
    assertThat(result.get(0).getEnergyLevelSufficientlyRecharged(), is(30));
    assertThat(result.get(0).getMaxVelocity(), is(1000));
    assertThat(result.get(0).getMaxReverseVelocity(), is(1000));
    assertThat(result.get(0).getLayout().getRouteColor(), is(Colors.decodeFromHexRGB("#00FF00")));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }

  @Test
  void checkToVehicleTOs() {
    Vehicle vehicle = new Vehicle("V1")
        .withLength(1000)
        .withEnergyLevelGood(90)
        .withEnergyLevelCritical(30)
        .withEnergyLevelFullyRecharged(90)
        .withEnergyLevelSufficientlyRecharged(30)
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(1000)
        .withLayout(new Vehicle.Layout())
        .withProperties(propertyMap);

    List<VehicleTO> result = vehicleConverter.toVehicleTOs(Set.of(vehicle));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("V1"));
    assertThat(result.get(0).getLength(), is(1000));
    assertThat(result.get(0).getEnergyLevelGood(), is(90));
    assertThat(result.get(0).getEnergyLevelCritical(), is(30));
    assertThat(result.get(0).getEnergyLevelFullyRecharged(), is(90));
    assertThat(result.get(0).getEnergyLevelSufficientlyRecharged(), is(30));
    assertThat(result.get(0).getMaxVelocity(), is(1000));
    assertThat(result.get(0).getMaxReverseVelocity(), is(1000));
    assertThat(result.get(0).getLayout().getRouteColor(), is(Colors.encodeToHexRGB(Color.RED)));
    assertThat(result.get(0).getProperties(), hasSize(1));
    assertThat(result.get(0).getProperties(), is(propertyList));
  }
}
