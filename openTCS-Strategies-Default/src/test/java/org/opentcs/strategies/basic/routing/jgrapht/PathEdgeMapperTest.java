/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.HashSet;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link PathEdgeMapper}.
 */
class PathEdgeMapperTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Point pointD;

  private Path pathAB;
  private Path pathBC;
  private Path pathCD;
  private Path pathAD;

  private Vehicle vehicle;

  private Graph<String, Edge> graph;

  private PathEdgeMapper mapper;
  private EdgeEvaluator edgeEvaluator;
  private ShortestPathConfiguration configuration;

  @BeforeEach
  void setUp() {
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

    vehicle = new Vehicle("someVehicle");

    graph = createGraphWithVertices("A", "B", "C", "D");

    edgeEvaluator = mock();
    configuration = mock();
    mapper = new PathEdgeMapper(edgeEvaluator, true, configuration);
  }

  @Test
  void dontManipulateGraph() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, Edge> result = mapper.translatePaths(new HashSet<>(), vehicle, graph);

    assertEquals(4, result.vertexSet().size());
    assertTrue(result.vertexSet().contains("A"));
    assertTrue(result.vertexSet().contains("B"));
    assertTrue(result.vertexSet().contains("C"));
    assertTrue(result.vertexSet().contains("D"));
    assertEquals(0, result.edgeSet().size());
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void extendGraphByOneUnidirectionalPath() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, Edge> result = mapper.translatePaths(Set.of(pathAB), vehicle, graph);

    assertEquals(4, result.vertexSet().size());
    assertEquals(1, result.edgeSet().size());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAB.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void extendGraphByOneBidirectionalPath() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, Edge> result = mapper.translatePaths(Set.of(pathAD), vehicle, graph);

    assertEquals(4, result.vertexSet().size());
    assertEquals(2, result.edgeSet().size());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAD.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAD.getName()))
                     .filter(edge -> edge.isTravellingReverse())
                     .count());
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void extendGraphByThreeUnidirectionalAndOneBidirectionalPaths() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, Edge> result = mapper.translatePaths(Set.of(pathAB, pathBC, pathCD, pathAD),
                                                       vehicle,
                                                       graph);

    assertEquals(4, result.vertexSet().size());
    assertEquals(5, result.edgeSet().size());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAB.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathBC.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathCD.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAD.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAD.getName()))
                     .filter(edge -> edge.isTravellingReverse())
                     .count());
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void excludeLockedPaths() {
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, Edge> result = mapper.translatePaths(Set.of(pathAD.withLocked(true)),
                                                       vehicle,
                                                       graph);

    assertEquals(4, result.vertexSet().size());
    assertEquals(0, result.edgeSet().size());
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void includeLockedPaths() {
    mapper = new PathEdgeMapper(edgeEvaluator, false, configuration);

    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);

    Graph<String, Edge> result = mapper.translatePaths(Set.of(pathAD.withLocked(true)),
                                                       vehicle,
                                                       graph);

    assertEquals(4, result.vertexSet().size());
    assertEquals(2, result.edgeSet().size());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAD.getName()))
                     .filter(edge -> !edge.isTravellingReverse())
                     .count());
    assertEquals(1,
                 result.edgeSet().stream()
                     .filter(edge -> edge.getPath().getName().equals(pathAD.getName()))
                     .filter(edge -> edge.isTravellingReverse())
                     .count());
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  private Graph<String, Edge> createGraphWithVertices(String... vertices) {
    Graph<String, Edge> g = new DirectedWeightedMultigraph<>(Edge.class);
    for (String vertex : vertices) {
      g.addVertex(vertex);
    }
    return g;
  }
}
