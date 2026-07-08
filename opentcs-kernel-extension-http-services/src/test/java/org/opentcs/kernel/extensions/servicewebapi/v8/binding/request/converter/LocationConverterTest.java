// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationRepresentationTO.LOAD_TRANSFER_GENERIC;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.TripleTO;

/**
 * Tests for {@link LocationConverter}.
 */
class LocationConverterTest {

  private LocationConverter locationConverter;
  private PropertyConverter propertyConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyList;

  @BeforeEach
  void setUp() {
    propertyConverter = mock();
    locationConverter = new LocationConverter(propertyConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyList = List.of(new PropertyTO("some-key", "some-value"));
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);
  }

  @Test
  void checkLocationCreationTOs() {
    LocationTO locationTo = new LocationTO("loc1", "T1", new TripleTO(1, 1, 1))
        .setLinks(
            List.of(
                new LinkTO()
                    .setPointName("point1")
                    .setAllowedOperations(
                        Set.of(LocationRepresentation.LOAD_TRANSFER_GENERIC.name())
                    )
            )
        )
        .setLocked(true)
        .setLayout(
            new LocationTO.Layout()
                .setPosition(new CoupleTO(2, 2))
                .setLabelOffset(new CoupleTO(3, 3))
                .setLayerId(4)
                .setLocationRepresentation(LOAD_TRANSFER_GENERIC)
        )
        .setProperties(propertyList);

    List<LocationCreationTO> result = locationConverter.toLocationCreationTOs(List.of(locationTo));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("loc1"));
    assertThat(result.get(0).getTypeName(), is("T1"));
    assertThat(result.get(0).getPosition(), is(new TripleCreationTO(1, 1, 1)));
    assertThat(result.get(0).getLinks(), is(aMapWithSize(1)));
    assertThat(
        result.get(0).getLinks(),
        hasEntry("point1", Set.of(LocationRepresentationTO.LOAD_TRANSFER_GENERIC.name()))
    );
    assertTrue(result.get(0).isLocked());
    assertThat(result.get(0).getLayout().getLabelOffset(), is(new CoupleCreationTO(3, 3)));
    assertThat(
        result.get(0).getLayout().getLocationRepresentation(),
        is(LocationRepresentationTO.LOAD_TRANSFER_GENERIC)
    );
    assertThat(result.get(0).getLayout().getLayerId(), is(4));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }
}
