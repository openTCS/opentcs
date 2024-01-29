/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules;

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
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;

/**
 * Unit tests for {@link PausedVehicleModule}.
 */
class PausedVehicleModuleTest {

  /**
   * The module to test.
   */
  private PausedVehicleModule module;

  private TCSObjectService objectService;

  @BeforeEach
  void setUp() {
    objectService = mock(TCSObjectService.class);
    module = new PausedVehicleModule(objectService, new Object());
  }

  @Test
  void allowAllocationForUnpausedVehicle() {
    Vehicle vehicle = new Vehicle("some-vehicle").withPaused(false);
    Scheduler.Client client = new SampleClient(vehicle.getName());

    when(objectService.fetchObject(eq(Vehicle.class), any(String.class))).thenReturn(vehicle);

    assertTrue(module.mayAllocate(client, Set.of()));
  }

  @Test
  void refuseAllocationForPausedVehicle() {
    Vehicle vehicle = new Vehicle("some-vehicle").withPaused(true);
    Scheduler.Client client = new SampleClient(vehicle.getName());

    when(objectService.fetchObject(eq(Vehicle.class), any(String.class))).thenReturn(vehicle);

    assertFalse(module.mayAllocate(client, Set.of()));
  }

  private class SampleClient
      implements Scheduler.Client {

    private final String id;

    SampleClient(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
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
