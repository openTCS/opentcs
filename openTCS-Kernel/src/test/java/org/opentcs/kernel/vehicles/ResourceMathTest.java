/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.List;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;

/**
 * Tests for {@link ResourceMath}.
 */
class ResourceMathTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Point pointD;

  private Path pathAB;
  private Path pathBC;
  private Path pathCD;

  private List<Set<TCSResource<?>>> allResources;

  @BeforeEach
  void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");
    pointD = new Point("D");

    pathAB = new Path("AB", pointA.getReference(), pointB.getReference()).withLength(3000);
    pathBC = new Path("BC", pointB.getReference(), pointC.getReference()).withLength(2000);
    pathCD = new Path("CD", pointC.getReference(), pointD.getReference()).withLength(1000);

    allResources = List.of(Set.of(pathAB, pointB), Set.of(pathBC, pointC), Set.of(pathCD, pointD));
  }

  @Test
  void handleVehicleCoveringLastResourceSet() {
    assertThat(ResourceMath.freeableResourceSetCount(allResources, 500), is(2));
  }

  @Test
  void handleVehicleCoveringLastResourceSetExactly() {
    assertThat(ResourceMath.freeableResourceSetCount(allResources, 1000), is(2));
  }

  @Test
  void handleVehicleCoveringLastTwoResourceSets() {
    assertThat(ResourceMath.freeableResourceSetCount(allResources, 1001), is(1));
  }

  @Test
  void handleVehicleCoveringLastThreeResourceSets() {
    assertThat(ResourceMath.freeableResourceSetCount(allResources, 3001), is(0));
  }

  @Test
  void handleVehicleCoveringMoreResourceSetsThanGiven() {
    assertThat(ResourceMath.freeableResourceSetCount(allResources, Integer.MAX_VALUE), is(0));
  }

}
