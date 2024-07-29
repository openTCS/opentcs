/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.watchdog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.watchdog.StrandedVehicles.VehicleSnapshot;

/**
 * Tests for {@link StrandedVehicles}.
 */
public class StrandedVehiclesTest {

  private StrandedVehicles stranded;
  private TCSObjectService objectService;
  private Vehicle vehicle;
  private Point parkingPoint;
  private Point noParkingPoint;
  private TransportOrder transportOrder;
  private TimeProvider timeProvider;

  @BeforeEach
  public void setUp() {
    objectService = mock();
    timeProvider = mock();

    vehicle = new Vehicle("vehicle");

    transportOrder = new TransportOrder("TransportOrder", mock());

    parkingPoint = new Point("point").withType(Point.Type.PARK_POSITION);
    noParkingPoint = new Point("point 2").withType(Point.Type.HALT_POSITION);

    when(objectService.fetchObject(Point.class, parkingPoint.getReference()))
        .thenReturn(parkingPoint);
    when(objectService.fetchObject(Point.class, noParkingPoint.getReference()))
        .thenReturn(noParkingPoint);
    when(timeProvider.getCurrentTime()).thenReturn(0L);

    stranded = new StrandedVehicles(objectService, timeProvider);
  }

  @Test
  void considerVehicleAtNoParkingPositionAsNewlyStranded() {
    vehicle = vehicle
        .withState(Vehicle.State.IDLE)
        .withCurrentPosition(noParkingPoint.getReference())
        .withTransportOrder(transportOrder.getReference());
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));
    stranded.initialize();

    long strandedDurationThreshold = 300000;
    long firstInvocationTime = 10000;
    long secondInvocationTime = firstInvocationTime + strandedDurationThreshold + 1000;

    // After the first invocation (when not exceeding the stranded duration threshold), the vehicle
    // should not be considered stranded.
    stranded.identifyStrandedVehicles(firstInvocationTime, strandedDurationThreshold);
    Set<VehicleSnapshot> result = stranded.newlyStrandedVehicles();
    assertTrue(result.isEmpty());

    // After the second invocation (when exceeding the stranded duration threshold), the vehicle
    // should be considered stranded.
    stranded.identifyStrandedVehicles(secondInvocationTime, strandedDurationThreshold);
    result = stranded.newlyStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(true));
    assertThat(result.iterator().next().getVehicle(), is(equalTo(vehicle)));

    stranded.terminate();
  }

  @Test
  void newlyStrandedVehicleShouldNotContainAlreadyStrandedVehicle() {
    vehicle = vehicle
        .withState(Vehicle.State.IDLE)
        .withCurrentPosition(noParkingPoint.getReference())
        .withTransportOrder(transportOrder.getReference());
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));
    stranded.initialize();

    long strandedDurationThreshold = 300000;
    long firstInvocationTime = strandedDurationThreshold + 1000;
    long secondInvocationTime = firstInvocationTime + strandedDurationThreshold + 1000;

    // After the first invocation (when exceeding the stranded duration threshold), the vehicle
    // should be considered stranded.
    stranded.identifyStrandedVehicles(firstInvocationTime, strandedDurationThreshold);
    Set<VehicleSnapshot> result = stranded.newlyStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(true));
    assertThat(result.iterator().next().getVehicle(), is(equalTo(vehicle)));

    // After the second invocation (when exceeding the stranded duration threshold), the already
    // stranded vehicle should not be considered as newly stranded.
    stranded.identifyStrandedVehicles(secondInvocationTime, strandedDurationThreshold);
    result = stranded.newlyStrandedVehicles();
    assertTrue(result.isEmpty());

    stranded.terminate();
  }

  @Test
  void considerVehicleWithChangedStateAsNoLongerStranded() {
    vehicle = vehicle
        .withState(Vehicle.State.IDLE)
        .withCurrentPosition(noParkingPoint.getReference())
        .withTransportOrder(transportOrder.getReference());
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));
    stranded.initialize();

    long strandedDurationThreshold = 300000;
    long firstInvocationTime = strandedDurationThreshold + 1000;
    long secondInvocationTime = firstInvocationTime + 1000;

    // After the first invocation (when exceeding the stranded duration threshold), the vehicle
    // should be considered stranded.
    stranded.identifyStrandedVehicles(firstInvocationTime, strandedDurationThreshold);
    Set<VehicleSnapshot> result = stranded.newlyStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(true));

    vehicle = vehicle.withState(Vehicle.State.EXECUTING);
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));

    // After the second invocation (when the vehicle is no longer in a "stranded" state), the
    // vehicle should be considered no longer stranded.
    stranded.identifyStrandedVehicles(secondInvocationTime, strandedDurationThreshold);
    result = stranded.newlyStrandedVehicles();
    assertThat(result, is(empty()));
    result = stranded.noLongerStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(false));

    stranded.terminate();
  }

  @Test
  void considerVehicleWithChangedPositionAsNoLongerStranded() {
    vehicle = vehicle
        .withState(Vehicle.State.IDLE)
        .withCurrentPosition(noParkingPoint.getReference())
        .withTransportOrder(transportOrder.getReference());
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));
    stranded.initialize();

    long strandedDurationThreshold = 300000;
    long firstInvocationTime = strandedDurationThreshold + 1000;
    long secondInvocationTime = firstInvocationTime + 1000;

    // After the first invocation (when exceeding the stranded duration threshold), the vehicle
    // should be considered stranded.
    stranded.identifyStrandedVehicles(firstInvocationTime, strandedDurationThreshold);
    Set<VehicleSnapshot> result = stranded.newlyStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(true));

    vehicle = vehicle.withCurrentPosition(parkingPoint.getReference());
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));

    // After the second invocation (when the vehicle is no longer in a "stranded" state), the
    // vehicle should be considered no longer stranded.
    stranded.identifyStrandedVehicles(secondInvocationTime, strandedDurationThreshold);
    result = stranded.newlyStrandedVehicles();
    assertThat(result, is(empty()));
    result = stranded.noLongerStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(false));

    stranded.terminate();
  }

  @Test
  void considerVehicleWithChangedTransportOrderAsNoLongerStranded() {
    vehicle = vehicle
        .withState(Vehicle.State.IDLE)
        .withCurrentPosition(noParkingPoint.getReference())
        .withTransportOrder(transportOrder.getReference());
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));
    stranded.initialize();

    long strandedDurationThreshold = 300000;
    long firstInvocationTime = strandedDurationThreshold + 1000;
    long secondInvocationTime = firstInvocationTime + 1000;

    // After the first invocation (when exceeding the stranded duration threshold), the vehicle
    // should be considered stranded.
    stranded.identifyStrandedVehicles(firstInvocationTime, strandedDurationThreshold);
    Set<VehicleSnapshot> result = stranded.newlyStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(true));

    vehicle = vehicle
        .withTransportOrder(
            new TransportOrder("TransportOrder2", mock()).getReference()
        );
    when(objectService.fetchObjects(Vehicle.class)).thenReturn(Set.of(vehicle));
    // After the second invocation (when the vehicle is no longer in a "stranded" state), the
    // vehicle should be considered no longer stranded.
    stranded.identifyStrandedVehicles(secondInvocationTime, strandedDurationThreshold);
    result = stranded.newlyStrandedVehicles();
    assertThat(result, is(empty()));
    result = stranded.noLongerStrandedVehicles();
    assertThat(result, hasSize(1));
    assertThat(result.iterator().next().isStranded(), is(false));

    stranded.terminate();
  }
}
