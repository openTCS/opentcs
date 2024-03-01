/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
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
    points = Set.of(pointA, pointB);
    paths = Set.of(
        new Path("A-->B", pointA.getReference(), pointB.getReference())
            .withMaxVelocity(1000)
            .withMaxReverseVelocity(0)
    );
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
  @SuppressWarnings("unchecked")
  void translatePointsAndThenPaths() {
    Graph<String, Edge> graphWithVertices = new DirectedWeightedMultigraph<>(Edge.class);
    when(pointVertexMapper.translatePoints(any(), any())).thenReturn(graphWithVertices);
    Graph<String, Edge> graphWithVerticesAndEdges = new DirectedWeightedMultigraph<>(Edge.class);
    when(pathEdgeMapper.translatePaths(any(), any(), any())).thenReturn(graphWithVerticesAndEdges);

    Graph<String, Edge> result = mapper.translateModel(points, paths, vehicle);

    assertThat(result, is(graphWithVerticesAndEdges));
    verify(pointVertexMapper).translatePoints(eq(points), any(Graph.class));
    verify(pathEdgeMapper).translatePaths(paths, vehicle, graphWithVertices);
  }
}
