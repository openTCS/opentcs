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
 * Tests for {@link CsmIncomingPoseTransformer}.
 */
public class CsmIncomingPoseTransformerTest {

  @Test
  public void applyTranslation() {
    CsmIncomingPoseTransformer transformer
        = new CsmIncomingPoseTransformer(
            new CoordinateSystemMapping(10, 20, 30, 0)
        );

    assertThat(
        transformer.apply(new Pose(new Triple(0, 0, 0), 45.0)),
        is(equalTo(new Pose(new Triple(10, 20, 30), 45.0)))
    );
  }

  @Test
  public void applyRotation() {
    CsmIncomingPoseTransformer transformer
        = new CsmIncomingPoseTransformer(
            new CoordinateSystemMapping(0, 0, 0, 90)
        );

    assertThat(
        transformer.apply(new Pose(new Triple(10, 20, 30), 45.0)),
        is(equalTo(new Pose(new Triple(-20, 10, 30), 135.0)))
    );
  }

  @Test
  public void applyTranslationAndRotation() {
    CsmIncomingPoseTransformer transformer
        = new CsmIncomingPoseTransformer(
            new CoordinateSystemMapping(10, 20, 30, 90)
        );

    assertThat(
        transformer.apply(new Pose(new Triple(40, -30, 50), 40.0)),
        is(equalTo(new Pose(new Triple(40, 60, 80), 130.0)))
    );
  }

  @ParameterizedTest
  @CsvSource({"450.0,-10,20, 90.0", "-405.0,21,-7,-45.0"})
  void limitTransformedOrientationAngle(
      double rotationZ,
      long expectedTransformedX,
      long expectedTransformedY,
      double expectedTransformedTheta
  ) {
    CoordinateSystemMapping mapping = new CoordinateSystemMapping(
        0,
        0,
        0,
        rotationZ
    );
    CsmIncomingPoseTransformer transformer
        = new CsmIncomingPoseTransformer(mapping);

    assertThat(
        transformer.apply(new Pose(new Triple(20, 10, 40), 0.0)),
        is(
            equalTo(
                new Pose(
                    new Triple(expectedTransformedX, expectedTransformedY, 40),
                    expectedTransformedTheta
                )
            )
        )
    );
  }
}
