/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange;

import java.util.List;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;

/**
 * Tests for {@link SplitResources}.
 */
class SplitResourcesTest {

  private Point pointA;
  private Point pointB;
  private Point pointC;
  private Point pointD;

  private Path pathAB;
  private Path pathBC;
  private Path pathCD;

  private List<Set<TCSResourceReference<?>>> allResources;

  @BeforeEach
  void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");
    pointD = new Point("D");

    pathAB = new Path("AB", pointA.getReference(), pointB.getReference());
    pathBC = new Path("BC", pointB.getReference(), pointC.getReference());
    pathCD = new Path("CD", pointC.getReference(), pointD.getReference());

    allResources = List.of(Set.of(pathAB.getReference(), pointB.getReference()),
                           Set.of(pathBC.getReference(), pointC.getReference()),
                           Set.of(pathCD.getReference(), pointD.getReference()));
  }

  @Test
  void handleEmptyResourceSets() {
    SplitResources result = SplitResources.from(List.of(), pointB.getReference());

    assertThat(result, is(notNullValue()));
    assertThat(result.getAllocatedResourcesBehind(), is(empty()));
    assertThat(result.getAllocatedResourcesAhead(), is(empty()));
  }

  @Test
  void treatAllResourcesAsBehindForNullDelimiter() {
    SplitResources result = SplitResources.from(allResources, null);

    assertThat(result, is(notNullValue()));
    assertThat(result.getAllocatedResourcesBehind(), is(equalTo(allResources)));
    assertThat(result.getAllocatedResourcesAhead(), is(empty()));
  }

  @Test
  void treatDelimiterAsBehind() {
    SplitResources result = SplitResources.from(allResources, pointC.getReference());

    assertThat(result, is(notNullValue()));
    assertThat(result.getAllocatedResourcesBehind(), is(equalTo(List.of(
               Set.of(pathAB.getReference(), pointB.getReference()),
               Set.of(pathBC.getReference(), pointC.getReference())))));
    assertThat(result.getAllocatedResourcesAhead(), is(equalTo(List.of(
                   Set.of(pathCD.getReference(), pointD.getReference())))));
  }
}
