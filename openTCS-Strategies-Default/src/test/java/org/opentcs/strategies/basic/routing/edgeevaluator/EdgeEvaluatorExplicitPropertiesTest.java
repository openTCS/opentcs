/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorExplicitPropertiesTest {

  private EdgeEvaluatorExplicitProperties edgeEvaluator;

  private ExplicitPropertiesConfiguration configuration;

  @Before
  public void setUp() {
    configuration = mock(ExplicitPropertiesConfiguration.class);
    edgeEvaluator = new EdgeEvaluatorExplicitProperties(configuration);
  }

  @Test
  public void extractCorrectProperties() {
    Edge edge = new Edge(
        new Path("pathName",
                 new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference())
            .withProperty(Router.PROPKEY_ROUTING_COST_FORWARD + "XYZ", "1234")
            .withProperty(Router.PROPKEY_ROUTING_COST_REVERSE + "XYZ", "5678"),
        false
    );
    Vehicle vehicle = new Vehicle("someVehicle")
        .withProperty(Router.PROPKEY_ROUTING_GROUP, "XYZ");

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(1234.0));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle), is(5678.0));
  }

  @Test
  public void useConfiguredDefaultValue() {
    when(configuration.defaultValue()).thenReturn("123.456");

    Edge edge = new Edge(
        new Path("pathName", new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference()),
        false
    );
    Vehicle vehicle = new Vehicle("someVehicle")
        .withProperty(Router.PROPKEY_ROUTING_GROUP, "XYZ");

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(123.456));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle), is(123.456));
  }

  @Test
  public void handleInvalidDefaultValue() {
    when(configuration.defaultValue()).thenReturn("some invalid value");

    Edge edge = new Edge(
        new Path("pathName", new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference()),
        false
    );
    Vehicle vehicle = new Vehicle("someVehicle")
        .withProperty(Router.PROPKEY_ROUTING_GROUP, "XYZ");

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(Double.POSITIVE_INFINITY));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle), is(Double.POSITIVE_INFINITY));
  }
}
