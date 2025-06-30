// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    mapper = new DefaultModelGraphMapper(
        mock(EdgeEvaluatorComposite.class),
        mapperComponentsFactory
    );

  }

  @Test
  void translateModelToGraph() {
    when(pointVertexMapper.translatePoints(any())).thenReturn(
        Set.of(
            new Vertex(new Point("A").getReference()),
            new Vertex(new Point("B").getReference()),
            new Vertex(new Point("C").getReference())
        )
    );
    Edge edgeAB = new Edge(pathAB, false);
    Edge edgeBC = new Edge(pathBC, false);
    when(pathEdgeMapper.translatePaths(any(), any()))
        .thenReturn(Map.of(edgeAB, 42.0, edgeBC, 29.0));

    Graph<Vertex, Edge> result = mapper.translateModel(points, paths, vehicle);

    assertThat(result.vertexSet())
        .map(vertex -> vertex.getPoint().getName())
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
    Graph<Vertex, Edge> originalGraph = new DirectedWeightedMultigraph<>(Edge.class);
    Vertex a = new Vertex(new Point("A").getReference());
    Vertex b = new Vertex(new Point("B").getReference());
    Vertex c = new Vertex(new Point("C").getReference());
    originalGraph.addVertex(a);
    originalGraph.addVertex(b);
    originalGraph.addVertex(c);
    Edge edgeAB = new Edge(pathAB, false);
    originalGraph.addEdge(a, b, edgeAB);
    originalGraph.setEdgeWeight(edgeAB, 42.0);
    Edge edgeBC = new Edge(pathBC, false);
    originalGraph.addEdge(b, c, edgeBC);
    originalGraph.setEdgeWeight(edgeBC, 29.0);

    when(pathEdgeMapper.translatePaths(any(), any())).thenReturn(Map.of(edgeAB, 79.0));
    Set<Path> changedPaths = Set.of(pathAB);

    Graph<Vertex, Edge> result = mapper.updateGraph(changedPaths, vehicle, originalGraph);

    // Assert that the output graph contains the same vertices and edges but the weight of one of
    // the edges was updated.
    assertThat(result.vertexSet())
        .hasSize(3)
        .contains(a, b, c);
    assertThat(result.edgeSet())
        .hasSize(2)
        .contains(edgeAB, edgeBC);
    assertThat(result.getEdgeWeight(edgeAB)).isEqualTo(79.0);
    assertThat(result.getEdgeWeight(edgeBC)).isEqualTo(29.0);
    verify(pathEdgeMapper).translatePaths(changedPaths, vehicle);
  }
}
