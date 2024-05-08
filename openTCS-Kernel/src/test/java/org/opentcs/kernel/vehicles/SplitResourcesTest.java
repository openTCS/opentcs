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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;

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

  private List<Set<TCSResource<?>>> allResources;

  @BeforeEach
  void setUp() {
    pointA = new Point("A");
    pointB = new Point("B");
    pointC = new Point("C");
    pointD = new Point("D");

    pathAB = new Path("AB", pointA.getReference(), pointB.getReference());
    pathBC = new Path("BC", pointB.getReference(), pointC.getReference());
    pathCD = new Path("CD", pointC.getReference(), pointD.getReference());

    allResources = List.of(Set.of(pathAB, pointB), Set.of(pathBC, pointC), Set.of(pathCD, pointD));
  }

  @Test
  void handleEmptyInput() {
    SplitResources result = SplitResources.from(List.of(), Set.of());

    assertThat(result, is(notNullValue()));
    assertThat(result.getResourcesPassed(), is(empty()));
    assertThat(result.getResourcesAhead(), is(empty()));
  }

  @Test
  void treatAllResourcesAsPassedForNonexistentDelimiter() {
    SplitResources result = SplitResources.from(allResources, Set.of());

    assertThat(result, is(notNullValue()));
    assertThat(result.getResourcesPassed(), is(equalTo(allResources)));
    assertThat(result.getResourcesAhead(), is(empty()));
  }

  @Test
  void treatResourcesAsPassedForCompleteDelimiterSet() {
    SplitResources result = SplitResources.from(allResources, Set.of(pathBC, pointC));

    assertThat(result, is(notNullValue()));
    assertThat(result.getResourcesPassed(),
               contains(Set.of(pathAB, pointB), Set.of(pathBC, pointC)));
    assertThat(result.getResourcesAhead(), contains(Set.of(pathCD, pointD)));
  }

  @Test
  void treatResourcesAsPassedForPartialDelimiterSet() {
    SplitResources result = SplitResources.from(allResources, Set.of(pointC));

    assertThat(result, is(notNullValue()));
    assertThat(result.getResourcesPassed(),
               contains(Set.of(pathAB, pointB), Set.of(pathBC, pointC)));
    assertThat(result.getResourcesAhead(), contains(Set.of(pathCD, pointD)));
  }
}
