// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_CUSTOM_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_CUSTOM_DEST_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.DeviationExtensionTrigger;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.Orientation;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Unit tests for {@link OrderMapper}.
 */
public class OrderMapperTest {

  private TransportOrder transportOrder;
  private Vehicle vehicle;
  private TCSObjectService objectService;
  /**
   * The order mapper being tested.
   */
  private OrderMapper mapper;

  @BeforeEach
  public void setup() {
    transportOrder
        = new TransportOrder(
            "some-order",
            List.of(
                new DriveOrder(
                    "drive-order",
                    new DriveOrder.Destination(new Point("some-point").getReference())
                )
            )
        )
            .withCurrentDriveOrderIndex(0);
    vehicle = new Vehicle("vehicle-0001")
        .withPose(new Pose(new Triple(0, 0, 0), Double.NaN))
        .withTransportOrder(transportOrder.getReference())
        .withProperty(ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER, "always");

    objectService = mock(TCSObjectService.class);
    when(objectService.fetch(Vehicle.class, vehicle.getReference()))
        .thenReturn(Optional.of(vehicle));
    when(objectService.fetch(TransportOrder.class, transportOrder.getReference()))
        .thenReturn(Optional.of(transportOrder));

    mapper = new OrderMapper(
        vehicle.getReference(),
        s -> true,
        new DeviationExtensionTrigger(vehicle),
        objectService,
        new NodeMapping()
    );
  }

