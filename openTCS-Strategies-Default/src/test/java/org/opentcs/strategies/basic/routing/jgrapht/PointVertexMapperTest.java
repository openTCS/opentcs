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
import org.jgrapht.graph.DirectedWeightedMultigraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Point;

/**
 * Tests for {@link PointVertexMapper}.
 */
class PointVertexMapperTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Point pointD;

  private Graph<String, Edge> emptyGraph;

  private PointVertexMapper mapper;

  @BeforeEach
  void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");
    pointD = new Point("D");

    emptyGraph = new DirectedWeightedMultigraph<>(Edge.class);

    mapper = new PointVertexMapper();
  }

  @Test
  void dontManipulateGraph() {
    Graph<String, Edge> result = mapper.translatePoints(new HashSet<>(), emptyGraph);

    assertEquals(0, result.vertexSet().size());
    assertEquals(0, result.edgeSet().size());
  }

  @Test
  void createGraphWithFourPointsAndNoPath() {
    Graph<String, Edge> graph
        = mapper.translatePoints(new HashSet<>(Arrays.asList(pointA, pointB, pointC, pointD)),
                                 emptyGraph);

    assertEquals(4, graph.vertexSet().size());
    assertTrue(graph.vertexSet().contains(pointA.getName()));
    assertTrue(graph.vertexSet().contains(pointB.getName()));
    assertTrue(graph.vertexSet().contains(pointC.getName()));
    assertTrue(graph.vertexSet().contains(pointD.getName()));
    assertEquals(0, graph.edgeSet().size());
  }
}
