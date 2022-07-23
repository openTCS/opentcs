/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.edgeevaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorCompositeTest {

  private static final String EVALUATOR_MOCK = "EVALUATOR_MOCK";
  private static final String EVALUATOR_1 = "EVALUATOR_1";
  private static final String EVALUATOR_2 = "EVALUATOR_2";
  private static final String EVALUATOR_3 = "EVALUATOR_3";

  private Edge edge;
  private Vehicle vehicle;
  private EdgeEvaluator evaluatorMock;
  private ShortestPathConfiguration configuration;
  private final Map<String, EdgeEvaluator> evaluators = new HashMap<>();

  @Before
  public void setUp() {
    Point srcPoint = new Point("srcPoint");
    Point dstPoint = new Point("dstPoint");

    edge = new Edge(new Path("pathName", srcPoint.getReference(), dstPoint.getReference()),
                    true);
    vehicle = new Vehicle("someVehicle");

    configuration = mock(ShortestPathConfiguration.class);

    evaluatorMock = mock(EdgeEvaluator.class);

    evaluators.put(EVALUATOR_MOCK, evaluatorMock);
    evaluators.put(EVALUATOR_1, (someEdge, someVehicle) -> 1.0);
    evaluators.put(EVALUATOR_2, (someEdge, someVehicle) -> 0.9);
    evaluators.put(EVALUATOR_3, (someEdge, someVehicle) -> Double.POSITIVE_INFINITY);
  }

  @Test
  public void notifyOnGraphComputation() {
    when(configuration.edgeEvaluators()).thenReturn(List.of(EVALUATOR_MOCK));
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    verify(evaluatorMock, never()).onGraphComputationStart(vehicle);
    verify(evaluatorMock, never()).onGraphComputationEnd(vehicle);

    edgeEvaluator.onGraphComputationStart(vehicle);
    verify(evaluatorMock).onGraphComputationStart(vehicle);
    verify(evaluatorMock, never()).onGraphComputationEnd(vehicle);

    edgeEvaluator.onGraphComputationEnd(vehicle);
    verify(evaluatorMock).onGraphComputationStart(vehicle);
    verify(evaluatorMock).onGraphComputationEnd(vehicle);
  }

  @Test
  public void computeZeroWithoutComponents() {
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    verifyNoInteractions(evaluatorMock);
    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(0.0));
  }

  @Test
  public void computeSumOfOneComponent() {
    when(configuration.edgeEvaluators()).thenReturn(List.of(EVALUATOR_1));
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    verifyNoInteractions(evaluatorMock);
    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(1.0));
  }

  @Test
  public void computeSumOfTwoComponents() {
    when(configuration.edgeEvaluators()).thenReturn(List.of(EVALUATOR_1, EVALUATOR_2));
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    verifyNoInteractions(evaluatorMock);
    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(1.9));
  }

  @Test
  public void computeInfinityIfAnyComponentReturnsInfinity() {
    when(configuration.edgeEvaluators()).thenReturn(List.of(EVALUATOR_1, EVALUATOR_2, EVALUATOR_3));
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    verifyNoInteractions(evaluatorMock);
    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(Double.POSITIVE_INFINITY));
  }
}
