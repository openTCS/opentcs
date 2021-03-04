/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.DriveOrder.Destination;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import static org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration.ReroutingImpossibleStrategy.IGNORE_PATH_LOCKS;

/**
 * Test cases for {@link RerouteUtil}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RerouteUtilTest {

  /**
   * The class to test.
   */
  private RerouteUtil rerouteUtil;
  /**
   * The router.
   */
  private Router router;
  /**
   * The vehicle controller pool.
   */
  private VehicleControllerPool vehicleControllerPool;
  /**
   * The object service.
   */
  private InternalTransportOrderService transportOrderService;

  private DefaultDispatcherConfiguration configuration;

  public RerouteUtilTest() {
  }

  @Before
  public void setUp() {
    router = mock(Router.class);
    vehicleControllerPool = mock(VehicleControllerPool.class);
    transportOrderService = mock(InternalTransportOrderService.class);
    configuration = mock(DefaultDispatcherConfiguration.class);
    when(configuration.reroutingImpossibleStrategy()).thenReturn(IGNORE_PATH_LOCKS);
    rerouteUtil = new RerouteUtil(router,
                                  vehicleControllerPool,
                                  transportOrderService,
                                  configuration);
  }

  @Test
  public void shouldMergeDriveOrders() {
    DriveOrder orderA = createDriveOrder(10, "A", "B", "C", "D", "E", "F", "G");
    DriveOrder orderB = createDriveOrder(10, "D", "H", "I", "J");

    when(router.getCosts(any(Vehicle.class), any(Point.class), any(Point.class))).thenReturn(20L);

    Route expected = createDriveOrder(20, "A", "B", "C", "D", "H", "I", "J").getRoute();
    Route actual = rerouteUtil.mergeDriveOrders(orderA, orderB, new Vehicle("Vehicle")).getRoute();

    assertEquals(expected, actual);
  }

  @Test
  public void shouldReturnFutureSteps() {
    List<DriveOrder> driveOrders = Arrays.asList(createDriveOrder(10, "A", "B", "C"),
                                                 createDriveOrder(20, "C", "D", "E", "F", "G", "H"),
                                                 createDriveOrder(20, "H", "I", "J", "K", "L"));
    TransportOrder transportOrder = new TransportOrder("TransportOrder", driveOrders)
        .withCurrentDriveOrderIndex(1);
    Vehicle vehicle = new Vehicle("Vehicle")
        .withTransportOrder(transportOrder.getReference());

    VehicleController controller = mock(VehicleController.class);
    when(transportOrderService.fetchObject(TransportOrder.class, transportOrder.getReference()))
        .thenReturn(transportOrder);
    when(vehicleControllerPool.getVehicleController(vehicle.getName())).thenReturn(controller);
    when(controller.getCommandsSent()).thenReturn(commandsSentToVehicle(driveOrders));

    List<String> expected = Arrays.asList("H", "I", "J", "K", "L");
    List<String> actual = rerouteUtil.getFutureSteps(vehicle).stream()
        .map(step -> step.getDestinationPoint().getName())
        .collect(Collectors.toList());

    assertEquals(expected, actual);
  }

  @Test
  public void shouldReturnFalseForStepsNotEqual() {
    DriveOrder orderA = createDriveOrder(10, "A", "B", "C");
    DriveOrder orderB = createDriveOrder(10, "A", "B");

    assertFalse(rerouteUtil.routesEquals(Arrays.asList(orderA), Arrays.asList(orderB)));
  }

  @Test
  public void shouldReturnFalseForCostsNotEqual() {
    DriveOrder orderA = createDriveOrder(10, "A", "B", "C");
    DriveOrder orderB = createDriveOrder(20, "A", "B", "C");

    assertFalse(rerouteUtil.routesEquals(Arrays.asList(orderA), Arrays.asList(orderB)));
  }

  @Test
  public void shouldReturnTrueForRoutesEqual() {
    DriveOrder orderA = createDriveOrder(10, "A", "B", "C");
    DriveOrder orderB = createDriveOrder(10, "A", "B", "C");

    assertTrue(rerouteUtil.routesEquals(Arrays.asList(orderA), Arrays.asList(orderB)));
  }

  private Queue<MovementCommand> commandsSentToVehicle(List<DriveOrder> orders) {
    Queue<MovementCommand> commandsSent = new LinkedList<>();
    commandsSent.add(createMovementCommand(orders.get(1), 1));
    commandsSent.add(createMovementCommand(orders.get(1), 2));
    commandsSent.add(createMovementCommand(orders.get(1), 3));
    return commandsSent;
  }

  private MovementCommand createMovementCommand(DriveOrder order, int stepIndex) {
    return new MovementCommand(order.getRoute().getSteps().get(stepIndex), "NOP", null, false, null,
                               order.getRoute().getFinalDestinationPoint(), "NOP", new HashMap<>());
  }

  private DriveOrder createDriveOrder(long routeCosts, String startPoint, String... pointNames) {
    List<Point> points = new ArrayList<>();
    for (String pointName : pointNames) {
      points.add(new Point(pointName));
    }
    Destination dest = new Destination(points.get(points.size() - 1).getReference());
    return new DriveOrder(dest).withRoute(createRoute(new Point(startPoint), points, routeCosts));
  }

  private Route createRoute(Point startPoint, List<Point> points, long costs) {
    List<Step> routeSteps = new ArrayList<>();
    Point srcPoint = startPoint;
    Point destPoint = points.get(0);
    Path path = new Path(srcPoint.getName() + " --- " + destPoint.getName(),
                         srcPoint.getReference(),
                         destPoint.getReference());
    routeSteps.add(new Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, 0));

    for (int i = 1; i < points.size(); i++) {
      srcPoint = points.get(i - 1);
      destPoint = points.get(i);
      path = new Path(srcPoint.getName() + " --- " + destPoint.getName(),
                      srcPoint.getReference(),
                      destPoint.getReference());
      routeSteps.add(new Step(path, srcPoint, destPoint, Vehicle.Orientation.FORWARD, i));
    }
    return new Route(routeSteps, costs);
  }
}
