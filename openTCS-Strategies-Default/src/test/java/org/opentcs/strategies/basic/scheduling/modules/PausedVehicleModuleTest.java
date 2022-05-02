/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules;

import java.util.Set;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PausedVehicleModuleTest {

  /**
   * The module to test.
   */
  private PausedVehicleModule module;

  private TCSObjectService objectService;

  @Before
  public void setUp() {
    objectService = mock(TCSObjectService.class);
    module = new PausedVehicleModule(objectService, new Object());
  }

  @Test
  public void allowAllocationForUnpausedVehicle() {
    Vehicle vehicle = new Vehicle("some-vehicle").withPaused(false);
    Scheduler.Client client = new SampleClient(vehicle.getName());

    when(objectService.fetchObject(eq(Vehicle.class), any(String.class))).thenReturn(vehicle);

    assertTrue(module.mayAllocate(client, Set.of()));
  }

  @Test
  public void refuseAllocationForPausedVehicle() {
    Vehicle vehicle = new Vehicle("some-vehicle").withPaused(true);
    Scheduler.Client client = new SampleClient(vehicle.getName());

    when(objectService.fetchObject(eq(Vehicle.class), any(String.class))).thenReturn(vehicle);

    assertFalse(module.mayAllocate(client, Set.of()));
  }

  private class SampleClient
      implements Scheduler.Client {

    private final String id;

    public SampleClient(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
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
