/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.workingset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.peripherals.PeripheralJobCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.data.model.Triple;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.util.event.SimpleEventBus;

/**
 * Unit tests for {@link PeripheralJobPoolManager}.
 */
class PeripheralJobPoolManagerTest {

  /**
   * The object repository.
   */
  private TCSObjectRepository objectRepo;
  /**
   * Manages plant model data.
   */
  private PlantModelManager plantModelManager;
  /**
   * The job pool manager to be tested here.
   */
  private PeripheralJobPoolManager jobPoolManager;

  @BeforeEach
  void setUp() {
    objectRepo = new TCSObjectRepository();
    plantModelManager = new PlantModelManager(objectRepo, new SimpleEventBus());
    jobPoolManager = new PeripheralJobPoolManager(objectRepo,
                                                  new SimpleEventBus(),
                                                  new PrefixedUlidObjectNameProvider());

    // Set up a minimal plant model.
    plantModelManager.createPlantModelObjects(
        new PlantModelCreationTO("some-plant-model")
            .withLocationType(new LocationTypeCreationTO("some-location-type"))
            .withLocation(new LocationCreationTO("some-location",
                                                 "some-location-type",
                                                 new Triple(1, 2, 3)))
    );
  }

  @Test
  void storeCreatedObjectsInRepo() {
    jobPoolManager.createPeripheralJob(
        new PeripheralJobCreationTO(
            "some-job",
            "some-token",
            new PeripheralOperationCreationTO("some-operation", "some-location")
        )
    );

    assertThat(objectRepo.getObjects(PeripheralJob.class), hasSize(1));
    assertThat(objectRepo.getObject(PeripheralJob.class, "some-job"), is(notNullValue()));
  }

  @Test
  void removeAllCreatedObjectsOnClear() {
    jobPoolManager.createPeripheralJob(
        new PeripheralJobCreationTO(
            "some-job",
            "some-token",
            new PeripheralOperationCreationTO("some-operation", "some-location")
        )
    );

    jobPoolManager.clear();

    assertThat(objectRepo.getObjects(PeripheralJob.class), is(empty()));
  }

  @Test
  public void doNotCreateJobWithCompletionRequiredAndExecutionTriggerImmediate() {
    assertThrows(
        IllegalArgumentException.class, () -> {
          jobPoolManager.createPeripheralJob(
              new PeripheralJobCreationTO(
                  "some-job",
                  "some-token",
                  new PeripheralOperationCreationTO("some-operation", "some-location")
                      .withExecutionTrigger(PeripheralOperation.ExecutionTrigger.IMMEDIATE)
                      .withCompletionRequired(true)
              )
          );
        });
  }
}
