/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.watchdog;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.IntegrationLevel;

/**
 * Tests the {@link BlockConsistencyCheck} watchdog task.
 */
public class BlockConsistencyCheckTest {

  private BlockConsistencyCheck blockCheck;
  private NotificationService notificationService;
  private TCSObjectService objectService;
  private Vehicle vehicle1;
  private Vehicle vehicle2;
  private Vehicle vehicle3;
  private Block block;
  private Point point;
  private Point pointOutSideBlock;

  @BeforeEach
  void setup() {
    notificationService = mock(NotificationService.class);
    objectService = mock(TCSObjectService.class);
    // Setup the object service with 1 block with 2 points and a third point outside of the block.
    vehicle1 = new Vehicle("vehicle 1")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED);
    vehicle2 = new Vehicle("vehicle 2")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED);
    vehicle3 = new Vehicle("vehicle 3")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED);
    point = new Point("Point 1");
    pointOutSideBlock = new Point("Point outside block");
    block = new Block("block 1").withMembers(Set.of(point.getReference()));
    rebuildObjectService();

    blockCheck = new BlockConsistencyCheck(
        mock(ScheduledExecutorService.class),
        objectService,
        notificationService,
        mock(WatchdogConfiguration.class)
    );
  }

  void rebuildObjectService() {
    when(objectService.fetchObjects(Vehicle.class))
        .thenReturn(Set.of(vehicle1, vehicle2, vehicle3));
    when(objectService.fetchObject(Point.class, point.getReference()))
        .thenReturn(point);
    when(objectService.fetchObject(Point.class, pointOutSideBlock.getReference()))
        .thenReturn(pointOutSideBlock);
    when(objectService.fetchObjects(Block.class)).thenReturn(Set.of(block));
  }

  @Test
  void shouldReportViolation() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    vehicle2 = vehicle2.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();

    verify(notificationService).publishUserNotification(any());
  }

  @Test
  void singleVehicleInBlockShouldNotReportViolation() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();

    verify(notificationService, never()).publishUserNotification(any());
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.IntegrationLevel.class, names = {"TO_BE_UTILIZED", "TO_BE_RESPECTED"})
  void onlyIntegratedVehiclesReportViolations(IntegrationLevel integrationLevel) {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference())
        .withIntegrationLevel(integrationLevel);
    vehicle2 = vehicle2.withCurrentPosition(point.getReference())
        .withIntegrationLevel(integrationLevel);
    rebuildObjectService();

    blockCheck.run();

    verify(notificationService).publishUserNotification(any());
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.IntegrationLevel.class, names = {"TO_BE_NOTICED", "TO_BE_IGNORED"})
  void nonIntegratedVehiclesDontReportViolations(IntegrationLevel integrationLevel) {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference())
        .withIntegrationLevel(integrationLevel);
    vehicle2 = vehicle2.withCurrentPosition(point.getReference())
        .withIntegrationLevel(integrationLevel);
    rebuildObjectService();

    blockCheck.run();

    verify(notificationService, never()).publishUserNotification(any());
  }

  @Test
  void aNewVehicleInTheBlockShouldCauseANewViolation() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    vehicle2 = vehicle2.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(1)).publishUserNotification(any());

    vehicle3 = vehicle3.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(2)).publishUserNotification(any());
  }

  @Test
  void vehicleLeavingTheBlockShouldCauseANewMessage() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    vehicle2 = vehicle2.withCurrentPosition(point.getReference());
    vehicle3 = vehicle3.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(1)).publishUserNotification(any());

    vehicle3 = vehicle3.withCurrentPosition(null);
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(2)).publishUserNotification(any());
  }

  @Test
  void vehicleLeavingBlockShouldCausesAViolationResolution() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    vehicle2 = vehicle2.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(1)).publishUserNotification(any());

    vehicle2 = vehicle2.withCurrentPosition(null);
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(2)).publishUserNotification(any());
  }

  @Test
  void singleVehicleLeavingBlockShouldNotCauseAResolution() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, never()).publishUserNotification(any());

    vehicle1 = vehicle1.withCurrentPosition(null);
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, never()).publishUserNotification(any());
  }

  @Test
  void dontSendNotificationIfBlockStaysTheSame() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    vehicle2 = vehicle2.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(1)).publishUserNotification(any());

    blockCheck.run();
    verify(notificationService, times(1)).publishUserNotification(any());
  }

  @Test
  void sendNotificationIfVehiclesInBlockChanged() {
    vehicle1 = vehicle1.withCurrentPosition(point.getReference());
    vehicle2 = vehicle2.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(1)).publishUserNotification(any());

    vehicle1 = vehicle1.withCurrentPosition(null);
    vehicle3 = vehicle3.withCurrentPosition(point.getReference());
    vehicle2 = vehicle2.withCurrentPosition(point.getReference());
    rebuildObjectService();

    blockCheck.run();
    verify(notificationService, times(2)).publishUserNotification(any());
  }

}
