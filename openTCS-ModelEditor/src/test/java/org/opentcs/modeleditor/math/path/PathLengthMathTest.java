/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PathLengthMath}.
 */
public class PathLengthMathTest {

  private PathLengthMath pathLengthMath;

  @BeforeEach
  void setUp() {
    pathLengthMath = new PathLengthMath();
  }

  @Test
  void approximateCubicBezierCurveLength() {
    double result = pathLengthMath.approximateCubicBezierCurveLength(new Coordinate(10, 10),
                                                                     new Coordinate(20, 30),
                                                                     new Coordinate(40, 50),
                                                                     new Coordinate(60, 60),
                                                                     1000);

    assertThat(result).isEqualTo(71.8, within(0.5));
  }

  @Test
  void testTZeroEqualsStartPointOfCubicBezier() {
    Coordinate result = pathLengthMath
        .evaluatePointOnCubicBezierCurve(0,
                                         new Coordinate(10, 10),
                                         new Coordinate(0.4, -0.6),
                                         new Coordinate(0.8, -1),
                                         new Coordinate(60, 60));

    assertThat(result, is(new Coordinate(10, 10)));
  }

  @Test
  void testTOneEqualsEndPointOfCubicBezier() {
    Coordinate result = pathLengthMath
        .evaluatePointOnCubicBezierCurve(1,
                                         new Coordinate(10, 10),
                                         new Coordinate(0.4, -0.6),
                                         new Coordinate(0.8, -1),
                                         new Coordinate(60, 60));

    assertThat(result, is(new Coordinate(60, 60)));
  }

  @Test
  void testTBetweenZeroAndOneForCubicBezier() {
    Coordinate result = pathLengthMath
        .evaluatePointOnCubicBezierCurve(0.75,
                                         new Coordinate(10, 10),
                                         new Coordinate(0.4, -0.6),
                                         new Coordinate(0.8, -1),
                                         new Coordinate(60, 60));

    assertThat(result.getX(), is(closeTo(25.8625, 0.5)));
    assertThat(result.getY(), is(closeTo(24.9625, 0.5)));
  }

  @Test
  void returnZeroForSamePosition() {
    double distance = pathLengthMath
        .euclideanDistance(new Coordinate(1000, 1000), new Coordinate(1000, 1000));

    assertThat(distance, is(0.0));
  }

  @Test
  void returnDistanceXForSameY() {
    double distance = pathLengthMath
        .euclideanDistance(new Coordinate(2000, 1000), new Coordinate(4500, 1000));

    assertThat(distance, is(2500.0));
  }

  @Test
  void returnDistanceYForSameX() {
    double distance = pathLengthMath
        .euclideanDistance(new Coordinate(1000, 3000), new Coordinate(1000, 7654));

    assertThat(distance, is(4654.0));
  }

  @Test
  void returnEuclideanDistance() {
    double distance = pathLengthMath
        .euclideanDistance(new Coordinate(2000, 1000), new Coordinate(4000, 5000));

    assertThat(distance, is(closeTo(4472.0, 0.5)));
  }
}
