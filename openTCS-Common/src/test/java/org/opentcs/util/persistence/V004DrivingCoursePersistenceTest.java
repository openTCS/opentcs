/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.util.persistence.v004.AllowedOperationTO;
import org.opentcs.util.persistence.v004.AllowedPeripheralOperationTO;
import org.opentcs.util.persistence.v004.BlockTO;
import org.opentcs.util.persistence.v004.CoupleTO;
import org.opentcs.util.persistence.v004.LocationTO;
import org.opentcs.util.persistence.v004.LocationTypeTO;
import org.opentcs.util.persistence.v004.MemberTO;
import org.opentcs.util.persistence.v004.PathTO;
import org.opentcs.util.persistence.v004.PeripheralOperationTO;
import org.opentcs.util.persistence.v004.PointTO;
import org.opentcs.util.persistence.v004.PropertyTO;
import org.opentcs.util.persistence.v004.V004PlantModelTO;
import org.opentcs.util.persistence.v004.VehicleEnvelopeTO;
import org.opentcs.util.persistence.v004.VehicleTO;
import org.opentcs.util.persistence.v004.VisualLayoutTO;

/**
 */
class V004DrivingCoursePersistenceTest {

  private V004PlantModelTO plantModel;

  @BeforeEach
  void setUp() {
    plantModel = createPlantModel();
  }

