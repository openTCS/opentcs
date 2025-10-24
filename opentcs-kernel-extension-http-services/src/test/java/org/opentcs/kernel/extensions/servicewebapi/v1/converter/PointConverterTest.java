// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 * Tests for {@link PointConverter}.
 */
class PointConverterTest {

  private PointConverter pointConverter;
  private PropertyConverter propertyConverter;
  private EnvelopeConverter envelopeConverter;

  private Map<String, String> propertyMap;
  private List<PropertyTO> propertyList;
  private Map<String, Envelope> envelopeMap;
  private Map<String, EnvelopeCreationTO> envelopeCreationTOMap;
  private List<EnvelopeTO> envelopeList;

  @BeforeEach
  void setUp() {
    propertyConverter = mock();
    envelopeConverter = mock();
    pointConverter = new PointConverter(propertyConverter, envelopeConverter);

    propertyMap = Map.of("some-key", "some-value");
    propertyList = List.of(new PropertyTO("some-key", "some-value"));
    when(propertyConverter.toPropertyTOs(propertyMap)).thenReturn(propertyList);
    when(propertyConverter.toPropertyMap(propertyList)).thenReturn(propertyMap);

    envelopeMap = Map.of("some-envelope-key", new Envelope(List.of(new Couple(2, 2))));
    envelopeCreationTOMap = Map.of(
        "some-envelope-key", new EnvelopeCreationTO(List.of(new CoupleCreationTO(2, 2)))
    );
    envelopeList = List.of(new EnvelopeTO("some-envelope-key", List.of(new CoupleTO(2, 2))));
    when(envelopeConverter.toEnvelopeTOs(envelopeMap)).thenReturn(envelopeList);
    when(envelopeConverter.toVehicleEnvelopeMap(envelopeList)).thenReturn(envelopeCreationTOMap);
  }

  @Test
  void checkToPointTOs() {
    Point point1 = new Point("P1")
        .withPose(new Pose(new Triple(1, 1, 1), 0.5))
        .withType(Point.Type.HALT_POSITION)
        .withVehicleEnvelopes(envelopeMap)
        .withMaxVehicleBoundingBox(
            new BoundingBox(1000, 2000, 3000)
                .withReferenceOffset(new Couple(4, 5))
        )
        .withLayout(new Point.Layout(new Couple(4, 4), 7))
        .withProperties(propertyMap);

    List<PointTO> result = pointConverter.toPointTOs(Set.of(point1));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("P1"));
    assertThat(result.get(0).getPosition(), samePropertyValuesAs(new TripleTO(1, 1, 1)));
    assertThat(result.get(0).getVehicleOrientationAngle(), is(0.5));
    assertThat(result.get(0).getType(), is(PointTO.Type.HALT_POSITION));
    assertThat(result.get(0).getVehicleEnvelopes(), hasSize(1));
    assertThat(result.get(0).getVehicleEnvelopes(), is(envelopeList));
    assertThat(result.get(0).getMaxVehicleBoundingBox().getLength(), is(1000L));
    assertThat(result.get(0).getMaxVehicleBoundingBox().getWidth(), is(2000L));
    assertThat(result.get(0).getMaxVehicleBoundingBox().getHeight(), is(3000L));
    assertThat(
        result.get(0).getMaxVehicleBoundingBox().getReferenceOffset(),
        samePropertyValuesAs(new CoupleTO(4, 5))
    );
    assertThat(result.get(0).getLayout().getPosition(), samePropertyValuesAs(new CoupleTO(1, 1)));
    assertThat(
        result.get(0).getLayout().getLabelOffset(),
        samePropertyValuesAs(new CoupleTO(4, 4))
    );
    assertThat(result.get(0).getLayout().getLayerId(), is(7));
    assertThat(result.get(0).getProperties(), hasSize(1));
    assertThat(result.get(0).getProperties(), is(propertyList));
  }

  @Test
  void checkToPointCreationTOs() {
    PointTO point1 = new PointTO("P1")
        .setPosition(new TripleTO(1, 1, 1))
        .setVehicleOrientationAngle(0.8)
        .setType(PointTO.Type.HALT_POSITION)
        .setVehicleEnvelopes(envelopeList)
        .setMaxVehicleBoundingBox(
            new BoundingBoxTO(1000, 2000, 3000, new CoupleTO(4, 5))
        )
        .setLayout(
            new PointTO.Layout()
                .setPosition(new CoupleTO(3, 3))
                .setLabelOffset(new CoupleTO(4, 4))
                .setLayerId(9)
        )
        .setProperties(propertyList);

    List<PointCreationTO> result = pointConverter.toPointCreationTOs(List.of(point1));

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getName(), is("P1"));
    assertThat(
        result.get(0).getPose(),
        samePropertyValuesAs(new PoseCreationTO(new TripleCreationTO(1, 1, 1), 0.8))
    );
    assertThat(result.get(0).getType(), is(PointCreationTO.Type.HALT_POSITION));
    assertThat(result.get(0).getVehicleEnvelopes(), is(aMapWithSize(1)));
    assertThat(result.get(0).getVehicleEnvelopes(), is(envelopeCreationTOMap));
    assertThat(result.get(0).getMaxVehicleBoundingBox().getLength(), is(1000L));
    assertThat(result.get(0).getMaxVehicleBoundingBox().getWidth(), is(2000L));
    assertThat(result.get(0).getMaxVehicleBoundingBox().getHeight(), is(3000L));
    assertThat(
        result.get(0).getMaxVehicleBoundingBox().getReferenceOffset(),
        samePropertyValuesAs(new CoupleCreationTO(4, 5))
    );
    assertThat(
        result.get(0).getLayout().getLabelOffset(), samePropertyValuesAs(new CoupleCreationTO(4, 4))
    );
    assertThat(result.get(0).getLayout().getLayerId(), is(9));
    assertThat(result.get(0).getProperties(), is(aMapWithSize(1)));
    assertThat(result.get(0).getProperties(), is(propertyMap));
  }
}
