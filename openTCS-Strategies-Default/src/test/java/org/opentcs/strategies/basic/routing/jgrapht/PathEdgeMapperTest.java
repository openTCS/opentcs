/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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

  private Path pathAB;
  private Path pathBC;

  private Vehicle vehicle;

  private PathEdgeMapper mapper;
  private EdgeEvaluator edgeEvaluator;
  private ShortestPathConfiguration configuration;

  @BeforeEach
  void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");

    pathAB = new Path("A-->B", pointA.getReference(), pointB.getReference())
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(0);
    pathBC = new Path("B<->C", pointB.getReference(), pointC.getReference())
        .withMaxVelocity(1000)
        .withMaxReverseVelocity(1000);

    vehicle = new Vehicle("someVehicle");

    edgeEvaluator = mock();
    when(edgeEvaluator.computeWeight(any(), any())).thenReturn(42.0);
    configuration = mock();
    when(configuration.algorithm()).thenReturn(ShortestPathConfiguration.Algorithm.DIJKSTRA);
    mapper = new PathEdgeMapper(edgeEvaluator, true, configuration);
  }

  @Test
  void translateEmptyPathCollectionToEmptyMap() {
    Map<Edge, Double> result = mapper.translatePaths(new HashSet<>(), vehicle);

    assertThat(result).isEmpty();
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void translateUnidirectionalPathToOneEdge() {
    Map<Edge, Double> result = mapper.translatePaths(Set.of(pathAB), vehicle);

    assertThat(result).hasSize(1);
    assertThat(result)
        .extractingFromEntries(entry -> entry.getKey().getPath(),
                               entry -> entry.getKey().isTravellingReverse(),
                               entry -> entry.getValue())
        .contains(tuple(pathAB, false, 42.0));
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void translateBidirectionalPathToTwoEdges() {
    Map<Edge, Double> result = mapper.translatePaths(Set.of(pathBC), vehicle);

    assertThat(result).hasSize(2);
    assertThat(result)
        .extractingFromEntries(entry -> entry.getKey().getPath(),
                               entry -> entry.getKey().isTravellingReverse(),
                               entry -> entry.getValue())
        .contains(tuple(pathBC, false, 42.0),
                  tuple(pathBC, true, 42.0));
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void translateOneUnidirectionalAndOneBidirectionalPathsToThreeEdges() {
    Map<Edge, Double> result = mapper.translatePaths(Set.of(pathAB, pathBC), vehicle);

    assertThat(result).hasSize(3);
    assertThat(result)
        .extractingFromEntries(entry -> entry.getKey().getPath(),
                               entry -> entry.getKey().isTravellingReverse(),
                               entry -> entry.getValue())
        .contains(tuple(pathAB, false, 42.0),
                  tuple(pathBC, false, 42.0),
                  tuple(pathBC, true, 42.0));
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void excludeLockedPaths() {
    Map<Edge, Double> result = mapper.translatePaths(Set.of(pathAB.withLocked(true)), vehicle);

    assertThat(result).isEmpty();
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }

  @Test
  void includeLockedPaths() {
    mapper = new PathEdgeMapper(edgeEvaluator, false, configuration);

    Map<Edge, Double> result = mapper.translatePaths(Set.of(pathAB.withLocked(true)), vehicle);

    assertThat(result).hasSize(1);
    assertThat(result)
        .extractingFromEntries(entry -> entry.getKey().getPath(),
                               entry -> entry.getKey().isTravellingReverse(),
                               entry -> entry.getValue())
        .contains(tuple(pathAB, false, 42.0));
    verify(edgeEvaluator).onGraphComputationStart(vehicle);
    verify(edgeEvaluator).onGraphComputationEnd(vehicle);
  }
}
