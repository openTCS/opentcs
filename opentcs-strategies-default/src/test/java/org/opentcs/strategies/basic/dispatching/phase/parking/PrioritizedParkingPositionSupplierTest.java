// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.parking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.LowestCostRouteSelector;
import org.opentcs.strategies.basic.dispatching.phase.TargetedPointsSupplier;

/**
 * Tests for {@link PrioritizedParkingPositionSupplier}.
 */
class PrioritizedParkingPositionSupplierTest {

  private PrioritizedParkingPositionSupplier supplier;
  private InternalPlantModelService plantModelService;
  private Router router;
  private ParkingPositionToPriorityFunction priorityFunction;
  private TargetedPointsSupplier targetedPointsSupplier;
  private DefaultDispatcherConfiguration configuration;

  @BeforeEach
  void setUp() {
    plantModelService = mock(InternalPlantModelService.class);
    router = mock(Router.class);
    priorityFunction = new ParkingPositionToPriorityFunction();
    targetedPointsSupplier = mock(TargetedPointsSupplier.class);
    configuration = mock(DefaultDispatcherConfiguration.class);
    supplier = new PrioritizedParkingPositionSupplier(
        plantModelService,
        router,
        priorityFunction,
        targetedPointsSupplier,
        configuration,
        new LowestCostRouteSelector()
    );
    when(targetedPointsSupplier.getTargetedPoints()).thenReturn(Set.of());
    when(configuration.maxRoutesToConsider()).thenReturn(1);
  }

  @Test
  void returnsPrioritizedParkingPosition() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position without priority")
        .withType(Point.Type.PARK_POSITION);
    Point point3 = new Point("parking position with priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "1");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getRoutes(vehicle, point1, point3, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(
                        new Route.Step(null, point1, point3, Vehicle.Orientation.FORWARD, 0, 10)
                    )
                )
            )
        );
    when(router.getRoutes(vehicle, point1, point2, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(
                        new Route.Step(null, point1, point2, Vehicle.Orientation.FORWARD, 0, 10)
                    )
                )
            )
        );
    when(plantModelService.fetch(Point.class, point1.getReference()))
        .thenReturn(Optional.of(point1));
    when(
        plantModelService.fetch(
            eq(Point.class),
            ArgumentMatchers.<Predicate<? super Point>>any()
        )
    ).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue(result.isPresent());
    assertEquals(point3, result.get());
  }

  @Test
  void returnsClosestPrioritizedParkingPositionForPositionsWithSamePriority() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "4");
    Point point3 = new Point("parking position with the same priority but closer to the vehicle")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "4");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getRoutes(vehicle, point1, point3, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(
                        new Route.Step(null, point1, point3, Vehicle.Orientation.FORWARD, 0, 10)
                    )
                )
            )
        );
    when(router.getRoutes(vehicle, point1, point2, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(
                        new Route.Step(null, point1, point2, Vehicle.Orientation.FORWARD, 0, 30)
                    )
                )
            )
        );
    when(plantModelService.fetch(Point.class, point1.getReference()))
        .thenReturn(Optional.of(point1));
    when(
        plantModelService.fetch(
            eq(Point.class),
            ArgumentMatchers.<Predicate<? super Point>>any()
        )
    ).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue(result.isPresent());
    assertEquals(point3, result.get());
  }

  @Test
  void returnsHigherPrioritizedParkingPositionThanCurrentlyOccupying() {
    Point point1 = new Point("vehicle's current parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "17");
    Point point2 = new Point("parking position with lower priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "20");
    Point point3 = new Point("parking position with higher priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "12");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(router.getRoutes(vehicle, point1, point3, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(
                        new Route.Step(null, point1, point3, Vehicle.Orientation.FORWARD, 0, 10)
                    )
                )
            )
        );
    when(router.getRoutes(vehicle, point1, point2, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(
                        new Route.Step(null, point1, point2, Vehicle.Orientation.FORWARD, 0, 10)
                    )
                )
            )
        );
    when(plantModelService.fetch(Point.class, point1.getReference()))
        .thenReturn(Optional.of(point1));
    when(
        plantModelService.fetch(
            eq(Point.class),
            ArgumentMatchers.<Predicate<? super Point>>any()
        )
    ).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertTrue(result.isPresent());
    assertEquals(point3, result.get());
  }

  @Test
  void returnsEmptyForOnlyPositionsAvailableWithSamePriorityAsCurrentlyOccupying() {
    Point point1 = new Point("vehicle's current parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Point point2 = new Point("parking position with the same priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Point point3 = new Point("another parking position with the same priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(plantModelService.fetch(Point.class, point1.getReference()))
        .thenReturn(Optional.of(point1));
    when(
        plantModelService.fetch(
            eq(Point.class),
            ArgumentMatchers.<Predicate<? super Point>>any()
        )
    ).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertFalse(result.isPresent());
  }

  @Test
  void returnsEmptyForNoHigherPrioritizedParkingPositionsAvailable() {
    Point point1 = new Point("vehicle's current parking position with some priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "13");
    Point point2 = new Point("parking position with lower priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "24");
    Point point3 = new Point("another parking position with lower priority")
        .withType(Point.Type.PARK_POSITION)
        .withProperty(Dispatcher.PROPKEY_PARKING_POSITION_PRIORITY, "45");
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(plantModelService.fetch(Point.class, point1.getReference()))
        .thenReturn(Optional.of(point1));
    when(
        plantModelService.fetch(
            eq(Point.class),
            ArgumentMatchers.<Predicate<? super Point>>any()
        )
    ).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertFalse(result.isPresent());
  }

  @Test
  void returnsEmptyForNoPrioritizedParkingPositionAvailable() {
    Point point1 = new Point("vehicle's current position");
    Point point2 = new Point("parking position with without priority")
        .withType(Point.Type.PARK_POSITION);
    Point point3 = new Point("another parking position without priority")
        .withType(Point.Type.PARK_POSITION);
    Vehicle vehicle = new Vehicle("vehicle")
        .withCurrentPosition(point1.getReference());

    when(plantModelService.fetch(Point.class, point1.getReference()))
        .thenReturn(Optional.of(point1));
    when(
        plantModelService.fetch(
            eq(Point.class),
            ArgumentMatchers.<Predicate<? super Point>>any()
        )
    ).thenReturn(setOf(point2, point3));

    Optional<Point> result = supplier.findParkingPosition(vehicle);
    assertFalse(result.isPresent());
  }

  @SuppressWarnings("unchecked")
  private <T> Set<T> setOf(T... resources) {
    return new HashSet<>(Arrays.asList(resources));
  }
}
