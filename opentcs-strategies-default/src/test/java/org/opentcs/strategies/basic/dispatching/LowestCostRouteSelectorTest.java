// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * Test for {@link LowestCostRouteSelector}.
 */
public class LowestCostRouteSelectorTest {

  private LowestCostRouteSelector routeSelector;


  @BeforeEach
  public void setUp() {
    routeSelector = new LowestCostRouteSelector();
  }

  @Test
  public void selectLowestCostRouteFromSetOfRoutes() {
    Point point1 = new Point("P1");
    Point point2 = new Point("P2");

    Route route1 = new Route(
        List.of(
            new Route.Step(null, null, point1, Vehicle.Orientation.UNDEFINED, 0, 10)
        )
    );
    Route route2 = new Route(
        List.of(
            new Route.Step(null, null, point2, Vehicle.Orientation.UNDEFINED, 0, 5)
        )
    );
    assertThat(routeSelector.select(Set.of(route1, route2)).get(), is(route2));
  }

  @Test
  public void selectLowestCostRouteSequenceFromSetOfRouteSequences() {
    Point pointA = new Point("A");
    Point pointB = new Point("B");
    Point pointC = new Point("C");
    Point pointD = new Point("D");
    Point pointE = new Point("E");
    Point pointF = new Point("F");
    Point pointG = new Point("G");

    Route routeAE = new Route(
        List.of(
            new Route.Step(null, pointA, pointE, Vehicle.Orientation.UNDEFINED, 0, 4)
        )
    );
    Route routeAFE = new Route(
        List.of(
            new Route.Step(null, pointA, pointF, Vehicle.Orientation.UNDEFINED, 0, 1),
            new Route.Step(null, pointF, pointE, Vehicle.Orientation.UNDEFINED, 1, 2)
        )
    );
    Route routeAB = new Route(
        List.of(
            new Route.Step(null, pointA, pointB, Vehicle.Orientation.UNDEFINED, 0, 4)
        )
    );
    Route routeEF = new Route(
        List.of(
            new Route.Step(null, pointE, pointF, Vehicle.Orientation.UNDEFINED, 0, 2)
        )
    );
    Route routeBCG = new Route(
        List.of(
            new Route.Step(null, pointB, pointC, Vehicle.Orientation.UNDEFINED, 0, 1),
            new Route.Step(null, pointC, pointG, Vehicle.Orientation.UNDEFINED, 1, 1)
        )
    );
    Route routeEG = new Route(
        List.of(
            new Route.Step(null, pointE, pointG, Vehicle.Orientation.UNDEFINED, 0, 3)
        )
    );
    Route routeFED = new Route(
        List.of(
            new Route.Step(null, pointF, pointE, Vehicle.Orientation.UNDEFINED, 0, 2),
            new Route.Step(null, pointE, pointD, Vehicle.Orientation.UNDEFINED, 1, 2)
        )
    );

    Route routeGED = new Route(
        List.of(
            new Route.Step(null, pointG, pointE, Vehicle.Orientation.UNDEFINED, 0, 3),
            new Route.Step(null, pointE, pointD, Vehicle.Orientation.UNDEFINED, 1, 2)
        )
    );
    Route routeGCD = new Route(
        List.of(
            new Route.Step(null, pointG, pointC, Vehicle.Orientation.UNDEFINED, 0, 1),
            new Route.Step(null, pointC, pointD, Vehicle.Orientation.UNDEFINED, 1, 2)
        )
    );

    Set<List<Route>> possibleRoutes = Set.of(
        List.of(routeAFE, routeEG, routeGCD),
        List.of(routeAE, routeEF, routeFED),
        List.of(routeAB, routeBCG, routeGED)
    );

    assertThat(
        routeSelector.selectSequence(possibleRoutes),
        is(Optional.of(List.of(routeAFE, routeEG, routeGCD)))
    );
  }
}
