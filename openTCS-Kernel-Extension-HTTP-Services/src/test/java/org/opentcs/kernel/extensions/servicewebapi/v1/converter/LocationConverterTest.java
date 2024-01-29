/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

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
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyList);
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
                .setLocationRepresentation(LocationRepresentation.LOAD_TRANSFER_GENERIC.name())
        )
        .setProperties(propertyList);

    List<LocationCreationTO> result = locationConverter.toLocationCreationTOs(List.of(locationTo));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("loc1"));
    assertThat(result.get(0).getTypeName(), is("T1"));
    assertThat(result.get(0).getPosition(), is(new Triple(1, 1, 1)));
    assertThat(result.get(0).getLinks(), is(aMapWithSize(1)));
    assertThat(result.get(0).getLinks(),
               hasEntry("point1", Set.of(LocationRepresentation.LOAD_TRANSFER_GENERIC.name())));
    assertTrue(result.get(0).isLocked());
    assertThat(result.get(0).getLayout().getPosition(), is(new Couple(2, 2)));
    assertThat(result.get(0).getLayout().getLabelOffset(), is(new Couple(3, 3)));
    assertThat(result.get(0).getLayout().getLocationRepresentation(),
               is(LocationRepresentation.LOAD_TRANSFER_GENERIC));
    assertThat(result.get(0).getLayout().getLayerId(), is(4));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }

  @Test
  void checkToLocationTOs() {
    Location location = new Location("L1", new LocationType("LT1").getReference())
        .withPosition(new Triple(1, 1, 1))
        .withAttachedLinks(
            Set.of(
                new Location.Link(
                    new Location("L1", new LocationType("LT1").getReference()).getReference(),
                    new Point("P1").getReference()
                )
                    .withAllowedOperations(Set.of("alle"))
            )
        )
        .withLocked(false)
        .withLayout(
            new Location.Layout(
                new Couple(1, 1),
                new Couple(2, 2),
                LocationRepresentation.LOAD_TRANSFER_GENERIC,
                3
            )
        )
        .withProperties(propertyMap);

    List<LocationTO> result = locationConverter.toLocationTOs(Set.of(location));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("L1"));
    assertThat(result.get(0).getTypeName(), is("LT1"));
    assertThat(result.get(0).getPosition(), samePropertyValuesAs(new TripleTO(1, 1, 1)));
    assertThat(result.get(0).getLinks(), hasSize(1));
    assertThat(result.get(0).getLinks().get(0).getPointName(), is("P1"));
    assertThat(result.get(0).getLinks().get(0).getAllowedOperations(), hasSize(1));
    assertThat(result.get(0).getLinks().get(0).getAllowedOperations(), contains("alle"));
    assertFalse(result.get(0).isLocked());
    assertThat(result.get(0).getLayout().getPosition(), samePropertyValuesAs(new CoupleTO(1, 1)));
    assertThat(result.get(0).getLayout().getLabelOffset(),
               samePropertyValuesAs(new CoupleTO(2, 2)));
    assertThat(result.get(0).getLayout().getLocationRepresentation(),
               is(LocationRepresentation.LOAD_TRANSFER_GENERIC.name()));
    assertThat(result.get(0).getLayout().getLayerId(), is(3));
    assertThat(result.get(0).getProperties(), hasSize(1));
    assertThat(result.get(0).getProperties(), is(propertyList));
  }
}
