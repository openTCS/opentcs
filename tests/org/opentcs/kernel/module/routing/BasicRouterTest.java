/*
 *
 * Created on February 6, 2007, 11:59 AM
 */
package org.opentcs.kernel.module.routing;

import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.algorithms.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.util.ModelGenerator;

/**
 * A test case for DijkstraRouter.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BasicRouterTest {

  private static final Logger log
      = Logger.getLogger(BasicRouterTest.class.getName());

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
  public void testRouteInRingModel() {
    Model model = ModelGenerator.getRingModel(20, 5);
    Vehicle vehicle = model.createVehicle(null);
    Router router
        = new BasicRouter(model,
                          new RoutingTableBuilderDfs(new RouteEvaluatorDistance(),
                                                     Integer.MAX_VALUE,
                                                     true),
                          false);
    router.updateRoutingTables();
    Point point1 = model.getPoint("Point-0");
    Point point2 = model.getPoint("Point-19");
    point2.setType(Point.Type.HALT_POSITION);
    log.fine("Requesting a route from "
        + point1.getName() + " to " + point2.getName());
    Route route = router.getRoute(vehicle, point1, point2);
    int hopIndex = 0;
    for (Route.Step curStep : route.getSteps()) {
      String pathName = curStep.getPath().getName();
      String pointName = curStep.getDestinationPoint().getName();
      log.fine("Hop " + hopIndex + ": via path " + pathName
          + " to point " + pointName);
      hopIndex++;
      String expectedPointName = "Point-" + hopIndex;
      assertEquals(expectedPointName, pointName);
    }
    log.fine("Costs for this route: " + route.getCosts());
    long expectedCosts = router.getCosts(vehicle,
                                         model.getPoint("Point-0"),
                                         model.getPoint("Point-19"));
    assertEquals(expectedCosts, route.getCosts());
  }
}
