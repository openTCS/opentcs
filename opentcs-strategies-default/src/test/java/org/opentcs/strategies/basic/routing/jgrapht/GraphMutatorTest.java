// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link GraphMutator}.
 */
class GraphMutatorTest {

  private Vehicle vehicle;
  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Path pathAB;
  private Path pathBC;
  private Path pathCA;
  private Graph<Vertex, Edge> graph;
  private Vertex vertexA;
  private Vertex vertexB;
  private Vertex vertexC;
  private Edge edgeAB;
  private Edge edgeBC;
  private Edge edgeCA;
  private GraphMutator graphMutator;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle("vehicle");

    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");
    pathAB = new Path("A --- B", pointA.getReference(), pointB.getReference());
    pathBC = new Path("B --- C", pointB.getReference(), pointC.getReference());
    pathCA = new Path("C --- A", pointC.getReference(), pointA.getReference());

    graph = new DirectedWeightedMultigraph<>(Edge.class);
    vertexA = new Vertex(pointA.getReference());
    vertexB = new Vertex(pointB.getReference());
    vertexC = new Vertex(pointC.getReference());
    edgeAB = new Edge(pathAB, false);
    edgeBC = new Edge(pathBC, false);
    edgeCA = new Edge(pathCA, false);
    graph.addVertex(vertexA);
    graph.addVertex(vertexB);
    graph.addVertex(vertexC);
    graph.addEdge(vertexA, vertexB, edgeAB);
    graph.addEdge(vertexB, vertexC, edgeBC);
    graph.addEdge(vertexC, vertexA, edgeCA);

    graphMutator = new GraphMutator();
  }

  @Test
  void returnsOriginalGraphWhenNothingToExclude() {
    GraphProvider.GraphResult originalGraphResult = new GraphProvider.GraphResult(
        vehicle,
        Set.of(pointA, pointB, pointC),
        Set.of(pathAB, pathBC, pathCA),
        Set.of(),
        Set.of(),
        graph
    );

    GraphProvider.GraphResult derivedGraphResult
        = graphMutator.deriveGraph(Set.of(), Set.of(), originalGraphResult);

    assertThat(derivedGraphResult.getVehicle()).isEqualTo(vehicle);
    assertThat(derivedGraphResult.getPointBase()).containsExactlyInAnyOrder(pointA, pointB, pointC);
    assertThat(derivedGraphResult.getPathBase()).containsExactlyInAnyOrder(pathAB, pathBC, pathCA);
    assertThat(derivedGraphResult.getExcludedPoints()).isEmpty();
    assertThat(derivedGraphResult.getExcludedPaths()).isEmpty();
    assertThat(derivedGraphResult.getGraph().vertexSet())
        .containsExactlyInAnyOrder(vertexA, vertexB, vertexC);
    assertThat(derivedGraphResult.getGraph().edgeSet())
        .containsExactlyInAnyOrder(edgeAB, edgeBC, edgeCA);
  }

  @Test
  void providedPointsToExcludeResultsInVerticesAndConnectedEdgesBeingRemoved() {
    GraphProvider.GraphResult originalGraphResult = new GraphProvider.GraphResult(
        vehicle,
        Set.of(pointA, pointB, pointC),
        Set.of(pathAB, pathBC, pathCA),
        Set.of(),
        Set.of(),
        graph
    );

    GraphProvider.GraphResult derivedGraphResult
        = graphMutator.deriveGraph(Set.of(pointA), Set.of(), originalGraphResult);

    assertThat(derivedGraphResult.getVehicle()).isEqualTo(vehicle);
    assertThat(derivedGraphResult.getPointBase()).containsExactlyInAnyOrder(pointA, pointB, pointC);
    assertThat(derivedGraphResult.getPathBase()).containsExactlyInAnyOrder(pathAB, pathBC, pathCA);
    assertThat(derivedGraphResult.getExcludedPoints()).containsExactlyInAnyOrder(pointA);
    assertThat(derivedGraphResult.getExcludedPaths()).isEmpty();
    assertThat(derivedGraphResult.getGraph().vertexSet())
        .containsExactlyInAnyOrder(vertexB, vertexC);
    assertThat(derivedGraphResult.getGraph().edgeSet())
        .containsExactlyInAnyOrder(edgeBC);
  }

  @Test
  void providedPathsToExcludeResultsInEdgesBeingRemoved() {
    GraphProvider.GraphResult originalGraphResult = new GraphProvider.GraphResult(
        vehicle,
        Set.of(pointA, pointB, pointC),
        Set.of(pathAB, pathBC, pathCA),
        Set.of(),
        Set.of(pathBC),
        graph
    );

    GraphProvider.GraphResult derivedGraphResult
        = graphMutator.deriveGraph(Set.of(), Set.of(pathBC), originalGraphResult);

    assertThat(derivedGraphResult.getVehicle()).isEqualTo(vehicle);
    assertThat(derivedGraphResult.getPointBase()).containsExactlyInAnyOrder(pointA, pointB, pointC);
    assertThat(derivedGraphResult.getPathBase()).containsExactlyInAnyOrder(pathAB, pathBC, pathCA);
    assertThat(derivedGraphResult.getExcludedPoints()).isEmpty();
    assertThat(derivedGraphResult.getExcludedPaths()).containsExactlyInAnyOrder(pathBC);
    assertThat(derivedGraphResult.getGraph().vertexSet())
        .containsExactlyInAnyOrder(vertexA, vertexB, vertexC);
    assertThat(derivedGraphResult.getGraph().edgeSet())
        .containsExactlyInAnyOrder(edgeAB, edgeCA);
  }
}
