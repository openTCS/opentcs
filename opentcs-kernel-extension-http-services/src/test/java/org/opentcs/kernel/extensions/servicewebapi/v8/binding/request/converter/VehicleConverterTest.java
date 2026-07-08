// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.AcceptableOrderTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.Property;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;
import org.opentcs.util.Colors;

/**
 * Tests for {@link VehicleConverter}.
 */
class VehicleConverterTest {

  private VehicleConverter vehicleConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyTOList;
  private List<Property> propertyList;

  @BeforeEach
  void setUp() {
    PropertyConverter propertyConverter = mock();
    vehicleConverter = new VehicleConverter(propertyConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyTOList = List.of(new PropertyTO("some-key", "some-value"));
    propertyList = List.of(new Property("some-key", "some-value"));
    Set<AcceptableOrderType> acceptableOrderTypes = Set.of(new AcceptableOrderType("order-1", 0));
    List<AcceptableOrderTypeTO> acceptableOrderTypeList = List.of(
        new AcceptableOrderTypeTO("order-1", 0)
    );
    when(propertyConverter.toProperties(propertyMap)).thenReturn(propertyList);
    when(propertyConverter.toPropertyMap(propertyTOList)).thenReturn(propertyMap);
  }

  @Test
  void checkToVehicleCreationTOs() {
    VehicleTO vehicleTo = new VehicleTO("V1")
        .setBoundingBox(new BoundingBoxTO(500, 100, 700, new CoupleTO(0, 0)))
        .setEnergyLevelGood(90)
        .setEnergyLevelCritical(30)
        .setEnergyLevelFullyRecharged(90)
        .setEnergyLevelSufficientlyRecharged(30)
        .setMaxVelocity(1000)
        .setMaxReverseVelocity(1000)
        .setLayout(new VehicleTO.Layout())
        .setProperties(propertyTOList);

    List<VehicleCreationTO> result = vehicleConverter.toVehicleCreationTOs(List.of(vehicleTo));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("V1"));
    assertThat(result.get(0).getBoundingBox().getLength(), is(500L));
    assertThat(result.get(0).getBoundingBox().getWidth(), is(100L));
    assertThat(result.get(0).getBoundingBox().getHeight(), is(700L));
    assertThat(result.get(0).getBoundingBox().getReferenceOffset().getX(), is(0L));
    assertThat(result.get(0).getBoundingBox().getReferenceOffset().getY(), is(0L));
    assertThat(result.get(0).getEnergyLevelThresholdSet().getEnergyLevelGood(), is(90));
    assertThat(result.get(0).getEnergyLevelThresholdSet().getEnergyLevelCritical(), is(30));
    assertThat(result.get(0).getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged(), is(90));
    assertThat(
        result.get(0).getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged(), is(30)
    );
    assertThat(result.get(0).getMaxVelocity(), is(1000));
    assertThat(result.get(0).getMaxReverseVelocity(), is(1000));
    assertThat(result.get(0).getLayout().getRouteColor(), is(Colors.decodeFromHexRGB("#00FF00")));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }
}
