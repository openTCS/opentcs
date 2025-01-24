// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link BlockAreaAllocations}.
 */
class BlockAreaAllocationsTest {

  private Point pointA;
  private Point pointB;
  private Point pointY;
  private Point pointZ;
  private Path pathAB;
  private Path pathZY;
  private Block block;
  private Vehicle requestingVehicle;
  private Vehicle otherVehicle;
  private AreaProvider areaProvider;
  private InternalPlantModelService plantModelService;
  private BlockAreaAllocations blockAreaAllocations;

  @BeforeEach
  void setUp() {
    // A plant model with two parallel paths where the envelopes of "a" and "y", and the ones of
    // "b" and "z" overlap.
    pointA = new Point("a")
        .withVehicleEnvelopes(Map.of("key-a", rectEnvelope(0, 400, 1000, 500)));
    pointB = new Point("b")
        .withVehicleEnvelopes(Map.of("key-a", rectEnvelope(1000, 400, 1000, 500)));
    pointY = new Point("y")
        .withVehicleEnvelopes(Map.of("key-b", rectEnvelope(0, -400, 1000, 500)));
    pointZ = new Point("z")
        .withVehicleEnvelopes(Map.of("key-b", rectEnvelope(1000, -400, 1000, 500)));
    pathAB = new Path("a --> b", pointA.getReference(), pointB.getReference());
    pathZY = new Path("y <-- z", pointZ.getReference(), pointY.getReference());
    block = new Block("block");
    requestingVehicle = new Vehicle("vehicle-a").withEnvelopeKey("key-a");
    otherVehicle = new Vehicle("vehicle-b").withEnvelopeKey("key-b");

    plantModelService = mock();
    areaProvider = new CachingAreaProvider(plantModelService);
    blockAreaAllocations = new BlockAreaAllocations(areaProvider, plantModelService);
  }

  @Test
  void preventAllocationWhenAreaOfRequestedPointOverlapsWithOccupiedBlock() {
    otherVehicle = otherVehicle.withAllocatedResources(List.of(Set.of(pointZ.getReference())));
    block = block.withMembers(Set.of(pointY.getReference(), pointZ.getReference()));
    defaultPlantModelServiceConfiguration();
    when(plantModelService.expandResources(Set.of(pointA.getReference())))
        .thenReturn(Set.of(pointA));
    when(plantModelService.expandResources(Set.of(pointZ.getReference())))
        .thenReturn(Set.of(pointY, pointZ));
    areaProvider.initialize();
    blockAreaAllocations.initialize();

    boolean result = blockAreaAllocations.isAreaAllocationAllowed(
        requestingVehicle.getReference(),
        "key-a",
        Set.of(pointA)
    );

    assertThat(result).isFalse();
  }

  @Test
  void preventAllocationWhenAreaOfRequestedBlockOverlapsWithOccupiedPoint() {
    otherVehicle = otherVehicle.withAllocatedResources(List.of(Set.of(pointZ.getReference())));
    block = block.withMembers(Set.of(pointA.getReference(), pointB.getReference()));
    defaultPlantModelServiceConfiguration();
    when(plantModelService.expandResources(Set.of(pointA.getReference())))
        .thenReturn(Set.of(pointA, pointB));
    when(plantModelService.expandResources(Set.of(pointZ.getReference())))
        .thenReturn(Set.of(pointZ));
    areaProvider.initialize();
    blockAreaAllocations.initialize();

    boolean result = blockAreaAllocations.isAreaAllocationAllowed(
        requestingVehicle.getReference(),
        "key-a",
        Set.of(pointA)
    );

    assertThat(result).isFalse();
  }

  @Test
  void allowAllocationWhenAreasNotOverlapping() {
    otherVehicle = otherVehicle.withAllocatedResources(List.of(Set.of(pointZ.getReference())));
    block = block.withMembers(Set.of(pointZ.getReference()));
    defaultPlantModelServiceConfiguration();
    when(plantModelService.expandResources(Set.of(pointA.getReference())))
        .thenReturn(Set.of(pointA));
    when(plantModelService.expandResources(Set.of(pointZ.getReference())))
        .thenReturn(Set.of(pointZ));
    areaProvider.initialize();
    blockAreaAllocations.initialize();

    boolean result = blockAreaAllocations.isAreaAllocationAllowed(
        requestingVehicle.getReference(),
        "key-a",
        Set.of(pointA)
    );

    assertThat(result).isTrue();
  }

  @Test
  void allowAllocationWhenNoBlocksInvolved() {
    otherVehicle = otherVehicle.withAllocatedResources(List.of(Set.of(pointZ.getReference())));
    defaultPlantModelServiceConfiguration();
    areaProvider.initialize();
    blockAreaAllocations.initialize();

    boolean result = blockAreaAllocations.isAreaAllocationAllowed(
        requestingVehicle.getReference(),
        "key-a",
        Set.of(pointB)
    );

    // The envelopes of "b" and "z" overlap but without a block being involved, allocation is
    // allowed as this situation is expected to be handled elsewhere.
    assertThat(result).isTrue();
  }

  @Test
  void ignoreVehiclesOccupyingOrRequestingSameBlock() {
    otherVehicle = otherVehicle.withAllocatedResources(List.of(Set.of(pointZ.getReference())));
    block = block.withMembers(Set.of(pointB.getReference(), pointZ.getReference()));
    defaultPlantModelServiceConfiguration();
    when(plantModelService.expandResources(Set.of(pointB.getReference())))
        .thenReturn(Set.of(pointB, pointZ));
    areaProvider.initialize();
    blockAreaAllocations.initialize();

    boolean result = blockAreaAllocations.isAreaAllocationAllowed(
        requestingVehicle.getReference(),
        "key-a",
        Set.of(pointB)
    );

    assertThat(result).isTrue();
  }

  private Envelope rectEnvelope(long x, long y, long height, long width) {
    return new Envelope(
        List.of(
            new Couple(x - width / 2, y + height / 2),
            new Couple(x + width / 2, y + height / 2),
            new Couple(x + width / 2, y - height / 2),
            new Couple(x - width / 2, y - height / 2),
            new Couple(x - width / 2, y + height / 2)
        )
    );
  }

  private void defaultPlantModelServiceConfiguration() {
    when(plantModelService.fetchObjects(eq(Vehicle.class), any())).thenReturn(Set.of(otherVehicle));
    when(plantModelService.fetchObjects(eq(Point.class), any()))
        .thenReturn(Set.of(pointA, pointB, pointY, pointZ));
    when(plantModelService.fetchObject(Point.class, pointA.getReference())).thenReturn(pointA);
    when(plantModelService.fetchObject(Point.class, pointB.getReference())).thenReturn(pointB);
    when(plantModelService.fetchObject(Point.class, pointY.getReference())).thenReturn(pointY);
    when(plantModelService.fetchObject(Point.class, pointZ.getReference())).thenReturn(pointZ);
    when(plantModelService.fetchObject(Path.class, pathAB.getReference())).thenReturn(pathAB);
    when(plantModelService.fetchObject(Path.class, pathZY.getReference())).thenReturn(pathZY);
    when(plantModelService.fetchObjects(Block.class)).thenReturn(Set.of(block));
  }
}
