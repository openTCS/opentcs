/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.watchdog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.kernel.extensions.watchdog.StrandedVehicles.VehicleSnapshot;

/**
 * Tests for {@link StrandedVehicleCheck}.
 */
public class StrandedVehicleCheckTest {

  private StrandedVehicleCheck strandedVehicleCheck;
  private StrandedVehicles stranded;
  private NotificationService notificationService;
  private WatchdogConfiguration watchdogConfiguration;
  private ScheduledExecutorService executorService;
  private Vehicle vehicle1;
  private VehicleSnapshot vehicleSnapshot;

  @BeforeEach
  void setup() {
    notificationService = mock();
    watchdogConfiguration = mock();
    executorService = mock();
    stranded = mock();

    vehicle1 = new Vehicle("vehicle 1")
        .withState(Vehicle.State.IDLE);
    vehicleSnapshot = new VehicleSnapshot(vehicle1);

    when(watchdogConfiguration.strandedVehicleCheckInterval()).thenReturn(1000);
    when(watchdogConfiguration.strandedVehicleDurationThreshold()).thenReturn(300000);

    strandedVehicleCheck = new StrandedVehicleCheck(
        executorService,
        notificationService,
        watchdogConfiguration,
        stranded
    );
  }

  @Test
  void strandedVehicleShouldSendNotification() {
    when(stranded.newlyStrandedVehicles())
        .thenReturn(Set.of(vehicleSnapshot));

    strandedVehicleCheck.run();

    verify(notificationService).publishUserNotification(any());
  }

  @Test
  void strandedVehicleShouldNotSendNotification() {
    when(stranded.newlyStrandedVehicles())
        .thenReturn(Set.of());

    strandedVehicleCheck.run();

    verify(notificationService, never()).publishUserNotification(any());
  }

  @Test
  void shouldUpdateNotification() {
    when(stranded.newlyStrandedVehicles())
        .thenReturn(Set.of(vehicleSnapshot));

    strandedVehicleCheck.run();

    verify(notificationService, times(1)).publishUserNotification(any());

    when(stranded.newlyStrandedVehicles())
        .thenReturn(Set.of());

    when(stranded.noLongerStrandedVehicles())
        .thenReturn(Set.of(vehicleSnapshot));

    strandedVehicleCheck.run();

    verify(notificationService, times(2)).publishUserNotification(any());
  }

}
