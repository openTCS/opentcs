/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorCompositeTest {

  private ModelEdge edge;

  private Vehicle vehicle;

  private EdgeEvaluatorComposite edgeEvaluator;

  @Before
  public void setUp() {
    Point srcPoint = new Point("srcPoint");
    Point dstPoint = new Point("dstPoint");

    edge = new ModelEdge(new Path("pathName", srcPoint.getReference(), dstPoint.getReference()),
                         true);
    vehicle = new Vehicle("someVehicle");

    edgeEvaluator = new EdgeEvaluatorComposite();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void computeSumOfComponentWeights() {
    assertEquals(0.0, edgeEvaluator.computeWeight(edge, vehicle), 0.0);

    edgeEvaluator.getComponents().add((someEdge, someVehicle) -> 1.0);
    assertEquals(1.0, edgeEvaluator.computeWeight(edge, vehicle), 0.0);

    edgeEvaluator.getComponents().add((someEdge, someVehicle) -> 0.9);
    assertEquals(1.9, edgeEvaluator.computeWeight(edge, vehicle), 0.0);

    edgeEvaluator.getComponents().clear();
    assertEquals(0.0, edgeEvaluator.computeWeight(edge, vehicle), 0.0);
  }

}
