/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.Route.Step;
import org.opentcs.strategies.basic.routing.PointRouter;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ShortestPathPointRouterTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;

  private Path pathAC;

  private ModelEdge edgeAC;

  private ShortestPathPointRouter pointRouter;

  @Before
  public void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");

    pathAC = new Path("A-->C", pointA.getReference(), pointC.getReference());

    edgeAC = new ModelEdge(pathAC, false);

    Graph<String, ModelEdge> graph = new DirectedWeightedMultigraph<>(ModelEdge.class);

    graph.addVertex(pointA.getName());
    graph.addVertex(pointB.getName());
    graph.addVertex(pointC.getName());

    graph.addEdge(pointA.getName(), pointC.getName(), edgeAC);
    graph.setEdgeWeight(edgeAC, 1234);

    pointRouter = new ShortestPathPointRouter(new DijkstraShortestPath<>(graph),
                                              new HashSet<>(Arrays.asList(pointA, pointB, pointC)));
  }

  @Test
  public void returnZeroCostsIfDestinationIsSource() {
    assertEquals(0, pointRouter.getCosts(pointA.getReference(), pointA.getReference()));
  }

  @Test
  public void returnEmptyRouteIfDestinationIsSource() {
    List<Step> steps = pointRouter.getRouteSteps(pointA, pointA);
    assertNotNull(steps);
    assertThat(steps, is(empty()));
  }

  @Test
  public void returnInfiniteCostsIfNoRouteExists() {
    assertEquals(PointRouter.INFINITE_COSTS,
                 pointRouter.getCosts(pointA.getReference(), pointB.getReference()));
  }

  @Test
  public void returnNullIfNoRouteExists() {
    assertNull(pointRouter.getRouteSteps(pointA, pointB));
  }

  @Test
  public void returnGraphPathCostsForExistingRoute() {
    assertEquals(1234,
                 pointRouter.getCosts(pointA.getReference(), pointC.getReference()));
  }

  @Test
  public void returnGraphPathStepsForExistingRoute() {
    List<Step> steps = pointRouter.getRouteSteps(pointA, pointC);
    assertNotNull(steps);
    assertThat(steps, is(not(empty())));
  }

}
