/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.scheduling.ReservationPool;

/**
 * Unit tests for {@link SingleVehicleBlockModule}.
 */
class SingleVehicleBlockModuleTest {

  /**
   * The module to test.
   */
  private SingleVehicleBlockModule module;

  private ReservationPool reservationPool;

  private InternalPlantModelService plantModelService;

  @BeforeEach
  void setUp() {
    reservationPool = mock(ReservationPool.class);
    plantModelService = mock(InternalPlantModelService.class);
    module = new SingleVehicleBlockModule(reservationPool, plantModelService, new Object());
  }

  @Test
  void shouldAllowAllocatioForNonBlocks() {
    Scheduler.Client client = new SampleClient();
    ModelData model = new ModelData();

    when(plantModelService.fetchObjects(eq(Block.class), any())).thenReturn(new HashSet<>());
    assertTrue(module.mayAllocate(client, model.resourcesToAllocate));
  }

  @Test
  void shouldAllowAllocationForUnoccupiedBlock() {
    Scheduler.Client client = new SampleClient();
    ModelData model = new ModelData();

    when(plantModelService.fetchObjects(eq(Block.class), any()))
        .thenReturn(new HashSet<>(Arrays.asList(model.getBlock())));
    when(plantModelService.expandResources(any())).thenReturn(model.getBlockResources());
    when(reservationPool.resourcesAvailableForUser(model.getBlockResources(), client))
        .thenReturn(true);
    assertTrue(module.mayAllocate(client, model.getResourcesToAllocate()));
  }

  @Test
  void shouldDenyAllocationForOccupiedBlock() {
    Scheduler.Client client = new SampleClient();
    ModelData model = new ModelData();

    when(plantModelService.fetchObjects(eq(Block.class), any()))
        .thenReturn(new HashSet<>(Arrays.asList(model.getBlock())));
    when(plantModelService.expandResources(any())).thenReturn(model.getBlockResources());
    when(reservationPool.resourcesAvailableForUser(model.getBlockResources(), client))
        .thenReturn(false);
    assertFalse(module.mayAllocate(client, model.getResourcesToAllocate()));
  }

  private class ModelData {

    private final Set<TCSResource<?>> blockResources = new HashSet<>();
    private final Set<TCSResource<?>> resourcesToAllocate = new HashSet<>();
    private final Block block;

    ModelData() {
      Point pointA = new Point("A");
      Point pointB = new Point("B");
      Point pointC = new Point("C");
      Path pathAB = new Path("A-B", pointA.getReference(), pointB.getReference());
      Path pathBC = new Path("B-C", pointB.getReference(), pointC.getReference());

      Set<TCSResourceReference<?>> resourceRefs = new HashSet<>();
      resourceRefs.add(pointB.getReference());
      resourceRefs.add(pointC.getReference());
      resourceRefs.add(pathAB.getReference());
      resourceRefs.add(pathBC.getReference());
      block = new Block("Block")
          .withMembers(resourceRefs)
          .withType(Block.Type.SINGLE_VEHICLE_ONLY);

      blockResources.add(pathAB);
      blockResources.add(pointB);
      blockResources.add(pathBC);
      blockResources.add(pointC);

      resourcesToAllocate.add(pathAB);
      resourcesToAllocate.add(pointB);
    }

    Set<TCSResource<?>> getBlockResources() {
      return blockResources;
    }

    Set<TCSResource<?>> getResourcesToAllocate() {
      return resourcesToAllocate;
    }

    Block getBlock() {
      return block;
    }
  }

  private class SampleClient
      implements Scheduler.Client {

    @Override
    public String getId() {
      return "SampleClient";
    }

    @Override
    public TCSObjectReference<Vehicle> getRelatedVehicle() {
      return null;
    }

    @Override
    public boolean allocationSuccessful(
        Set<TCSResource<?>> resources) {
      return true;
    }

    @Override
    public void allocationFailed(
        Set<TCSResource<?>> resources) {
    }
  }
}