  @Test
  public void shouldGenerateCorrectOrderWithTwoNodesAndOneEdge() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");
    MovementCommand command = createMovementCommandWithPoints(sourcePoint, destPoint);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getEdges().size(), is(1));
    assertThat(order.getNodes().get(0).getSequenceId(), is(0L));
    assertThat(order.getEdges().get(0).getSequenceId(), is(1L));
    assertThat(order.getNodes().get(1).getSequenceId(), is(2L));
  }

  @Test
  public void generateSingleNodeIfMovementCommandHasNoActualMovement() {
    // setup a movement command
    Point destPoint = new Point("Point-0001");
    MovementCommand command = createMovementCommandWithPoints(null, destPoint);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes(), hasSize(1));
    assertThat(order.getNodes().get(0).getSequenceId(), is(0L));
    assertThat(order.getEdges(), is(empty()));
  }

  @Test
  public void shouldGenerateCustomActions() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameters", "x = 234 | y = 567")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameters", "x = 234 | y = 567");

    Path path = new Path("Path-0001", sourcePoint.getReference(), destPoint.getReference());
    Step step = new Step(path, sourcePoint, destPoint, Orientation.FORWARD, 0, 1);

    MovementCommand command = createMovementCommandWithStep(step);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("beep"));
    assertThat(order.getNodes().get(1).getActions().get(1).getActionType(), is("duck"));
  }

  @Test
  public void shouldGenerateCustomActionsForSourcePoints() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameters", "x = 234 | y = 567");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameters", "x = 234 | y = 567");

    Path path = new Path("Path-0001", sourcePoint.getReference(), destPoint.getReference());
    Step step = new Step(path, sourcePoint, destPoint, Orientation.FORWARD, 0, 1);

    MovementCommand command = createMovementCommandWithStep(step);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(0).getActions().size(), is(1));
    assertThat(order.getNodes().get(1).getActions().size(), is(1));
  }

  @Test
  public void shouldGenerateCustomActionsFromLinkedLocation() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");
    Location destLoc = new Location("Location-0002", new LocationType("Loc-type").getReference())
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameters", "x = 234 | y = 567");

    Path path = new Path("Path-0001", sourcePoint.getReference(), destPoint.getReference());
    Step step = new Step(path, sourcePoint, destPoint, Orientation.FORWARD, 0, 1);

    MovementCommand command = createMovementCommandWithStep(step)
        .withOpLocation(destLoc);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(1));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("duck"));
  }

  @Test
  public void shouldGenerateCustomActionsFromDriveOrder() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");

    MovementCommand command = createMovementCommandWithPointsAndSpeeds(
        sourcePoint,
        destPoint,
        1000,
        500
    );
    LocationType locType = new LocationType("loc-type");
    command = command.withOpLocation(new Location("location-0001", locType.getReference()));
    when(objectService.fetch(LocationType.class, locType.getReference()))
        .thenReturn(Optional.of(locType));

    command = command.withOperation("customOp");
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".customOp.blockingType", "SOFT");
    properties.put(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".customOp.parameters", "x = 234 | y = 567");
    command = command.withProperties(properties);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(1));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("customOp"));
  }

  @Test
  public void onlyAddActionsThatTheVehicleCanExecute() {
    // Setup mapper with a vehicle with executable actions.
    vehicle = vehicle
        .withProperty(ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_beep | tag_duck");
    when(objectService.fetch(Vehicle.class, vehicle.getReference()))
        .thenReturn(Optional.of(vehicle));

    mapper = new OrderMapper(
        vehicle.getReference(),
        new ExecutableActionsTagsPredicate(vehicle),
        new DeviationExtensionTrigger(vehicle),
        objectService,
        new NodeMapping()
    );

    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.tags", "tag_beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03", "run")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03.tags", "tag_run");
    MovementCommand command = createMovementCommandWithPoints(sourcePoint, destPoint);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("duck"));
    assertThat(order.getNodes().get(1).getActions().get(1).getActionType(), is("beep"));
  }

  @Test
  public void onlyAddActionsThatTheMovementCommandCanExecute() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.tags", "tag_beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03", "run")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03.tags", "tag_run");

    MovementCommand command = createMovementCommandWithPoints(sourcePoint, destPoint);
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_beep | tag_duck");
    command = command.withProperties(properties);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("duck"));
    assertThat(order.getNodes().get(1).getActions().get(1).getActionType(), is("beep"));
  }

  @Test
  public void destNodeAndSourceNodeOfConsecutiveOrdersMustMatch() {
    // setup a movement command
    Point startPoint = new Point("Point-0001");
    Point middlePoint = new Point("Point-0002");
    Point endPoint = new Point("Point-0003");

    MovementCommand commandOne = createMovementCommandWithPoints(startPoint, middlePoint);
    Order orderOne = mapper.toOrder(commandOne);

    MovementCommand commandTwo = createMovementCommandWithPoints(middlePoint, endPoint);
    Order orderTwo = mapper.toOrder(commandTwo);

    assertThat(orderOne.getNodes().size(), is(2));
    assertThat(orderTwo.getNodes().size(), is(2));

    Node dest = orderOne.getNodes().get(1);
    Node source = orderTwo.getNodes().get(0);

    assertEquals(dest.getNodeId(), source.getNodeId());
    assertEquals(dest.getSequenceId(), source.getSequenceId());
    assertEquals(dest.getNodePosition(), source.getNodePosition());
    assertEquals(dest.getActions().size(), source.getActions().size());
  }

  @Test
  public void destNodeAndSourceNodeOfConsecutiveOrdersMustNotMatchIfANewOrderHasStarted() {
    // setup a movement command
    Point startPoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");
    Point sourcePoint = new Point("Point-0003");
    Point endPoint = new Point("Point-0004");

    MovementCommand commandOne = createMovementCommandWithPoints(startPoint, destPoint);
    Order orderOne = mapper.toOrder(commandOne);

    when(objectService.fetch(TransportOrder.class, transportOrder.getReference()))
        .thenReturn(Optional.of(transportOrder.withCurrentDriveOrderIndex(1)));

    MovementCommand commandTwo = createMovementCommandWithPoints(sourcePoint, endPoint);
    Order orderTwo = mapper.toOrder(commandTwo);

    assertThat(orderOne.getNodes().size(), is(2));
    assertThat(orderTwo.getNodes().size(), is(2));

    Node dest = orderOne.getNodes().get(1);
    assertThat(dest.getNodeId(), is(destPoint.getName()));

    Node source = orderTwo.getNodes().get(0);
    assertThat(source.getNodeId(), is(sourcePoint.getName()));
  }

  @Test
  public void filterDestinationNodeActionsBasedOnEdgeTags() {
    Point source = new Point("Point-0001");
    Point dest = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.tags", "tag_duck");
    Path path = new Path("path-0001", source.getReference(), dest.getReference())
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_duck");

    Step step = new Step(
        path,
        source,
        dest,
        Orientation.FORWARD,
        0,
        1
    );
    MovementCommand command = createMovementCommandWithStep(step);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));

    Node destNode = order.getNodes().get(1);
    assertThat(destNode.getActions().size(), is(1));
    assertThat(destNode.getActions().get(0).getActionType(), is("duck"));
  }

  @Test
  public void consecutiveOrdersMatchWithEdgeFilter() {
    Point source = new Point("Point-0001");
    Point middle = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_beep");
    Point dest = new Point("Point-0003");

    Path path = new Path("path-0001", source.getReference(), dest.getReference())
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_duck");
    Step step = new Step(path, source, dest, Orientation.FORWARD, 0, 1);

    MovementCommand commandOne = createMovementCommandWithStep(step);
    Order orderOne = mapper.toOrder(commandOne);

    MovementCommand commandTwo = createMovementCommandWithPoints(middle, source);
    Order orderTwo = mapper.toOrder(commandTwo);

    assertThat(orderOne.getNodes().size(), is(2));
    assertThat(orderTwo.getNodes().size(), is(2));

    Node destNode = orderOne.getNodes().get(1);
    Node sourceNode = orderTwo.getNodes().get(0);

    assertEquals(destNode.getNodeId(), sourceNode.getNodeId());
    assertEquals(destNode.getSequenceId(), sourceNode.getSequenceId());
    assertEquals(destNode.getNodePosition(), sourceNode.getNodePosition());
    assertEquals(destNode.getActions().size(), sourceNode.getActions().size());
  }

  @Test
  public void firstNodeShouldHaveVehicleBasedDeviationRange() {
    // The source point is 1.234m away from the vehicle.
    // The deviation range of that node should be at least this distance.
    Point source = new Point("Point-0001");
    source = source.withPose(
        source.getPose().withPosition(
            new Triple(
                vehicle.getPose().getPosition().getX() + 1234,
                vehicle.getPose().getPosition().getY(),
                0
            )
        )
    );
    Point dest = new Point("Point-0002");

    MovementCommand commandOne = createMovementCommandWithPoints(source, dest);
    Order orderOne = mapper.toOrder(commandOne);

    assertThat(orderOne.getNodes().size(), is(2));

    Node sourceNode = orderOne.getNodes().get(0);
    assertThat(sourceNode.getNodePosition().getAllowedDeviationTheta(), is(greaterThan(1.234)));
  }

  @Test
  public void includeRouteAsHorizon() {
    Point p1 = new Point("Point-0001");
    Point p2 = new Point("Point-0002");
    Point p3 = new Point("Point-0003");

    Path l1 = new Path("path-0001", p1.getReference(), p2.getReference());
    Step s1 = new Step(l1, p1, p2, Orientation.FORWARD, 0, 1);
    Path l2 = new Path("path-0002", p2.getReference(), p3.getReference());
    Step s2 = new Step(l2, p2, p3, Orientation.FORWARD, 1, 1);

    MovementCommand command = createMovementCommandWithRoute(new Route(Arrays.asList(s1, s2)), 0)
        .withFinalOperation(MovementCommand.NO_OPERATION)
        .withFinalDestinationLocation(
            new Location("Location-0001", new LocationType("loc-type").getReference())
        );

    Order order = mapper.toOrder(command);
    assertThat(order.getNodes().size(), is(3));
    assertThat(order.getEdges().size(), is(2));
    assertThat(order.getNodes().get(0).isReleased(), is(true));
    assertThat(order.getNodes().get(1).isReleased(), is(true));
    assertThat(order.getNodes().get(2).isReleased(), is(false));
    assertThat(order.getEdges().get(0).isReleased(), is(true));
    assertThat(order.getEdges().get(1).isReleased(), is(false));
  }

  private MovementCommand createMovementCommandWithStep(Step step) {
    return createBasicMovementCommand(null, new Point("dest"), 1000, 500, 0, false)
        .withStep(step)
        .withDriveOrder(
            new DriveOrder(
                "drive-order",
                new DriveOrder.Destination(new Point("point1").getReference())
            )
                .withRoute(new Route(Arrays.asList(step)))
        );
  }

  private MovementCommand createMovementCommandWithRoute(Route route, int currentIndex) {
    return createBasicMovementCommand(null, new Point("dest"), 1000, 500, 0, false)
        .withDriveOrder(
            new DriveOrder(
                "drive-order",
                new DriveOrder.Destination(new Point("point1").getReference())
            )
                .withRoute(route)
        )
        .withStep(route.getSteps().get(currentIndex));
  }

  private MovementCommand createMovementCommandWithPoints(Point source, Point dest) {
    return createBasicMovementCommand(source, dest, 1000, 500, 0, false);
  }

  private MovementCommand createMovementCommandWithPointsAndSpeeds(
      Point source,
      Point dest,
      int maxSpeedForward,
      int maxSpeedReverse
  ) {
    return createBasicMovementCommand(source, dest, maxSpeedForward, maxSpeedReverse, 0, false);
  }

  private MovementCommand createBasicMovementCommand(
      Point source,
      Point dest,
      int maxSpeedForward,
      int maxSpeedReverse,
      int routeIndex,
      boolean isFinalMovement
  ) {
    MovementCommand movementCommand = new MovementCommand(
        new TransportOrder("1", List.of()),
        new DriveOrder("drive-order", new DriveOrder.Destination(new Point("p1").getReference())),
        new Route.Step(null, null, new Point("p2"), Orientation.FORWARD, 0, 1),
        "NOP",
        null,
        isFinalMovement,
        null,
        new Point("p3"),
        "NOP",
        Map.of()
    );

    Path path = null;
    if (source != null && dest != null) {
      path = new Path("Path-0001", source.getReference(), dest.getReference())
          .withMaxVelocity(maxSpeedForward)
          .withMaxReverseVelocity(maxSpeedReverse);
    }

    Route.Step newstep = new Step(
        path,
        source,
        dest,
        Orientation.FORWARD,
        routeIndex,
        1
    );

    return movementCommand
        .withStep(newstep)
        .withDriveOrder(
            new DriveOrder(
                "drive-order",
                new DriveOrder.Destination(new Point("point1").getReference())
            )
                .withRoute(new Route(Arrays.asList(newstep)))
        );
  }
}
