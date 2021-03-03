/*
 *
 * Created on February 6, 2007, 11:59 AM
 */
package org.opentcs.strategies.basic.routing;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test case for DijkstraRouter.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BasicRouterTest {

  private static final Logger log
      = LoggerFactory.getLogger(BasicRouterTest.class);

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Get a route between two points in a ring model and print its steps.
   */
  @Test
  @Ignore("Requires a kernel (mock) that forwards the model's data")
  public void testRouteInRingModel() {
//    Model model = ModelGenerator.getRingModel(20, 5);
//    Vehicle vehicle = model.createVehicle(null);
//    Router router = new BasicRouter(
//        model,
//        new RoutingTableBuilderDfs(new RouteEvaluatorDistance(new RouteEvaluatorNull()),
//                                   Integer.MAX_VALUE,
//                                   true),
//        false);
//    router.updateRoutingTables();
//    Point point1 = model.getPoint("Point-0");
//    Point point2 = model.getPoint("Point-19");
//    point2.setType(Point.Type.HALT_POSITION);
//    log.fine("Requesting a route from "
//        + point1.getName() + " to " + point2.getName());
//    Route route = router.getRoute(vehicle, point1, point2);
//    int hopIndex = 0;
//    for (Route.Step curStep : route.getSteps()) {
//      String pathName = curStep.getPath().getName();
//      String pointName = curStep.getDestinationPoint().getName();
//      log.fine("Hop " + hopIndex + ": via path " + pathName
//          + " to point " + pointName);
//      hopIndex++;
//      String expectedPointName = "Point-" + hopIndex;
//      assertEquals(expectedPointName, pointName);
//    }
//    log.fine("Costs for this route: " + route.getCosts());
//    long expectedCosts = router.getCosts(vehicle,
//                                         model.getPoint("Point-0"),
//                                         model.getPoint("Point-19"));
//    assertEquals(expectedCosts, route.getCosts());
  }
}
