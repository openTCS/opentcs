/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Couple;
import org.opentcs.strategies.basic.routing.edgeevaluator.BoundingBoxProtrusionCheck.BoundingBoxProtrusion;

/**
 * Tests for {@link BoundingBoxProtrusionCheck}.
 */
class BoundingBoxProtrusionCheckTest {

  private BoundingBoxProtrusionCheck boundingBoxProtrusionCheck;

  @BeforeEach
  void setUp() {
    boundingBoxProtrusionCheck = new BoundingBoxProtrusionCheck();
  }

  @Test
  void detectProtrusionFromTheFront() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(200, 0));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(-100, 0));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertTrue(result.protrudesFront());
    assertFalse(result.protrudesBack());
    assertFalse(result.protrudesLeft());
    assertFalse(result.protrudesRight());
    assertFalse(result.protrudesTop());
  }

  @Test
  void detectProtrusionFromTheBack() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(-200, 0));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(100, 0));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesFront());
    assertTrue(result.protrudesBack());
    assertFalse(result.protrudesLeft());
    assertFalse(result.protrudesRight());
    assertFalse(result.protrudesTop());
  }

  @Test
  void detectProtrusionFromTheLeft() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(0, 200));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(0, -100));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesFront());
    assertFalse(result.protrudesBack());
    assertTrue(result.protrudesLeft());
    assertFalse(result.protrudesRight());
    assertFalse(result.protrudesTop());
  }

  @Test
  void detectProtrusionFromTheRight() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(0, -200));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(0, 100));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesFront());
    assertFalse(result.protrudesBack());
    assertFalse(result.protrudesLeft());
    assertTrue(result.protrudesRight());
    assertFalse(result.protrudesTop());
  }

  @Test
  void detectProtrusionFromTheTop() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(0, 0));
    BoundingBox inner = new BoundingBox(600, 300, 1100)
        .withReferenceOffset(new Couple(0, 0));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesFront());
    assertFalse(result.protrudesBack());
    assertFalse(result.protrudesLeft());
    assertFalse(result.protrudesRight());
    assertTrue(result.protrudesTop());
  }

  @Test
  void noProtrusionWhenInnerIsFlushWithTheFrontOfOuter() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(200, 0));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(0, 0));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesAnywhere());
  }

  @Test
  void noProtrusionWhenInnerIsFlushWithTheBackOfOuter() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(-200, 0));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(0, 0));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesAnywhere());
  }

  @Test
  void noProtrusionWhenInnerIsFlushWithTheLeftOfOuter() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(0, 200));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(0, 100));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesAnywhere());
  }

  @Test
  void noProtrusionWhenInnerIsFlushWithTheRightOfOuter() {
    BoundingBox outer = new BoundingBox(1000, 500, 1000)
        .withReferenceOffset(new Couple(0, -200));
    BoundingBox inner = new BoundingBox(600, 300, 200)
        .withReferenceOffset(new Couple(0, -100));

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesAnywhere());
  }

  @Test
  void noProtrusionWhenInnerIsEqualToOuter() {
    BoundingBox outer = new BoundingBox(1000, 1000, 1000);
    BoundingBox inner = new BoundingBox(1000, 1000, 1000);

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesAnywhere());
  }

  @Test
  void noProtrusionWhenInnerIsSmallerThanOuter() {
    BoundingBox outer = new BoundingBox(1000, 1000, 1000);
    BoundingBox inner = new BoundingBox(500, 500, 500);

    BoundingBoxProtrusion result = boundingBoxProtrusionCheck.checkProtrusion(inner, outer);

    assertFalse(result.protrudesAnywhere());
  }
}
