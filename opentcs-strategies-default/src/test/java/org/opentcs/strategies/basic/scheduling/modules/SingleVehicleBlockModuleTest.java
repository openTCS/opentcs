// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

    when(plantModelService.fetch(eq(Block.class), ArgumentMatchers.<Predicate<? super Block>>any()))
        .thenReturn(Set.of());
    assertTrue(module.mayAllocate(client, model.resourcesToAllocate));
  }

  @Test
  void shouldAllowAllocationForUnoccupiedBlock() {
    Scheduler.Client client = new SampleClient();
    ModelData model = new ModelData();

    when(
        plantModelService.fetch(eq(Block.class), ArgumentMatchers.<Predicate<? super Block>>any())
    )
        .thenReturn(Set.of(model.getBlock()));
    when(plantModelService.expandResources(any())).thenReturn(model.getBlockResources());
    when(reservationPool.resourcesAvailableForUser(model.getBlockResources(), client))
        .thenReturn(true);
    assertTrue(module.mayAllocate(client, model.getResourcesToAllocate()));
  }

  @Test
  void shouldDenyAllocationForOccupiedBlock() {
    Scheduler.Client client = new SampleClient();
    ModelData model = new ModelData();

    when(plantModelService.fetch(eq(Block.class), ArgumentMatchers.<Predicate<? super Block>>any()))
        .thenReturn(Set.of(model.getBlock()));
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
      implements
        Scheduler.Client {

    @Override
    public String getId() {
      return "SampleClient";
    }

    @Override
    public TCSObjectReference<Vehicle> getRelatedVehicle() {
      return null;
    }

    @Override
    public boolean onAllocation(
        @Nonnull
        Set<TCSResource<?>> resources
    ) {
      return true;
    }
  }
}
