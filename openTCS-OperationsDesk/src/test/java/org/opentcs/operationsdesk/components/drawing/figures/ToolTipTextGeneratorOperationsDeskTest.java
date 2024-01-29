/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.drawing.figures;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.peripherals.PeripheralJob;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.guing.base.AllocationState;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.peripherals.jobs.PeripheralJobsContainer;

/**
 * Unit tests for {@link ToolTipTextGeneratorOperationsDesk}.
 */
class ToolTipTextGeneratorOperationsDeskTest {

  private ToolTipTextGeneratorOperationsDesk toolTipTextGenerator;

  private PeripheralJobsContainer peripheralJobsContainer;

  private Vehicle vehicle;
  private VehicleModel vehicleModel;
  private Location location;
  private LocationType locationType;

  @BeforeEach
  void setup() {
    Locale.setDefault(Locale.forLanguageTag("en"));

    SystemModel systemModel = mock(SystemModel.class);
    when(systemModel.getBlockModels()).thenReturn(new ArrayList<>());

    ModelManager modelManager = mock(ModelManager.class);
    when(modelManager.getModel()).thenReturn(systemModel);

    peripheralJobsContainer = mock(PeripheralJobsContainer.class);
    toolTipTextGenerator = new ToolTipTextGeneratorOperationsDesk(
        modelManager,
        peripheralJobsContainer
    );

    vehicle = new Vehicle("Vehicle-001");
    vehicleModel = new VehicleModel();
    vehicleModel.setVehicle(vehicle);
    vehicleModel.setName(vehicle.getName());

    locationType = new LocationType("Loc-Type-001");
    location = new Location("Location-001", locationType.getReference());
  }

  @Test
  void listNoPeripheralJobs() {
    Approvals.verify(toolTipTextGenerator.getToolTipText(vehicleModel));
  }

  @Test
  void listOnlyPeripheralJobsRelatedToVehicle() {
    PeripheralJob relatedJob = new PeripheralJob(
        "Job-name-001",
        "ReservationToken-01",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference());
    PeripheralJob unrelatedJob = new PeripheralJob(
        "Job-name-002",
        "ReservationToken-02",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(new Vehicle("Other Vehicle").getReference());
    PeripheralJob jobWithNoRelatedVehicle = new PeripheralJob(
        "Job-name-003",
        "ReservationToken-02",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    );

    when(peripheralJobsContainer.getPeripheralJobs())
        .thenReturn(Arrays.asList(relatedJob, unrelatedJob, jobWithNoRelatedVehicle));

    Approvals.verify(toolTipTextGenerator.getToolTipText(vehicleModel));
  }

  @Test
  void listOnlyPeripheralJobsWithCompletionRequired() {
    PeripheralJob completionRequiredJob = new PeripheralJob(
        "Job-name-001",
        "ReservationToken-01",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference());
    PeripheralJob completionNotequiredJob = new PeripheralJob(
        "Job-name-002",
        "ReservationToken-02",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            false
        )
    ).withRelatedVehicle(vehicle.getReference());

    when(peripheralJobsContainer.getPeripheralJobs())
        .thenReturn(Arrays.asList(completionRequiredJob, completionNotequiredJob));

    Approvals.verify(toolTipTextGenerator.getToolTipText(vehicleModel));
  }

  @Test
  public void listOnlyPeripheralJobsInNonFinalState() {
    PeripheralJob job1 = new PeripheralJob(
        "Approved-job-01",
        "ReservationToken-01",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference())
        .withState(PeripheralJob.State.TO_BE_PROCESSED);
    PeripheralJob job2 = new PeripheralJob(
        "Approved-job-02",
        "ReservationToken-02",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference())
        .withState(PeripheralJob.State.BEING_PROCESSED);
    PeripheralJob job3 = new PeripheralJob(
        "Rejected-job-01",
        "ReservationToken-02",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference())
        .withState(PeripheralJob.State.FAILED);
    PeripheralJob job4 = new PeripheralJob(
        "Rejected-job-02",
        "ReservationToken-02",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference())
        .withState(PeripheralJob.State.FINISHED);

    when(peripheralJobsContainer.getPeripheralJobs())
        .thenReturn(Arrays.asList(job1, job2, job3, job4));

    Approvals.verify(toolTipTextGenerator.getToolTipText(vehicleModel));
  }

  @Test
  void sortPeripheralJobsByCreationTime() {
    PeripheralJob firstJob = new PeripheralJob(
        "This one first",
        "ReservationToken-01",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference())
        .withCreationTime(Instant.ofEpochSecond(10));
    PeripheralJob secondJob = new PeripheralJob(
        "This one second",
        "ReservationToken-02",
        new PeripheralOperation(
            location.getReference(),
            "PeripheralOperation",
            PeripheralOperation.ExecutionTrigger.AFTER_MOVEMENT,
            true
        )
    ).withRelatedVehicle(vehicle.getReference())
        .withCreationTime(Instant.ofEpochSecond(999));

    when(peripheralJobsContainer.getPeripheralJobs())
        .thenReturn(Arrays.asList(secondJob, firstJob));

    Approvals.verify(toolTipTextGenerator.getToolTipText(vehicleModel));
  }

  @Test
  void addAllocatingVehicleForPoints() {
    Vehicle vehicle2 = new Vehicle("Vehicle-002");
    VehicleModel vehicleModel2 = new VehicleModel();
    vehicleModel2.setVehicle(vehicle2);
    vehicleModel2.setName(vehicle2.getName());

    PointModel point = new PointModel();
    point.setName("Point-0001");
    point.updateAllocationState(vehicleModel, AllocationState.ALLOCATED);
    point.updateAllocationState(vehicleModel2, AllocationState.CLAIMED);

    Approvals.verify(toolTipTextGenerator.getToolTipText(point));
  }

  @Test
  void addAllocatingVehicleForPaths() {
    Vehicle vehicle2 = new Vehicle("Vehicle-002");
    VehicleModel vehicleModel2 = new VehicleModel();
    vehicleModel2.setVehicle(vehicle2);
    vehicleModel2.setName(vehicle2.getName());

    PathModel path = new PathModel();
    path.setName("Path-0001");
    path.updateAllocationState(vehicleModel, AllocationState.ALLOCATED);
    path.updateAllocationState(vehicleModel2, AllocationState.CLAIMED);

    Approvals.verify(toolTipTextGenerator.getToolTipText(path));
  }

  @Test
  void addAllocatingVehicleForLocations() {
    Vehicle vehicle2 = new Vehicle("Vehicle-002");
    VehicleModel vehicleModel2 = new VehicleModel();
    vehicleModel2.setVehicle(vehicle2);
    vehicleModel2.setName(vehicle2.getName());

    LocationModel location = new LocationModel();
    location.setName("Location-0001");
    location.updateAllocationState(vehicleModel, AllocationState.ALLOCATED);
    location.updateAllocationState(vehicleModel2, AllocationState.CLAIMED);

    Approvals.verify(toolTipTextGenerator.getToolTipText(location));
  }

}
