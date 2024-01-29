/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import java.util.List;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Unit tests for {@link PlantModelManager}.
 */
class PlantModelManagerTest {

  private TCSObjectRepository objectRepo;
  private PlantModelManager plantModelManager;
  private PlantModelCreationTO plantModelCreationTo;

  @BeforeEach
  @SuppressWarnings("deprecation")
  void setUp() {
    objectRepo = new TCSObjectRepository();
    plantModelManager = new PlantModelManager(objectRepo, new SimpleEventBus());
    plantModelCreationTo = new PlantModelCreationTO("some-plant-model")
        .withPoint(new PointCreationTO("point1"))
        .withPoint(new PointCreationTO("point2"))
        .withPath(
            new PathCreationTO("some-path", "point1", "point2")
                .withPeripheralOperations(List.of(
                    new PeripheralOperationCreationTO("some-op", "some-location")
                ))
        )
        .withLocationType(
            new LocationTypeCreationTO("some-location-type")
                .withAllowedOperations(List.of("some-op"))
        )
        .withLocation(
            new LocationCreationTO("some-location",
                                   "some-location-type",
                                   new Triple(1, 2, 3))
                .withLink("point1", Set.of("some-op"))
        )
        .withBlock(new BlockCreationTO("some-block"))
        .withGroup(new GroupCreationTO("some-group"))
        .withVehicle(new VehicleCreationTO("some-vehicle"))
        .withVisualLayout(new VisualLayoutCreationTO("some-visual-layout"))
        .withProperty("some-prop-key", "some-prop-value");
  }

  @Test
  @SuppressWarnings("deprecation")
  void importPlantModel() {
    plantModelManager.createPlantModelObjects(plantModelCreationTo);

    assertThat(plantModelManager.getName(), is(equalTo("some-plant-model")));
    assertThat(plantModelManager.getProperties(), hasEntry("some-prop-key", "some-prop-value"));
    assertThat(objectRepo.getObjects(Point.class), hasSize(2));
    assertThat(objectRepo.getObject(Point.class, "point1"), is(notNullValue()));
    assertThat(objectRepo.getObject(Point.class, "point2"), is(notNullValue()));
    assertThat(objectRepo.getObjects(Path.class), hasSize(1));
    assertThat(objectRepo.getObject(Path.class, "some-path"), is(notNullValue()));
    assertThat(objectRepo.getObjects(LocationType.class), hasSize(1));
    assertThat(objectRepo.getObject(LocationType.class, "some-location-type"), is(notNullValue()));
    assertThat(objectRepo.getObjects(Location.class), hasSize(1));
    assertThat(objectRepo.getObject(Location.class, "some-location"), is(notNullValue()));
    assertThat(objectRepo.getObjects(Block.class), hasSize(1));
    assertThat(objectRepo.getObject(Block.class, "some-block"), is(notNullValue()));
    assertThat(objectRepo.getObjects(Group.class), hasSize(1));
    assertThat(objectRepo.getObject(Group.class, "some-group"), is(notNullValue()));
    assertThat(objectRepo.getObjects(Vehicle.class), hasSize(1));
    assertThat(objectRepo.getObject(Vehicle.class, "some-vehicle"), is(notNullValue()));
    assertThat(objectRepo.getObjects(VisualLayout.class), hasSize(1));
    assertThat(objectRepo.getObject(VisualLayout.class, "some-visual-layout"), is(notNullValue()));
  }

  @Test
  @SuppressWarnings("deprecation")
  void exportPlantModel() {
    plantModelManager.createPlantModelObjects(plantModelCreationTo);

    PlantModelCreationTO exportedModel = plantModelManager.createPlantModelCreationTO();

    assertThat(exportedModel.getName(), is(equalTo("some-plant-model")));
    assertThat(exportedModel.getPoints(), hasSize(2));
    assertThat(exportedModel.getPaths(), hasSize(1));
    assertThat(exportedModel.getLocationTypes(), hasSize(1));
    assertThat(exportedModel.getLocations(), hasSize(1));
    assertThat(exportedModel.getBlocks(), hasSize(1));
    assertThat(exportedModel.getGroups(), hasSize(1));
    assertThat(exportedModel.getVehicles(), hasSize(1));
  }

  @Test
  @SuppressWarnings("deprecation")
  void clearPlantModel() {
    plantModelManager.createPlantModelObjects(plantModelCreationTo);

    plantModelManager.clear();

    assertThat(objectRepo.getObjects(Point.class), is(empty()));
    assertThat(objectRepo.getObjects(Path.class), is(empty()));
    assertThat(objectRepo.getObjects(LocationType.class), is(empty()));
    assertThat(objectRepo.getObjects(Location.class), is(empty()));
    assertThat(objectRepo.getObjects(Block.class), is(empty()));
    assertThat(objectRepo.getObjects(Group.class), is(empty()));
    assertThat(objectRepo.getObjects(Vehicle.class), is(empty()));
    assertThat(objectRepo.getObjects(VisualLayout.class), is(empty()));
  }

  @Test
  void expandResources() {
    plantModelManager.createPlantModelObjects(
        new PlantModelCreationTO("some-plant-model")
            .withPoint(new PointCreationTO("point-in-block-1"))
            .withPoint(new PointCreationTO("point-in-block-2"))
            .withPoint(new PointCreationTO("point-outside-of-block"))
            .withPath(new PathCreationTO("path-in-block", "point-in-block-1", "point-in-block-2"))
            .withLocationType(new LocationTypeCreationTO("some-location-type"))
            .withLocation(new LocationCreationTO("location-in-block",
                                                 "some-location-type",
                                                 new Triple(1, 2, 3)))
            .withBlock(
                new BlockCreationTO("some-block")
                    .withMemberNames(Set.of("point-in-block-1",
                                            "point-in-block-2",
                                            "path-in-block",
                                            "location-in-block"))
            )
    );

    Path pathInBlock = objectRepo.getObject(Path.class, "path-in-block");
    Point pointInBlock = objectRepo.getObject(Point.class, "point-in-block-1");
    Point pointOutsideOfBlock = objectRepo.getObject(Point.class, "point-outside-of-block");

    assertThat(
        "Single element in block should result in all elements of block.",
        plantModelManager.expandResources(Set.of(pathInBlock.getReference())),
        hasSize(4)
    );
    assertThat(
        "Multiple elements in block should result in all elements of block.",
        plantModelManager.expandResources(Set.of(
            pathInBlock.getReference(), pointInBlock.getReference())
        ),
        hasSize(4)
    );
    assertThat(
        "Single element not in block should result in this single element.",
        plantModelManager.expandResources(Set.of(pointOutsideOfBlock.getReference())),
        hasSize(1)
    );
    assertThat(
        "Elements inside and outside of block should result in block + outside elements.",
        plantModelManager.expandResources(Set.of(
            pointOutsideOfBlock.getReference(), pointInBlock.getReference()
        )),
        hasSize(5)
    );
  }
}