  @Test
  void persistAndMaterializeModelAttributes()
      throws IOException {
    plantModel.setVersion("0.0.4");
    plantModel.getProperties().add(new PropertyTO().setName("some-prop").setValue("some-prop-val"));

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V004PlantModelTO parsedModel = V004PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getVersion(), is(equalTo("0.0.4")));
    assertThat(parsedModel.getProperties(), hasSize(1));
    assertThat(parsedModel.getProperties().get(0).getName(), is(equalTo("some-prop")));
    assertThat(parsedModel.getProperties().get(0).getValue(), is(equalTo("some-prop-val")));
  }

  @Test
  void persistAndMaterializePoints()
      throws IOException {
    plantModel.getPoints().get(0).setName("my-point");
    plantModel.getPoints().get(0).setxPosition(1L);
    plantModel.getPoints().get(0).setyPosition(2L);
    plantModel.getPoints().get(0).setzPosition(3L);
    plantModel.getPoints().get(0).setVehicleOrientationAngle(12.34f);
    plantModel.getPoints().get(0).setType("REPORT_POSITION");
    plantModel.getPoints().get(0).getOutgoingPaths()
        .add(new PointTO.OutgoingPath().setName("some-path"));

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V004PlantModelTO parsedModel = V004PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getPoints(), hasSize(2));
    assertThat(parsedModel.getPoints().get(0).getName(), is(equalTo("my-point")));
    assertThat(parsedModel.getPoints().get(0).getxPosition(), is(1L));
    assertThat(parsedModel.getPoints().get(0).getyPosition(), is(2L));
    assertThat(parsedModel.getPoints().get(0).getzPosition(), is(3L));
    assertThat(parsedModel.getPoints().get(0).getVehicleOrientationAngle(), is(12.34f));
    assertThat(parsedModel.getPoints().get(0).getType(), is(equalTo("REPORT_POSITION")));
    assertThat(parsedModel.getPoints().get(0).getOutgoingPaths(), hasSize(1));
    assertThat(parsedModel.getPoints().get(0).getOutgoingPaths().get(0).getName(),
               is(equalTo("some-path")));
  }

  @Test
  void persistAndMaterializePaths()
      throws IOException {
    plantModel.getPaths().get(0).setName("my-path");
    plantModel.getPaths().get(0).setSourcePoint("some-source-point");
    plantModel.getPaths().get(0).setDestinationPoint("some-dest-point");
    plantModel.getPaths().get(0).setLength(1234L);
    plantModel.getPaths().get(0).setLocked(true);
    plantModel.getPaths().get(0).setMaxVelocity(9876L);
    plantModel.getPaths().get(0).setMaxReverseVelocity(5432L);
    plantModel.getPaths().get(0).getPeripheralOperations().add(
        new PeripheralOperationTO()
            .setLocationName("some-loc-name")
            .setExecutionTrigger("AFTER_ALLOCATION")
            .setCompletionRequired(true)
    );
    plantModel.getPaths().get(0).getVehicleEnvelopes().add(
        new VehicleEnvelopeTO()
            .setKey("some-key")
            .setVertices(
                List.of(
                    new CoupleTO()
                        .setX(1234L)
                        .setY(5678L)
                )
            )
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V004PlantModelTO parsedModel = V004PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getPaths(), hasSize(1));
    assertThat(parsedModel.getPaths().get(0).getName(), is(equalTo("my-path")));
    assertThat(parsedModel.getPaths().get(0).getLength(), is(1234L));
    assertThat(parsedModel.getPaths().get(0).isLocked(), is(true));
    assertThat(parsedModel.getPaths().get(0).getMaxVelocity(), is(9876L));
    assertThat(parsedModel.getPaths().get(0).getMaxReverseVelocity(), is(5432L));
    assertThat(parsedModel.getPaths().get(0).getSourcePoint(), is(equalTo("some-source-point")));
    assertThat(parsedModel.getPaths().get(0).getDestinationPoint(), is(equalTo("some-dest-point")));
    assertThat(parsedModel.getPaths().get(0).getPeripheralOperations(), hasSize(1));
    assertThat(parsedModel.getPaths().get(0).getPeripheralOperations().get(0).getLocationName(),
               is(equalTo("some-loc-name")));
    assertThat(parsedModel.getPaths().get(0).getPeripheralOperations().get(0).getExecutionTrigger(),
               is(equalTo("AFTER_ALLOCATION")));
    assertThat(
        parsedModel.getPaths().get(0).getPeripheralOperations().get(0).isCompletionRequired(),
        is(true)
    );
    assertThat(parsedModel.getPaths().get(0).getVehicleEnvelopes(), hasSize(1));
    assertThat(parsedModel.getPaths().get(0).getVehicleEnvelopes().get(0).getKey(), is("some-key"));
    assertThat(
        parsedModel.getPaths().get(0).getVehicleEnvelopes().get(0).getVertices(),
        hasSize(1)
    );
    assertThat(
        parsedModel.getPaths().get(0).getVehicleEnvelopes().get(0).getVertices().get(0).getX(),
        is(1234L)
    );
    assertThat(
        parsedModel.getPaths().get(0).getVehicleEnvelopes().get(0).getVertices().get(0).getY(),
        is(5678L)
    );
  }

  @Test
  void persistAndMaterializeLocationTypes()
      throws IOException {
    plantModel.getLocationTypes().get(0).setName("my-location-type");
    plantModel.getLocationTypes().get(0).getAllowedPeripheralOperations().add(
        (AllowedPeripheralOperationTO) new AllowedPeripheralOperationTO().setName("some-op")
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V004PlantModelTO parsedModel = V004PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getLocationTypes(), hasSize(1));
    assertThat(parsedModel.getLocationTypes().get(0).getName(), is(equalTo("my-location-type")));
    assertThat(parsedModel.getLocationTypes().get(0).getAllowedPeripheralOperations(), hasSize(1));
    assertThat(
        parsedModel.getLocationTypes().get(0).getAllowedPeripheralOperations().get(0).getName(),
        is(equalTo("some-op"))
    );
  }

  @Test
  void persistAndMaterializeLocations()
      throws IOException {
    plantModel.getLocations().get(0).setName("my-location");
    plantModel.getLocations().get(0).setType("some-loc-type");
    plantModel.getLocations().get(0).setLocked(true);
    plantModel.getLocations().get(0).setxPosition(1L);
    plantModel.getLocations().get(0).setyPosition(2L);
    plantModel.getLocations().get(0).setzPosition(3L);
    plantModel.getLocations().get(0).getLinks().add(
        new LocationTO.Link()
            .setPoint("some-point")
            .setAllowedOperations(new ArrayList<>(List.of(
                (AllowedOperationTO) new AllowedOperationTO().setName("some-op")
            )))
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V004PlantModelTO parsedModel = V004PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getLocations(), hasSize(1));
    assertThat(parsedModel.getLocations().get(0).getName(), is(equalTo("my-location")));
    assertThat(parsedModel.getLocations().get(0).getType(), is(equalTo("some-loc-type")));
    assertThat(parsedModel.getLocations().get(0).isLocked(), is(true));
    assertThat(parsedModel.getLocations().get(0).getxPosition(), is(1L));
    assertThat(parsedModel.getLocations().get(0).getyPosition(), is(2L));
    assertThat(parsedModel.getLocations().get(0).getzPosition(), is(3L));
    assertThat(parsedModel.getLocations().get(0).getLinks(), hasSize(1));
    assertThat(parsedModel.getLocations().get(0).getLinks().get(0).getPoint(),
               is(equalTo("some-point")));
    assertThat(parsedModel.getLocations().get(0).getLinks().get(0).getAllowedOperations(),
               hasSize(1));
    assertThat(
        parsedModel.getLocations().get(0).getLinks().get(0).getAllowedOperations().get(0).getName(),
        is(equalTo("some-op"))
    );
  }

  @Test
  void persistAndMaterializeBlocks()
      throws IOException {
    plantModel.getBlocks().get(0).setName("my-block");
    plantModel.getBlocks().get(0).setType("SAME_DIRECTION_ONLY");
    plantModel.getBlocks().get(0).getMembers().add(
        (MemberTO) new MemberTO().setName("some-member")
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V004PlantModelTO parsedModel = V004PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getBlocks(), hasSize(1));
    assertThat(parsedModel.getBlocks().get(0).getName(), is(equalTo("my-block")));
    assertThat(parsedModel.getBlocks().get(0).getType(), is(equalTo("SAME_DIRECTION_ONLY")));
    assertThat(parsedModel.getBlocks().get(0).getMembers(), hasSize(1));
    assertThat(parsedModel.getBlocks().get(0).getMembers().get(0).getName(),
               is(equalTo("some-member")));
  }

  @Test
  void persistAndMaterializeVehicles()
      throws IOException {
    plantModel.getVehicles().get(0).setName("my-vehicle");
    plantModel.getVehicles().get(0).setLength(1234L);
    plantModel.getVehicles().get(0).setMaxVelocity(333);
    plantModel.getVehicles().get(0).setMaxReverseVelocity(444);
    plantModel.getVehicles().get(0).setEnergyLevelCritical(33L);
    plantModel.getVehicles().get(0).setEnergyLevelGood(88L);
    plantModel.getVehicles().get(0).setEnergyLevelSufficientlyRecharged(66L);
    plantModel.getVehicles().get(0).setEnergyLevelFullyRecharged(99L);

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V004PlantModelTO parsedModel = V004PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getVehicles(), hasSize(1));
    assertThat(parsedModel.getVehicles().get(0).getName(), is(equalTo("my-vehicle")));
    assertThat(parsedModel.getVehicles().get(0).getLength(), is(1234L));
    assertThat(parsedModel.getVehicles().get(0).getMaxVelocity(), is(333));
    assertThat(parsedModel.getVehicles().get(0).getMaxReverseVelocity(), is(444));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelCritical(), is(33L));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelGood(), is(88L));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelSufficientlyRecharged(), is(66L));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelFullyRecharged(), is(99L));
  }

  private String toXml(V004PlantModelTO plantModel)
      throws IOException {
    StringWriter writer = new StringWriter();
    plantModel.toXml(writer);
    return writer.toString();
  }

  private V004PlantModelTO createPlantModel() {
    return (V004PlantModelTO) new V004PlantModelTO()
        .setName(UUID.randomUUID().toString())
        .setPoints(new ArrayList<>(List.of(
            (PointTO) new PointTO()
                .setPointLayout(new PointTO.PointLayout()
                    .setxPosition(1L)
                    .setyPosition(2L)
                    .setxLabelOffset(20L)
                    .setyLabelOffset(20L)
                    .setLayerId(0))
                .setName(UUID.randomUUID().toString()),
            (PointTO) new PointTO()
                .setPointLayout(new PointTO.PointLayout()
                    .setxPosition(4L)
                    .setyPosition(5L)
                    .setxLabelOffset(20L)
                    .setyLabelOffset(20L)
                    .setLayerId(0))
                .setName(UUID.randomUUID().toString())
        )))
        .setPaths(new ArrayList<>(List.of(
            (PathTO) new PathTO()
                .setPathLayout(new PathTO.PathLayout()
                    .setConnectionType("DIRECT")
                    .setLayerId(0))
                .setName(UUID.randomUUID().toString())
        )))
        .setLocationTypes(new ArrayList<>(List.of(
            (LocationTypeTO) new LocationTypeTO()
                .setLocationTypeLayout(new LocationTypeTO.LocationTypeLayout()
                    .setLocationRepresentation("LOAD_TRANSFER_GENERIC")
                )
                .setName(UUID.randomUUID().toString())
        )))
        .setLocations(List.of(
            (LocationTO) new LocationTO()
                .setLocationLayout(new LocationTO.LocationLayout()
                    .setxPosition(100L)
                    .setyPosition(200L)
                    .setxLabelOffset(20L)
                    .setyLabelOffset(20L)
                    .setLocationRepresentation("LOAD_TRANSFER_GENERIC")
                    .setLayerId(0))
                .setName(UUID.randomUUID().toString())
        ))
        .setBlocks(List.of(
            (BlockTO) new BlockTO()
                .setBlockLayout(
                    new BlockTO.BlockLayout()
                        .setColor("#FF0000")
                )
                .setName(UUID.randomUUID().toString())
        ))
        .setVehicles(List.of(
            (VehicleTO) new VehicleTO()
                .setVehicleLayout(
                    new VehicleTO.VehicleLayout()
                        .setColor("#FF0000")
                )
                .setName(UUID.randomUUID().toString())
        ))
        .setVisualLayout(
            (VisualLayoutTO) new VisualLayoutTO()
                .setScaleX(50.0f)
                .setScaleY(50.0f)
                .setLayers(List.of(new VisualLayoutTO.Layer()
                    .setId(0)
                    .setOrdinal(0)
                    .setVisible(Boolean.TRUE)
                    .setName(UUID.randomUUID().toString())
                    .setGroupId(0)))
                .setLayerGroups(List.of(new VisualLayoutTO.LayerGroup()
                    .setId(0)
                    .setName(UUID.randomUUID().toString())
                    .setVisible(Boolean.TRUE)))
                .setName(UUID.randomUUID().toString())
        )
        .setVersion("0.0.4");
  }
}
