// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.recharging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.RouteSelector;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalPlantModelService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.Route;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.phase.TargetedPointsSupplier;

/**
 * Tests for {@link DefaultRechargePositionSupplier}.
 */
class DefaultRechargePositionSupplierTest {

  private Point currentPosition;
  private Vehicle vehicle;
  private LocationType rechargeLocType;
  private Location rechargeLoc1;
  private Location rechargeLoc2;
  private Location rechargeLoc3;
  private Point locationAccessPoint;

  private InternalPlantModelService plantModelService;
  private Router router;
  private TargetedPointsSupplier targetedPointsSupplier;
  private DefaultDispatcherConfiguration configuration;
  private DefaultRechargePositionSupplier rechargePosSupplier;

  @BeforeEach
  void setUp() {
    currentPosition = new Point("current-position");
    vehicle = new Vehicle("some-vehicle")
        .withCurrentPosition(currentPosition.getReference())
        .withRechargeOperation("Do some recharging");

    rechargeLocType = new LocationType("some-recharge-loc-type")
        .withAllowedOperations(List.of(vehicle.getRechargeOperation()));

    rechargeLoc1 = new Location("recharge-loc-1", rechargeLocType.getReference());
    rechargeLoc2 = new Location("recharge-loc-2", rechargeLocType.getReference());
    rechargeLoc3 = new Location("recharge-loc-3", rechargeLocType.getReference());

    locationAccessPoint = new Point("location-access-point");

    Location.Link link1
        = new Location.Link(rechargeLoc1.getReference(), locationAccessPoint.getReference());
    Location.Link link2
        = new Location.Link(rechargeLoc2.getReference(), locationAccessPoint.getReference());
    Location.Link link3
        = new Location.Link(rechargeLoc3.getReference(), locationAccessPoint.getReference());

    rechargeLoc1 = rechargeLoc1.withAttachedLinks(Set.of(link1));
    rechargeLoc2 = rechargeLoc2.withAttachedLinks(Set.of(link2));
    rechargeLoc3 = rechargeLoc3.withAttachedLinks(Set.of(link3));

    locationAccessPoint = locationAccessPoint.withAttachedLinks(Set.of(link1, link2, link3));

    plantModelService = mock(InternalPlantModelService.class);
    router = mock(Router.class);
    targetedPointsSupplier = mock(TargetedPointsSupplier.class);
    configuration = mock(DefaultDispatcherConfiguration.class);

    rechargePosSupplier = new DefaultRechargePositionSupplier(
        plantModelService,
        router,
        targetedPointsSupplier,
        configuration,
        mock(RouteSelector.class)
    );

    when(plantModelService.fetch(Point.class, currentPosition.getReference()))
        .thenReturn(Optional.of(currentPosition));
    when(plantModelService.fetch(Point.class, locationAccessPoint.getReference()))
        .thenReturn(Optional.of(locationAccessPoint));
    when(plantModelService.fetch(LocationType.class, rechargeLocType.getReference()))
        .thenReturn(Optional.of(rechargeLocType));
    when(plantModelService.fetch(Location.class))
        .thenReturn(Set.of(rechargeLoc1, rechargeLoc2, rechargeLoc3));
    when(plantModelService.expandResources(Set.of(locationAccessPoint.getReference())))
        .thenReturn(Set.of(locationAccessPoint));
    when(configuration.maxRoutesToConsider()).thenReturn(1);
    when(router.getRoutes(vehicle, currentPosition, locationAccessPoint, Set.of(), 1))
        .thenReturn(
            Set.of(
                new Route(
                    List.of(
                        new Route.Step(
                            null,
                            currentPosition,
                            locationAccessPoint,
                            Vehicle.Orientation.FORWARD,
                            0,
                            10
                        )
                    )
                )
            )
        );

    rechargePosSupplier.initialize();
  }

  @AfterEach
  void tearDown() {
    rechargePosSupplier.terminate();
  }

  @Test
  void returnEmptyListForUnknownVehiclePosition() {
    assertThat(
        rechargePosSupplier.findRechargeSequence(vehicle.withCurrentPosition(null)),
        is(empty())
    );
  }

  @Test
  void returnEmptyListForUnknownRechargeOperation() {
    assertThat(
        rechargePosSupplier.findRechargeSequence(
            vehicle.withRechargeOperation("some-unknown-recharge-operation")
        ),
        is(empty())
    );
  }

  @Test
  void returnEmptyListForNonexistentAssignedRechargeLocation() {
    assertThat(
        rechargePosSupplier.findRechargeSequence(
            vehicle.withProperty(
                Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION,
                "some-unknown-location"
            )
        ),
        is(empty())
    );
  }

  @Test
  void returnAnyForNonexistentPreferredRechargeLocation() {
    List<Destination> result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(
            Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION,
            "some-unknown-location"
        )
    );

    assertThat(result, hasSize(1));
    assertThat(
        result.get(0).getDestination(),
        is(
            oneOf(
                rechargeLoc1.getReference(),
                rechargeLoc2.getReference(),
                rechargeLoc3.getReference()
            )
        )
    );
  }

  @Test
  void returnAnyIfVehicleHasNoAssignedOrPreferredLocation() {
    List<Destination> result = rechargePosSupplier.findRechargeSequence(vehicle);

    assertThat(result, hasSize(1));
    assertThat(
        result.get(0).getDestination(),
        is(
            oneOf(
                rechargeLoc1.getReference(),
                rechargeLoc2.getReference(),
                rechargeLoc3.getReference()
            )
        )
    );
  }

  @Test
  void returnAssignedRechargeLocationIfSet() {
    List<Destination> result;

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION, rechargeLoc1.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getDestination(), is(rechargeLoc1.getReference()));

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION, rechargeLoc2.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getDestination(), is(rechargeLoc2.getReference()));

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION, rechargeLoc3.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getDestination(), is(rechargeLoc3.getReference()));
  }

  @Test
  void returnPreferredRechargeLocationIfSet() {
    List<Destination> result;

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION, rechargeLoc1.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getDestination(), is(rechargeLoc1.getReference()));

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION, rechargeLoc2.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getDestination(), is(rechargeLoc2.getReference()));

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION, rechargeLoc3.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(result.get(0).getDestination(), is(rechargeLoc3.getReference()));
  }

  @Test
  void givePrecedenceToAssignedOverPreferredIfBothSet() {
    vehicle = vehicle.withProperty(
        Dispatcher.PROPKEY_PREFERRED_RECHARGE_LOCATION,
        rechargeLoc2.getName()
    );

    List<Destination> result;

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION, rechargeLoc1.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(
        result.get(0).getDestination(),
        is(rechargeLoc1.getReference())
    );

    result = rechargePosSupplier.findRechargeSequence(
        vehicle.withProperty(Dispatcher.PROPKEY_ASSIGNED_RECHARGE_LOCATION, rechargeLoc3.getName())
    );

    assertThat(result, hasSize(1));
    assertThat(
        result.get(0).getDestination(),
        is(rechargeLoc3.getReference())
    );
  }
}
