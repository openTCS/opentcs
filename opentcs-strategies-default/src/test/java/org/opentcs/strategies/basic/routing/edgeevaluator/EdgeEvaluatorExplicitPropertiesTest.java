// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.GroupMapper;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 */
class EdgeEvaluatorExplicitPropertiesTest {

  private EdgeEvaluatorExplicitProperties edgeEvaluator;

  private ExplicitPropertiesConfiguration configuration;

  private GroupMapper routingGroupMapper;

  @BeforeEach
  void setUp() {
    configuration = mock(ExplicitPropertiesConfiguration.class);
    routingGroupMapper = mock(GroupMapper.class);
    edgeEvaluator = new EdgeEvaluatorExplicitProperties(configuration, routingGroupMapper);
  }

  @Test
  void extractCorrectProperties() {
    Edge edge = new Edge(
        new Path(
            "pathName",
            new Point("srcPoint").getReference(),
            new Point("dstPoint").getReference()
        )
            .withProperty(Router.PROPKEY_ROUTING_COST_FORWARD + "XYZ", "1234")
            .withProperty(Router.PROPKEY_ROUTING_COST_REVERSE + "XYZ", "5678"),
        false
    );
    Vehicle vehicle = new Vehicle("someVehicle");
    when(routingGroupMapper.apply(vehicle)).thenReturn("XYZ");

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(1234.0));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle), is(5678.0));
  }

  @Test
  void useConfiguredDefaultValue() {
    when(configuration.defaultValue()).thenReturn("123.456");

    Edge edge = new Edge(
        new Path("pathName", new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference()),
        false
    );
    Vehicle vehicle = new Vehicle("someVehicle");
    when(routingGroupMapper.apply(vehicle)).thenReturn("XYZ");

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(123.456));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle), is(123.456));
  }

  @Test
  void handleInvalidDefaultValue() {
    when(configuration.defaultValue()).thenReturn("some invalid value");

    Edge edge = new Edge(
        new Path("pathName", new Point("srcPoint").getReference(),
                 new Point("dstPoint").getReference()),
        false
    );
    Vehicle vehicle = new Vehicle("someVehicle");
    when(routingGroupMapper.apply(vehicle)).thenReturn("XYZ");

    assertThat(edgeEvaluator.computeWeight(edge, vehicle), is(Double.POSITIVE_INFINITY));

    Edge reverseEdge = new Edge(edge.getPath(), true);

    assertThat(edgeEvaluator.computeWeight(reverseEdge, vehicle), is(Double.POSITIVE_INFINITY));
  }
}
