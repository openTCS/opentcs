// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v7;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.util.persistence.v7.BlockTO.BlockLayout;
import org.opentcs.util.persistence.v7.LocationTO.LocationLayout;
import org.opentcs.util.persistence.v7.LocationTypeTO.LocationTypeLayout;
import org.opentcs.util.persistence.v7.PathTO.ControlPoint;
import org.opentcs.util.persistence.v7.PathTO.PathLayout;
import org.opentcs.util.persistence.v7.PointTO.PointLayout;
import org.opentcs.util.persistence.v7.VehicleTO.VehicleLayout;

/**
 */
public class V7DrivingCoursePersistenceTest {

  private V7PlantModelTO plantModel;

  @BeforeEach
  void setUp() {
    plantModel = createPlantModel();
  }

  @Test
  void persistAndMaterializeModelAttributes()
      throws IOException {
    plantModel.setVersion("7.0.0");
    plantModel.getProperties().add(new PropertyTO().setName("some-prop").setValue("some-prop-val"));

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V7PlantModelTO parsedModel = V7PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getVersion(), is(equalTo("7.0.0")));
    assertThat(parsedModel.getProperties(), hasSize(1));
    assertThat(parsedModel.getProperties().get(0).getName(), is(equalTo("some-prop")));
    assertThat(parsedModel.getProperties().get(0).getValue(), is(equalTo("some-prop-val")));
  }

  @Test
  void persistAndMaterializePoints()
      throws IOException {
    plantModel.getPoints().get(0).setName("my-point");
    plantModel.getPoints().get(0).setPositionX(1L);
    plantModel.getPoints().get(0).setPositionY(2L);
    plantModel.getPoints().get(0).setPositionZ(3L);
    plantModel.getPoints().get(0).setVehicleOrientationAngle(12.34f);
    plantModel.getPoints().get(0).setType(PointTO.Type.PARK_POSITION);
    plantModel.getPoints().get(0).setMaxVehicleBoundingBox(
        new BoundingBoxTO()
            .setLength(100)
            .setWidth(200)
            .setHeight(150)
            .setReferenceOffsetX(0)
            .setReferenceOffsetY(10)
    );
    plantModel.getPoints().get(0).getVehicleEnvelopes().add(
        new VehicleEnvelopeTO()
            .setKey("some-key")
            .setVertices(
                List.of(
                    new CoupleTO()
                        .setX(10L)
                        .setY(20L)
                )
            )
    );
    plantModel.getPoints().get(0).getOutgoingPaths()
        .add(new PointTO.OutgoingPath().setName("some-path"));
    plantModel.getPoints().get(0).getProperties().add(
        new PropertyTO()
            .setName("some-name")
            .setValue("some-value")
    );
    plantModel.getPoints().get(0).setPointLayout(
        new PointLayout()
            .setLabelOffsetX(10L)
            .setLabelOffsetY(20L)
            .setLayerId(11)
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V7PlantModelTO parsedModel = V7PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getPoints(), hasSize(2));
    assertThat(parsedModel.getPoints().get(0).getName(), is(equalTo("my-point")));
    assertThat(parsedModel.getPoints().get(0).getPositionX(), is(1L));
    assertThat(parsedModel.getPoints().get(0).getPositionY(), is(2L));
    assertThat(parsedModel.getPoints().get(0).getPositionZ(), is(3L));
    assertThat(parsedModel.getPoints().get(0).getVehicleOrientationAngle(), is(12.34f));
    assertThat(
        parsedModel.getPoints().get(0).getType(), is(
            equalTo(
                PointTO.Type.PARK_POSITION
            )
        )
    );
    assertThat(parsedModel.getPoints().get(0).getMaxVehicleBoundingBox().getLength(), is(100L));
    assertThat(parsedModel.getPoints().get(0).getMaxVehicleBoundingBox().getWidth(), is(200L));
    assertThat(parsedModel.getPoints().get(0).getMaxVehicleBoundingBox().getHeight(), is(150L));
    assertThat(
        parsedModel.getPoints().get(0).getMaxVehicleBoundingBox().getReferenceOffsetX(), is(0L)
    );
    assertThat(
        parsedModel.getPoints().get(0).getMaxVehicleBoundingBox().getReferenceOffsetY(), is(10L)
    );
    assertThat(parsedModel.getPoints().get(0).getVehicleEnvelopes(), hasSize(1));
    assertThat(
        parsedModel.getPoints().get(0).getVehicleEnvelopes().get(0).getKey(), is("some-key")
    );
    assertThat(
        parsedModel.getPoints().get(0).getVehicleEnvelopes().get(0).getVertices(), hasSize(1)
    );
    assertThat(
        parsedModel.getPoints().get(0).getVehicleEnvelopes().get(0).getVertices().get(0).getX(),
        is(10L)
    );
    assertThat(
        parsedModel.getPoints().get(0).getVehicleEnvelopes().get(0).getVertices().get(0).getY(),
        is(20L)
    );
    assertThat(parsedModel.getPoints().get(0).getOutgoingPaths(), hasSize(1));
    assertThat(
        parsedModel.getPoints().get(0).getOutgoingPaths().get(0).getName(),
        is(equalTo("some-path"))
    );
    assertThat(parsedModel.getPoints().get(0).getProperties(), hasSize(1));
    assertThat(parsedModel.getPoints().get(0).getProperties().get(0).getName(), is("some-name"));
    assertThat(parsedModel.getPoints().get(0).getProperties().get(0).getValue(), is("some-value"));
    assertThat(parsedModel.getPoints().get(0).getPointLayout().getLabelOffsetX(), is(10L));
    assertThat(parsedModel.getPoints().get(0).getPointLayout().getLabelOffsetY(), is(20L));
    assertThat(parsedModel.getPoints().get(0).getPointLayout().getLayerId(), is(11));
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
            .setExecutionTrigger(PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION)
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
    plantModel.getPaths().get(0).getProperties().add(
        new PropertyTO()
            .setName("some-name")
            .setValue("some-value")
    );
    plantModel.getPaths().get(0).setPathLayout(
        new PathLayout()
            .setConnectionType(PathLayout.ConnectionType.POLYPATH)
            .setLayerId(1)
            .setControlPoints(
                List.of(
                    new ControlPoint()
                        .setX(10L)
                        .setY(20L)
                )
            )
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V7PlantModelTO parsedModel = V7PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getPaths(), hasSize(1));
    assertThat(parsedModel.getPaths().get(0).getName(), is(equalTo("my-path")));
    assertThat(parsedModel.getPaths().get(0).getLength(), is(1234L));
    assertThat(parsedModel.getPaths().get(0).isLocked(), is(true));
    assertThat(parsedModel.getPaths().get(0).getMaxVelocity(), is(9876L));
    assertThat(parsedModel.getPaths().get(0).getMaxReverseVelocity(), is(5432L));
    assertThat(parsedModel.getPaths().get(0).getSourcePoint(), is(equalTo("some-source-point")));
    assertThat(parsedModel.getPaths().get(0).getDestinationPoint(), is(equalTo("some-dest-point")));
    assertThat(parsedModel.getPaths().get(0).getPeripheralOperations(), hasSize(1));
    assertThat(
        parsedModel.getPaths().get(0).getPeripheralOperations().get(0).getLocationName(),
        is(equalTo("some-loc-name"))
    );
    assertThat(
        parsedModel.getPaths().get(0).getPeripheralOperations().get(0).getExecutionTrigger(),
        is(equalTo(PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION))
    );
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
    assertThat(parsedModel.getPaths().get(0).getProperties(), hasSize(1));
    assertThat(parsedModel.getPaths().get(0).getProperties().get(0).getName(), is("some-name"));
    assertThat(parsedModel.getPaths().get(0).getProperties().get(0).getValue(), is("some-value"));
    assertThat(
        parsedModel.getPaths().get(0).getPathLayout().getConnectionType(),
        is(PathLayout.ConnectionType.POLYPATH)
    );
    assertThat(parsedModel.getPaths().get(0).getPathLayout().getLayerId(), is(1));
    assertThat(parsedModel.getPaths().get(0).getPathLayout().getControlPoints(), hasSize(1));
    assertThat(
        parsedModel.getPaths().get(0).getPathLayout().getControlPoints().get(0).getX(), is(10L)
    );
    assertThat(
        parsedModel.getPaths().get(0).getPathLayout().getControlPoints().get(0).getY(), is(20L)
    );
  }

  @Test
  void persistAndMaterializeLocationTypes()
      throws IOException {
    plantModel.getLocationTypes().get(0).setName("my-location-type");
    plantModel.getLocationTypes().get(0).getAllowedOperations().add(
        (AllowedOperationTO) new AllowedOperationTO().setName("some-op")
    );
    plantModel.getLocationTypes().get(0).getAllowedPeripheralOperations().add(
        (AllowedPeripheralOperationTO) new AllowedPeripheralOperationTO().setName("some-op")
    );
    plantModel.getLocationTypes().get(0).getProperties().add(
        new PropertyTO()
            .setName("some-name")
            .setValue("some-value")
    );
    plantModel.getLocationTypes().get(0).setLocationTypeLayout(
        new LocationTypeLayout()
            .setLocationRepresentation(LocationRepresentation.LOAD_TRANSFER_GENERIC)
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V7PlantModelTO parsedModel = V7PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getLocationTypes(), hasSize(1));
    assertThat(parsedModel.getLocationTypes().get(0).getName(), is(equalTo("my-location-type")));
    assertThat(parsedModel.getLocationTypes().get(0).getAllowedOperations(), hasSize(1));
    assertThat(
        parsedModel.getLocationTypes().get(0).getAllowedOperations().get(0).getName(),
        is(equalTo("some-op"))
    );
    assertThat(parsedModel.getLocationTypes().get(0).getAllowedPeripheralOperations(), hasSize(1));
    assertThat(
        parsedModel.getLocationTypes().get(0).getAllowedPeripheralOperations().get(0).getName(),
        is(equalTo("some-op"))
    );
    assertThat(parsedModel.getLocationTypes().get(0).getProperties(), hasSize(1));
    assertThat(
        parsedModel.getLocationTypes().get(0).getProperties().get(0).getName(), is("some-name")
    );
    assertThat(
        parsedModel.getLocationTypes().get(0).getProperties().get(0).getValue(), is("some-value")
    );
    assertThat(
        parsedModel.getLocationTypes().get(0).getLocationTypeLayout().getLocationRepresentation(),
        is(LocationRepresentation.LOAD_TRANSFER_GENERIC)
    );
  }

  @Test
  void persistAndMaterializeLocations()
      throws IOException {
    plantModel.getLocations().get(0).setName("my-location");
    plantModel.getLocations().get(0).setType("some-loc-type");
    plantModel.getLocations().get(0).setLocked(true);
    plantModel.getLocations().get(0).setPositionX(1L);
    plantModel.getLocations().get(0).setPositionY(2L);
    plantModel.getLocations().get(0).setPositionZ(3L);
    plantModel.getLocations().get(0).getLinks().add(
        new LocationTO.Link()
            .setPoint("some-point")
            .setAllowedOperations(
                new ArrayList<>(
                    List.of(
                        (AllowedOperationTO) new AllowedOperationTO().setName("some-op")
                    )
                )
            )
    );
    plantModel.getLocations().get(0).getProperties().add(
        new PropertyTO()
            .setName("some-name")
            .setValue("some-value")
    );
    plantModel.getLocations().get(0).setLocationLayout(
        new LocationLayout()
            .setLabelOffsetX(10L)
            .setLabelOffsetY(20L)
            .setLocationRepresentation(
                LocationRepresentation.LOAD_TRANSFER_GENERIC
            )
            .setLayerId(11)
    );

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V7PlantModelTO parsedModel = V7PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getLocations(), hasSize(1));
    assertThat(parsedModel.getLocations().get(0).getName(), is(equalTo("my-location")));
    assertThat(parsedModel.getLocations().get(0).getType(), is(equalTo("some-loc-type")));
    assertThat(parsedModel.getLocations().get(0).isLocked(), is(true));
    assertThat(parsedModel.getLocations().get(0).getPositionX(), is(1L));
    assertThat(parsedModel.getLocations().get(0).getPositionY(), is(2L));
    assertThat(parsedModel.getLocations().get(0).getPositionZ(), is(3L));
    assertThat(parsedModel.getLocations().get(0).getLinks(), hasSize(1));
    assertThat(
        parsedModel.getLocations().get(0).getLinks().get(0).getPoint(),
        is(equalTo("some-point"))
    );
    assertThat(
        parsedModel.getLocations().get(0).getLinks().get(0).getAllowedOperations(),
        hasSize(1)
    );
    assertThat(
        parsedModel.getLocations().get(0).getLinks().get(0).getAllowedOperations().get(0).getName(),
        is(equalTo("some-op"))
    );
    assertThat(parsedModel.getLocations().get(0).getProperties(), hasSize(1));
    assertThat(parsedModel.getLocations().get(0).getProperties().get(0).getName(), is("some-name"));
    assertThat(
        parsedModel.getLocations().get(0).getProperties().get(0).getValue(), is("some-value")
    );
    assertThat(parsedModel.getLocations().get(0).getLocationLayout().getLabelOffsetX(), is(10L));
    assertThat(parsedModel.getLocations().get(0).getLocationLayout().getLabelOffsetY(), is(20L));
    assertThat(
        parsedModel.getLocations().get(0).getLocationLayout().getLocationRepresentation(),
        is(LocationRepresentation.LOAD_TRANSFER_GENERIC)
    );
    assertThat(parsedModel.getLocations().get(0).getLocationLayout().getLayerId(), is(11));
  }

  @Test
  void persistAndMaterializeBlocks()
      throws IOException {
    plantModel.getBlocks().get(0).setName("my-block");
    plantModel.getBlocks().get(0).setType(BlockTO.Type.SAME_DIRECTION_ONLY);
    plantModel.getBlocks().get(0).getMembers().add(
        (MemberTO) new MemberTO().setName("some-member")
    );
    plantModel.getBlocks().get(0).getProperties().add(
        new PropertyTO()
            .setName("some-name")
            .setValue("some-value")
    );
    plantModel.getBlocks().get(0).setBlockLayout(new BlockLayout().setColor("#FFFFFF"));

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V7PlantModelTO parsedModel = V7PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getBlocks(), hasSize(1));
    assertThat(parsedModel.getBlocks().get(0).getName(), is(equalTo("my-block")));
    assertThat(
        parsedModel.getBlocks().get(0).getType(),
        is(equalTo(BlockTO.Type.SAME_DIRECTION_ONLY))
    );
    assertThat(parsedModel.getBlocks().get(0).getMembers(), hasSize(1));
    assertThat(
        parsedModel.getBlocks().get(0).getMembers().get(0).getName(),
        is(equalTo("some-member"))
    );
    assertThat(parsedModel.getBlocks().get(0).getProperties(), hasSize(1));
    assertThat(parsedModel.getBlocks().get(0).getProperties().get(0).getName(), is("some-name"));
    assertThat(parsedModel.getBlocks().get(0).getProperties().get(0).getValue(), is("some-value"));
    assertThat(parsedModel.getBlocks().get(0).getBlockLayout().getColor(), is("#FFFFFF"));
  }

  @Test
  void persistAndMaterializeVehicles()
      throws IOException {
    plantModel.getVehicles().get(0).setName("my-vehicle");
    plantModel.getVehicles().get(0).setBoundingBox(
        new BoundingBoxTO()
            .setLength(100)
            .setWidth(200)
            .setHeight(150)
            .setReferenceOffsetX(0)
            .setReferenceOffsetY(10)
    );
    plantModel.getVehicles().get(0).setMaxVelocity(333);
    plantModel.getVehicles().get(0).setMaxReverseVelocity(444);
    plantModel.getVehicles().get(0).setEnergyLevelCritical(33L);
    plantModel.getVehicles().get(0).setEnergyLevelGood(88L);
    plantModel.getVehicles().get(0).setEnergyLevelSufficientlyRecharged(66L);
    plantModel.getVehicles().get(0).setEnergyLevelFullyRecharged(99L);
    plantModel.getVehicles().get(0).setEnvelopeKey("some-key");
    plantModel.getVehicles().get(0).getProperties().add(
        new PropertyTO()
            .setName("some-name")
            .setValue("some-value")
    );
    plantModel.getVehicles().get(0).setVehicleLayout(new VehicleLayout().setColor("#FFFFFF"));

    // Write to XML...
    String xmlOutput = toXml(plantModel);
    // ...then parse it back and verify it contains the same elements.
    V7PlantModelTO parsedModel = V7PlantModelTO.fromXml(new StringReader(xmlOutput));

    assertThat(parsedModel.getVehicles(), hasSize(1));
    assertThat(parsedModel.getVehicles().get(0).getName(), is(equalTo("my-vehicle")));
    assertThat(parsedModel.getVehicles().get(0).getBoundingBox().getLength(), is(100L));
    assertThat(parsedModel.getVehicles().get(0).getBoundingBox().getWidth(), is(200L));
    assertThat(parsedModel.getVehicles().get(0).getBoundingBox().getHeight(), is(150L));
    assertThat(parsedModel.getVehicles().get(0).getBoundingBox().getReferenceOffsetX(), is(0L));
    assertThat(parsedModel.getVehicles().get(0).getBoundingBox().getReferenceOffsetY(), is(10L));
    assertThat(parsedModel.getVehicles().get(0).getMaxVelocity(), is(333));
    assertThat(parsedModel.getVehicles().get(0).getMaxReverseVelocity(), is(444));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelCritical(), is(33L));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelGood(), is(88L));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelSufficientlyRecharged(), is(66L));
    assertThat(parsedModel.getVehicles().get(0).getEnergyLevelFullyRecharged(), is(99L));
    assertThat(parsedModel.getVehicles().get(0).getEnvelopeKey(), is("some-key"));
    assertThat(parsedModel.getVehicles().get(0).getProperties(), hasSize(1));
    assertThat(parsedModel.getVehicles().get(0).getProperties().get(0).getName(), is("some-name"));
    assertThat(
        parsedModel.getVehicles().get(0).getProperties().get(0).getValue(), is("some-value")
    );
    assertThat(parsedModel.getVehicles().get(0).getVehicleLayout().getColor(), is("#FFFFFF"));
  }

  private String toXml(V7PlantModelTO plantModel)
      throws IOException {
    StringWriter writer = new StringWriter();
    plantModel.toXml(writer);
    return writer.toString();
  }

  @SuppressWarnings("checkstyle:LineLength")
  private V7PlantModelTO createPlantModel() {
    return (V7PlantModelTO) new V7PlantModelTO()
        .setName(UUID.randomUUID().toString())
        .setPoints(
            new ArrayList<>(
                List.of(
                    (PointTO) new PointTO()
                        .setPointLayout(
                            new PointLayout()
                                .setLabelOffsetX(20L)
                                .setLabelOffsetY(20L)
                                .setLayerId(0)
                        )
                        .setName(UUID.randomUUID().toString()),
                    (PointTO) new PointTO()
                        .setPointLayout(
                            new PointLayout()
                                .setLabelOffsetX(20L)
                                .setLabelOffsetY(20L)
                                .setLayerId(0)
                        )
                        .setName(UUID.randomUUID().toString())
                )
            )
        )
        .setPaths(
            new ArrayList<>(
                List.of(
                    (PathTO) new PathTO()
                        .setPathLayout(
                            new PathLayout()
                                .setConnectionType(PathLayout.ConnectionType.DIRECT)
                                .setLayerId(0)
                        )
                        .setName(UUID.randomUUID().toString())
                )
            )
        )
        .setLocationTypes(
            new ArrayList<>(
                List.of(
                    (LocationTypeTO) new LocationTypeTO()
                        .setLocationTypeLayout(
                            new LocationTypeLayout()
                                .setLocationRepresentation(
                                    LocationRepresentation.LOAD_TRANSFER_GENERIC
                                )
                        )
                        .setName(UUID.randomUUID().toString())
                )
            )
        )
        .setLocations(
            List.of(
                (LocationTO) new LocationTO()
                    .setLocationLayout(
                        new LocationLayout()
                            .setLabelOffsetX(20L)
                            .setLabelOffsetY(20L)
                            .setLocationRepresentation(
                                LocationRepresentation.LOAD_TRANSFER_GENERIC
                            )
                            .setLayerId(0)
                    )
                    .setName(UUID.randomUUID().toString())
            )
        )
        .setBlocks(
            List.of(
                (BlockTO) new BlockTO()
                    .setBlockLayout(
                        new BlockLayout()
                            .setColor("#FF0000")
                    )
                    .setName(UUID.randomUUID().toString())
            )
        )
        .setVehicles(
            List.of(
                (VehicleTO) new VehicleTO()
                    .setVehicleLayout(
                        new VehicleLayout()
                            .setColor("#FF0000")
                    )
                    .setName(UUID.randomUUID().toString())
            )
        )
        .setVisualLayout(
            (VisualLayoutTO) new VisualLayoutTO()
                .setScaleX(50.0f)
                .setScaleY(50.0f)
                .setLayers(
                    List.of(
                        new VisualLayoutTO.Layer()
                            .setId(0)
                            .setOrdinal(0)
                            .setVisible(Boolean.TRUE)
                            .setName(UUID.randomUUID().toString())
                            .setGroupId(0)
                    )
                )
                .setLayerGroups(
                    List.of(
                        new VisualLayoutTO.LayerGroup()
                            .setId(0)
                            .setName(UUID.randomUUID().toString())
                            .setVisible(Boolean.TRUE)
                    )
                )
                .setName(UUID.randomUUID().toString())
        )
        .setVersion("7.0.0");
  }
}
