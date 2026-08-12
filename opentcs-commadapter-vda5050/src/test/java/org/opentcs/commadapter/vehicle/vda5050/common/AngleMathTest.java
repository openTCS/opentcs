// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AngleMath}.
 */
public class AngleMathTest {

  @Test
  public void keepConvexAnglesAsTheyAre() {
    assertThat(AngleMath.toRelativeConvexAngle(0.0), is(0.0));
    assertThat(AngleMath.toRelativeConvexAngle(179.9), is(179.9));
    assertThat(AngleMath.toRelativeConvexAngle(-179.9), is(-179.9));
  }

  @Test
  public void mapLargePositiveAnglesToConvexAngles() {
    assertThat(AngleMath.toRelativeConvexAngle(360.0), is(0.0));
    assertThat(AngleMath.toRelativeConvexAngle(270.0), is(-90.0));
  }

  @Test
  public void mapLargeNegativeAnglesToConvexAngles() {
    assertThat(AngleMath.toRelativeConvexAngle(-360.0), is(-0.0));
    assertThat(AngleMath.toRelativeConvexAngle(-270.0), is(90.0));
  }

  @Test
  public void computeAngleBetweenPositiveAngles() {
    assertThat(AngleMath.angleBetween(1.0, 2.0), is(closeTo(1.0, 0.0001)));
    assertThat(AngleMath.angleBetween(2.0, 1.0), is(closeTo(1.0, 0.0001)));
  }

  @Test
  public void computeAngleBetweenNegativeAngles() {
    assertThat(AngleMath.angleBetween(-1.0, -2.0), is(closeTo(1.0, 0.0001)));
    assertThat(AngleMath.angleBetween(-2.0, -1.0), is(closeTo(1.0, 0.0001)));
  }

  @Test
  public void computeAngleBetweenPositiveAndNegativeAngle() {
    assertThat(AngleMath.angleBetween(-1.0, 2.0), is(closeTo(3.0, 0.0001)));
    assertThat(AngleMath.angleBetween(2.0, -1.0), is(closeTo(3.0, 0.0001)));
    assertThat(AngleMath.angleBetween(1.0, -2.0), is(closeTo(3.0, 0.0001)));
    assertThat(AngleMath.angleBetween(-2.0, 1.0), is(closeTo(3.0, 0.0001)));
  }

  @Test
  public void computeLargeAngleBetween() {
    assertThat(AngleMath.angleBetween(359.0, 1.0), is(closeTo(2.0, 0.0001)));
    assertThat(AngleMath.angleBetween(1.0, 359.0), is(closeTo(2.0, 0.0001)));
    assertThat(AngleMath.angleBetween(359.0, -359.0), is(closeTo(2.0, 0.0001)));
    assertThat(AngleMath.angleBetween(-359.0, 359.0), is(closeTo(2.0, 0.0001)));
    assertThat(AngleMath.angleBetween(359.0, -1.0), is(closeTo(0.0, 0.0001)));
    assertThat(AngleMath.angleBetween(-1.0, 359.0), is(closeTo(0.0, 0.0001)));
  }
}
