// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.LowestCostRouteSelector;
import org.opentcs.strategies.basic.dispatching.phase.TargetedPointsSupplier;

/**
 */
class DefaultParkingPositionSupplierTest {

  private InternalPlantModelService plantModelService;
  private Router router;
  private Vehicle vehicle;
  private DefaultParkingPositionSupplier supplier;
  private TargetedPointsSupplier targetedPointsSupplier;
  private DefaultDispatcherConfiguration configuration;

  @BeforeEach
  void setUp() {
    plantModelService = mock(InternalPlantModelService.class);
    router = mock(Router.class);
    targetedPointsSupplier = mock(TargetedPointsSupplier.class);
    configuration = mock(DefaultDispatcherConfiguration.class);

    vehicle = new Vehicle("vehicle");
    supplier = new DefaultParkingPositionSupplier(
        plantModelService,
        router,
        targetedPointsSupplier,
        configuration,
        new LowestCostRouteSelector()
    );
    when(targetedPointsSupplier.getTargetedPoints()).thenReturn(Set.of());
    when(configuration.maxRoutesToConsider()).thenReturn(1);
  }

  @AfterEach
  void tearDown() {
    supplier.terminate();
  }

  @Test
  void returnsEmptyForUnknownVehiclePosition() {
    vehicle = vehicle.withCurrentPosition(null);
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  void returnsEmptyForUnknownAssignedParkingPosition() {
    vehicle = vehicle
        .withCurrentPosition(new Point("dummyPoint").getReference())
        .withProperty(Dispatcher.PROPKEY_ASSIGNED_PARKING_POSITION, "someUnknownPoint");
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  void returnsEmptyForNoParkingPositionsFromKernel() {
    when(plantModelService.fetchObjects(Point.class)).thenReturn(new HashSet<>());
    when(plantModelService.fetchObjects(Block.class)).thenReturn(new HashSet<>());
    vehicle = vehicle.withCurrentPosition(new Point("dummyPoint").getReference());
    supplier.initialize();
    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  void returnsClosestParkingPosition() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position")
        .withType(Point.Type.PARK_POSITION);
    Point point3 = new Point("another parking position closer to the vehicle")
        .withType(Point.Type.PARK_POSITION);
    vehicle = new Vehicle("vehicle").withCurrentPosition(point1.getReference());
    when(router.getRoutes(vehicle, point1, point3, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(new Step(null, point1, point3, Vehicle.Orientation.FORWARD, 0, 10))
                )
            )
        );
    when(router.getRoutes(vehicle, point1, point2, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(new Step(null, point1, point2, Vehicle.Orientation.FORWARD, 0, 30))
                )
            )
        );
    when(plantModelService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(plantModelService.fetchObjects(eq(Point.class), any())).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue(result.isPresent());
    assertEquals(point3, result.get());
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> setOf(T... resources) {
    return new HashSet<>(Arrays.asList(resources));
  }
}
