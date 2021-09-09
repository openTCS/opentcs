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

  private static final String EVALUATOR_1 = "EVALUATOR_1";
  private static final String EVALUATOR_2 = "EVALUATOR_2";
  private static final String EVALUATOR_3 = "EVALUATOR_3";

  private Edge edge;

  private Vehicle vehicle;

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

    evaluators.put(EVALUATOR_1, (someEdge, someVehicle) -> 1.0);
    evaluators.put(EVALUATOR_2, (someEdge, someVehicle) -> 0.9);
    evaluators.put(EVALUATOR_3, (someEdge, someVehicle) -> Double.POSITIVE_INFINITY);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void computeZeroWithoutComponents() {
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(0.0));
  }

  @Test
  public void computeSumOfOneComponent() {
    when(configuration.edgeEvaluators()).thenReturn(List.of(EVALUATOR_1));
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(1.0));
  }

  @Test
  public void computeSumOfTwoComponents() {
    when(configuration.edgeEvaluators()).thenReturn(List.of(EVALUATOR_1, EVALUATOR_2));
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(1.9));
  }

  @Test
  public void computeInfinityIfAnyComponentReturnsInfinity() {
    when(configuration.edgeEvaluators()).thenReturn(List.of(EVALUATOR_1, EVALUATOR_2, EVALUATOR_3));
    EdgeEvaluatorComposite edgeEvaluator = new EdgeEvaluatorComposite(configuration, evaluators);

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(Double.POSITIVE_INFINITY));
  }
}
