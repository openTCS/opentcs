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
import org.jgrapht.Graph;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultModelGraphMapperTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Point pointD;

  private Path pathAB;
  private Path pathBC;
  private Path pathCD;
  private Path pathAD;

  private ShortestPathConfiguration configuration;
  private DefaultModelGraphMapper mapper;

  @Before
  public void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");
    pointD = new Point("D");

    pathAB = new Path("A-->B", pointA.getReference(), pointB.getReference())
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(0);
    pathBC = new Path("B-->C", pointB.getReference(), pointC.getReference())
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(0);
    pathCD = new Path("C-->D", pointC.getReference(), pointD.getReference())
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(0);
    pathAD = new Path("A<->D", pointA.getReference(), pointD.getReference())
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(1000);

    configuration = mock(ShortestPathConfiguration.class);
    mapper = new DefaultModelGraphMapper(new EdgeEvaluatorHops(), configuration);
  }

  @Test
  public void createEmptyGraph() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, ModelEdge> graph = mapper.translateModel(new HashSet<>(),
                                                           new HashSet<>(),
                                                           new Vehicle("someVehicle"));
    assertEquals("Number of vertices", 0, graph.vertexSet().size());
    assertEquals("Number of edges", 0, graph.edgeSet().size());
  }

  @Test
  public void createGraphWithFourPointsAndNoPath() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, ModelEdge> graph
        = mapper.translateModel(new HashSet<>(Arrays.asList(pointA, pointB, pointC, pointD)),
                                new HashSet<>(),
                                new Vehicle("someVehicle"));
    assertEquals("Number of vertices", 4, graph.vertexSet().size());
    assertTrue(graph.vertexSet().contains(pointA.getName()));
    assertTrue(graph.vertexSet().contains(pointB.getName()));
    assertTrue(graph.vertexSet().contains(pointC.getName()));
    assertTrue(graph.vertexSet().contains(pointD.getName()));
    assertEquals("Number of edges", 0, graph.edgeSet().size());
  }

  @Test
  public void createGraphWithFourPointsAndOneUnidirectionalPath() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, ModelEdge> graph
        = mapper.translateModel(new HashSet<>(Arrays.asList(pointA, pointB, pointC, pointD)),
                                new HashSet<>(Arrays.asList(pathAB)),
                                new Vehicle("someVehicle"));
    assertEquals("Number of vertices", 4, graph.vertexSet().size());
    assertTrue(graph.vertexSet().contains(pointA.getName()));
    assertTrue(graph.vertexSet().contains(pointB.getName()));
    assertTrue(graph.vertexSet().contains(pointC.getName()));
    assertTrue(graph.vertexSet().contains(pointD.getName()));
    assertEquals("Number of edges", 1, graph.edgeSet().size());
    assertEquals("Forward edges for path " + pathAB.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathAB.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
  }

  @Test
  public void createGraphWithFourPointsAndOneBidirectionalPath() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, ModelEdge> graph
        = mapper.translateModel(new HashSet<>(Arrays.asList(pointA, pointB, pointC, pointD)),
                                new HashSet<>(Arrays.asList(pathAD)),
                                new Vehicle("someVehicle"));
    assertEquals("Number of vertices", 4, graph.vertexSet().size());
    assertTrue(graph.vertexSet().contains(pointA.getName()));
    assertTrue(graph.vertexSet().contains(pointB.getName()));
    assertTrue(graph.vertexSet().contains(pointC.getName()));
    assertTrue(graph.vertexSet().contains(pointD.getName()));
    assertEquals("Number of edges", 2, graph.edgeSet().size());
    assertEquals("Forward edges for path " + pathAD.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathAD.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals("Reverse edges for path " + pathAD.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathAD.getName()))
                     .filter(edge -> edge.isTravellingReverse())
                     .count());
  }

  @Test
  public void createGraphWithFourPointsThreeUnidirectionalAndOneBidirectionalPaths() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, ModelEdge> graph
        = mapper.translateModel(new HashSet<>(Arrays.asList(pointA, pointB, pointC, pointD)),
                                new HashSet<>(Arrays.asList(pathAB, pathBC, pathCD, pathAD)),
                                new Vehicle("someVehicle"));
    assertEquals("Number of vertices", 4, graph.vertexSet().size());
    assertTrue(graph.vertexSet().contains(pointA.getName()));
    assertTrue(graph.vertexSet().contains(pointB.getName()));
    assertTrue(graph.vertexSet().contains(pointC.getName()));
    assertTrue(graph.vertexSet().contains(pointD.getName()));
    assertEquals("Number of edges", 5, graph.edgeSet().size());
    assertEquals("Forward edges for path " + pathAB.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathAB.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals("Forward edges for path " + pathBC.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathBC.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals("Forward edges for path " + pathCD.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathCD.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals("Forward edges for path " + pathAD.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathAD.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals("Reverse edges for path " + pathAD.getName(),
                 1,
                 graph.edgeSet().stream()
                     .filter(edge -> edge.getModelPath().getName().equals(pathAD.getName()))
                     .filter(edge -> edge.isTravellingReverse())
                     .count());
  }

}
