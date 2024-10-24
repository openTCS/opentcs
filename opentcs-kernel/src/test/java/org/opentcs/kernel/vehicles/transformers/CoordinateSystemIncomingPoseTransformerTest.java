// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles.transformers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Triple;

/**
 * Tests for {@link CoordinateSystemIncomingPoseTransformer}.
 */
public class CoordinateSystemIncomingPoseTransformerTest {

  @Test
  public void applyTransformation() {
    CoordinateSystemIncomingPoseTransformer transformer
        = new CoordinateSystemIncomingPoseTransformer(
            new CoordinateSystemTransformation(10, 20, 30, 40)
        );

    assertThat(
        transformer.apply(new Pose(new Triple(0, 0, 0), 40.0)),
        is(equalTo(new Pose(new Triple(-10, -20, -30), 0.0)))
    );
  }

  @ParameterizedTest
  @CsvSource({"380.0,-20.0", "-430.0,70.0"})
  void limitTransformedOrientationAngle(
      double offsetOrientation,
      double expectedTransformedOrientation
  ) {
    CoordinateSystemTransformation transformation = new CoordinateSystemTransformation(
        0,
        0,
        0,
        offsetOrientation
    );
    CoordinateSystemIncomingPoseTransformer transformer
        = new CoordinateSystemIncomingPoseTransformer(transformation);

    assertThat(
        transformer.apply(new Pose(new Triple(0, 0, 0), 0.0)),
        is(equalTo(new Pose(new Triple(0, 0, 0), expectedTransformedOrientation)))
    );
  }
}
