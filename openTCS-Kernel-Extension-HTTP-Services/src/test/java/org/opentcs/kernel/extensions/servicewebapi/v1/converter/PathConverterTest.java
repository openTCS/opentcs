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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;

/**
 * Tests for {@link PathConverter}.
 */
class PathConverterTest {

  private PathConverter pathConverter;
  private PropertyConverter propertyConverter;
  private PeripheralOperationConverter peripheralOpConverter;
  private EnvelopeConverter envelopeConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyList;
  private Map<String, Envelope> envelopeMap;
  private List<EnvelopeTO> envelopeList;
  private PeripheralOperationTO peripheralOperationTO;
  private List<PeripheralOperation> peripheralOperationList;
  private List<PeripheralOperationTO> peripheralOperationTOList;
  private PeripheralOperationCreationTO peripheralOperationCreationTO;

  @BeforeEach
  void setUp() {
    propertyConverter = mock();
    peripheralOpConverter = mock();
    envelopeConverter = mock();
    pathConverter = new PathConverter(propertyConverter, peripheralOpConverter, envelopeConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyList = List.of(new PropertyTO("some-key", "some-value"));
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyList);
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);

    envelopeMap = Map.of("some-envelope-key", new Envelope(List.of(new Couple(2, 2))));
    envelopeList = List.of(new EnvelopeTO("some-envelope-key", List.of(new CoupleTO(2, 2))));
    when(envelopeConverter.toEnvelopeTOs(envelopeMap)).thenReturn(envelopeList);
    when(envelopeConverter.toVehicleEnvelopeMap(envelopeList)).thenReturn(envelopeMap);

    peripheralOperationList = List.of(
        new PeripheralOperation(
            new Location("some-location",
                         new LocationType("some-location-type").getReference()).getReference(),
            "some-operation",
            PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION,
            true
        ));
    peripheralOperationTO = new PeripheralOperationTO("some-operation", "some-location")
        .setExecutionTrigger(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION.name())
        .setCompletionRequired(true);
    peripheralOperationTOList = List.of(peripheralOperationTO);
    peripheralOperationCreationTO
        = new PeripheralOperationCreationTO("some-operation", "some-location")
            .withExecutionTrigger(PeripheralOperation.ExecutionTrigger.AFTER_ALLOCATION)
            .withCompletionRequired(true);
    when(peripheralOpConverter.toPeripheralOperationsTOs(peripheralOperationList))
        .thenReturn(List.of(peripheralOperationTO));
    when(peripheralOpConverter.toPeripheralOperationCreationTOs(peripheralOperationTOList))
        .thenReturn(List.of(peripheralOperationCreationTO));
  }

  @Test
  void checkToPathTOs() {
    Path path1 = new Path("Path1", new Point("p1").getReference(), new Point("p2").getReference())
        .withLength(3)
        .withMaxVelocity(6)
        .withMaxReverseVelocity(6)
        .withPeripheralOperations(peripheralOperationList)
        .withLocked(true)
        .withVehicleEnvelopes(envelopeMap)
        .withLayout(
            new Path.Layout(Path.Layout.ConnectionType.POLYPATH,
                            List.of(new Couple(2, 2)),
                            4)
        )
        .withProperties(propertyMap);

    List<PathTO> result = pathConverter.toPathTOs(Set.of(path1));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("Path1"));
    assertThat(result.get(0).getSrcPointName(), is("p1"));
    assertThat(result.get(0).getDestPointName(), is("p2"));
    assertThat(result.get(0).getLength(), is(3L));
    assertThat(result.get(0).getMaxVelocity(), is(6));
    assertThat(result.get(0).getMaxReverseVelocity(), is(6));
    assertThat(result.get(0).getPeripheralOperations(), hasSize(1));
    assertThat(result.get(0).getPeripheralOperations(), contains(peripheralOperationTO));
    assertTrue(result.get(0).isLocked());
    assertThat(result.get(0).getVehicleEnvelopes(), hasSize(1));
    assertThat(result.get(0).getVehicleEnvelopes(), is(envelopeList));
    assertThat(result.get(0).getLayout().getLayerId(), is(4));
    assertThat(result.get(0).getLayout().getConnectionType(),
               is(Path.Layout.ConnectionType.POLYPATH.name()));
    assertThat(result.get(0).getLayout().getControlPoints(), hasSize(1));
    assertThat(result.get(0).getLayout().getControlPoints().get(0),
               samePropertyValuesAs(new CoupleTO(2, 2)));
    assertThat(result.get(0).getProperties(), hasSize(1));
    assertThat(result.get(0).getProperties(), is(propertyList));
  }

  @Test
  void checkToPathCreationTOs() {
    PathTO path1 = new PathTO("Path1", "srcP", "desP")
        .setLength(3)
        .setMaxVelocity(6)
        .setMaxReverseVelocity(6)
        .setPeripheralOperations(peripheralOperationTOList)
        .setLocked(true)
        .setVehicleEnvelopes(envelopeList)
        .setLayout(
            new PathTO.Layout()
                .setConnectionType(Path.Layout.ConnectionType.POLYPATH.name())
                .setControlPoints(List.of(new CoupleTO(1, 1)))
                .setLayerId(4)
        )
        .setProperties(propertyList);

    List<PathCreationTO> result = pathConverter.toPathCreationTOs(List.of(path1));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("Path1"));
    assertThat(result.get(0).getSrcPointName(), is("srcP"));
    assertThat(result.get(0).getDestPointName(), is("desP"));
    assertThat(result.get(0).getLength(), is(3L));
    assertThat(result.get(0).getMaxVelocity(), is(6));
    assertThat(result.get(0).getMaxReverseVelocity(), is(6));
    assertThat(result.get(0).getPeripheralOperations(), hasSize(1));
    assertThat(result.get(0).getPeripheralOperations(), contains(peripheralOperationCreationTO));
    assertTrue(result.get(0).isLocked());
    assertThat(result.get(0).getVehicleEnvelopes(), is(aMapWithSize(1)));
    assertThat(result.get(0).getVehicleEnvelopes(), is(envelopeMap));
    assertThat(result.get(0).getLayout().getLayerId(), is(4));
    assertThat(result.get(0).getLayout().getConnectionType(),
               is(Path.Layout.ConnectionType.POLYPATH));
    assertThat(result.get(0).getLayout().getControlPoints(), hasSize(1));
    assertThat(result.get(0).getLayout().getControlPoints().get(0),
               samePropertyValuesAs(new Couple(1, 1)));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }
}
