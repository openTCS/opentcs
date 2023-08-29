/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tests for {@link MovementCommandMapper}.
 */
public class MovementCommandMapperTest {

  private MovementCommandMapper mapper;
  private TCSObjectService objectService;

  @BeforeEach
  public void setUp() {
    objectService = mock(TCSObjectService.class);
    mapper = new MovementCommandMapper(objectService);

  }

  @Test
  public void mapDriveOrderToMovementCommands() {
    Point pointA = new Point("point-a");
    Point pointB = new Point("point-b");
    Point pointC = new Point("point-c");
    Path pathAB = new Path("path-ab", pointA.getReference(), pointB.getReference());
    Path pathBC = new Path("path-bc", pointB.getReference(), pointC.getReference());
    LocationType locationType = new LocationType("location-type");
    Location destinationLocation = new Location("location", locationType.getReference());
    when(objectService.fetchObject(eq(Location.class), eq("location")))
        .thenReturn(destinationLocation);

    Route.Step stepAB = new Route.Step(pathAB, pointA, pointB, Vehicle.Orientation.FORWARD, 0);
    Route.Step stepBC = new Route.Step(pathBC, pointB, pointC, Vehicle.Orientation.FORWARD, 1);
    Route route = new Route(List.of(stepAB, stepBC), 1L);
    DriveOrder driveOrder
        = new DriveOrder(
            new DriveOrder.Destination(destinationLocation.getReference())
                .withOperation("operation")
                .withProperties(Map.of("key1", "value1"))
        ).withRoute(route);

    List<MovementCommand> result
        = mapper.toMovementCommands(driveOrder, Map.of("key2", "value2", "key3", "value3"));

    assertThat(result, hasSize(2));
    assertThat(result.get(0).getRoute(), is(equalTo(route)));
    assertThat(result.get(0).getStep(), is(equalTo(stepAB)));
    assertThat(result.get(0).getOperation(), is(MovementCommand.NO_OPERATION));
    assertThat(result.get(0).getOpLocation(), is(nullValue()));
    assertThat(result.get(0).isFinalMovement(), is(false));
    assertThat(result.get(0).getFinalDestination(), is(pointC));
    assertThat(result.get(0).getFinalDestinationLocation(), is(destinationLocation));
    assertThat(result.get(0).getFinalOperation(), is("operation"));
    assertThat(result.get(0).getProperties(), is(Matchers.aMapWithSize(3)));
    assertThat(result.get(0).getProperties().keySet(), containsInAnyOrder("key1", "key2", "key3"));
    assertThat(result.get(1).getRoute(), is(equalTo(route)));
    assertThat(result.get(1).getStep(), is(equalTo(stepBC)));
    assertThat(result.get(1).getOperation(), is("operation"));
    assertThat(result.get(1).getOpLocation(), is(destinationLocation));
    assertThat(result.get(1).isFinalMovement(), is(true));
    assertThat(result.get(1).getFinalDestination(), is(pointC));
    assertThat(result.get(1).getFinalDestinationLocation(), is(destinationLocation));
    assertThat(result.get(1).getFinalOperation(), is("operation"));
    assertThat(result.get(1).getProperties().keySet(), containsInAnyOrder("key1", "key2", "key3"));
    assertThat(result.get(1).getProperties(), is(Matchers.aMapWithSize(3)));
    assertThat(result.get(1).getProperties().keySet(), containsInAnyOrder("key1", "key2", "key3"));
  }
}
