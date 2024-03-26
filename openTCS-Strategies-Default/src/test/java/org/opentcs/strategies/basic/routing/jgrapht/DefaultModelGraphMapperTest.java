/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorComposite;

/**
 * Tests for {@link DefaultModelGraphMapper}.
 */
class DefaultModelGraphMapperTest {

  private Set<Point> points;
  private Path pathAB;
  private Path pathBC;
  private Set<Path> paths;

  private Vehicle vehicle;

  private MapperComponentsFactory mapperComponentsFactory;
  private PointVertexMapper pointVertexMapper;
  private PathEdgeMapper pathEdgeMapper;
  private DefaultModelGraphMapper mapper;

  @BeforeEach
  void setUp() {
    Point pointA = new Point("A");
    Point pointB = new Point("B");
    Point pointC = new Point("C");
    points = Set.of(pointA, pointB, pointC);
    pathAB = new Path("A-->B", pointA.getReference(), pointB.getReference());
    pathBC = new Path("B-->C", pointB.getReference(), pointC.getReference());
    paths = Set.of(pathAB, pathBC);
    vehicle = new Vehicle("someVehicle");

    mapperComponentsFactory = mock(MapperComponentsFactory.class);
    pointVertexMapper = mock(PointVertexMapper.class);
    when(mapperComponentsFactory.createPointVertexMapper())
        .thenReturn(pointVertexMapper);
    pathEdgeMapper = mock(PathEdgeMapper.class);
    when(mapperComponentsFactory.createPathEdgeMapper(any(EdgeEvaluator.class), anyBoolean()))
        .thenReturn(pathEdgeMapper);
    mapper = new DefaultModelGraphMapper(mock(EdgeEvaluatorComposite.class),
                                         mapperComponentsFactory);

  }

  @Test
  void translateModelToGraph() {
    when(pointVertexMapper.translatePoints(any())).thenReturn(Set.of("A", "B", "C"));
    Edge edgeAB = new Edge(pathAB, false);
    Edge edgeBC = new Edge(pathBC, false);
    when(pathEdgeMapper.translatePaths(any(), any()))
        .thenReturn(Map.of(edgeAB, 42.0, edgeBC, 29.0));

    Graph<String, Edge> result = mapper.translateModel(points, paths, vehicle);

    assertThat(result.vertexSet())
        .hasSize(3)
        .contains("A", "B", "C");
    assertThat(result.edgeSet())
        .hasSize(2)
        .contains(edgeAB, edgeBC);
    assertThat(result.getEdgeWeight(edgeAB)).isEqualTo(42.0);
    assertThat(result.getEdgeWeight(edgeBC)).isEqualTo(29.0);
    verify(pointVertexMapper).translatePoints(points);
    verify(pathEdgeMapper).translatePaths(paths, vehicle);
  }

  @Test
  void updateGraphWithChangedPaths() {
    // Build the input graph with one path/edge that is expected to be updated.
    Graph<String, Edge> originalGraph = new DirectedWeightedMultigraph<>(Edge.class);
    originalGraph.addVertex("A");
    originalGraph.addVertex("B");
    originalGraph.addVertex("C");
    Edge edgeAB = new Edge(pathAB, false);
    originalGraph.addEdge("A", "B", edgeAB);
    originalGraph.setEdgeWeight(edgeAB, 42.0);
    Edge edgeBC = new Edge(pathBC, false);
    originalGraph.addEdge("B", "C", edgeBC);
    originalGraph.setEdgeWeight(edgeBC, 29.0);

    when(pathEdgeMapper.translatePaths(any(), any())).thenReturn(Map.of(edgeAB, 79.0));
    Set<Path> changedPaths = Set.of(pathAB);

    Graph<String, Edge> result = mapper.updateGraph(changedPaths, vehicle, originalGraph);

    // Assert that the output graph contains the same vertices and edges but the weight of one of
    // the edges was updated.
    assertThat(result.vertexSet())
        .hasSize(3)
        .contains("A", "B", "C");
    assertThat(result.edgeSet())
        .hasSize(2)
        .contains(edgeAB, edgeBC);
    assertThat(result.getEdgeWeight(edgeAB)).isEqualTo(79.0);
    assertThat(result.getEdgeWeight(edgeBC)).isEqualTo(29.0);
    verify(pathEdgeMapper).translatePaths(changedPaths, vehicle);
  }
}
