/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorExplicitPropertiesTest {

  private EdgeEvaluatorExplicitProperties edgeEvaluator;

  @Before
  public void setUp() {
    edgeEvaluator = new EdgeEvaluatorExplicitProperties();
  }

  @Test
  public void extractCorrectProperties() {
    ModelEdge edge = new ModelEdge(
        new Path("pathName",
                 new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference())
            .withProperty(Router.PROPKEY_ROUTING_COST_FORWARD + "XYZ", "1234")
            .withProperty(Router.PROPKEY_ROUTING_COST_REVERSE + "XYZ", "5678"),
        false
    );
    Vehicle vehicle = new Vehicle("someVehicle")
        .withProperty(Router.PROPKEY_ROUTING_GROUP, "XYZ");

    assertEquals(1234.0, edgeEvaluator.computeWeight(edge, vehicle), 0.0);

    ModelEdge reverseEdge = new ModelEdge(edge.getModelPath(), true);

    assertEquals(5678.0, edgeEvaluator.computeWeight(reverseEdge, vehicle), 0.0);
  }

}
