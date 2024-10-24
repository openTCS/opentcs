// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.edgeevaluator;

import org.opentcs.data.model.BoundingBox;

/**
 * Provides a method for checking whether one bounding box protrudes beyond another one.
 */
public class BoundingBoxProtrusionCheck {

  public BoundingBoxProtrusionCheck() {
  }

  /**
   * Checks whether one (inner) bounding box protrudes beyond another (outer) one.
   *
   * @param inner The inner bounding box.
   * @param outer The outer bounding box.
   * @return The result of the check, indicating where and how much the inner bounding box protrudes
   * beyond the outer one.
   */
  public BoundingBoxProtrusion checkProtrusion(BoundingBox inner, BoundingBox outer) {
    return new BoundingBoxProtrusion(
        (inner.getLength() / 2.0 - inner.getReferenceOffset().getX()) -
            (outer.getLength() / 2.0 - outer.getReferenceOffset().getX()),
        (inner.getLength() / 2.0 + inner.getReferenceOffset().getX()) -
            (outer.getLength() / 2.0 + outer.getReferenceOffset().getX()),
        (inner.getWidth() / 2.0 - inner.getReferenceOffset().getY()) -
            (outer.getWidth() / 2.0 - outer.getReferenceOffset().getY()),
        (inner.getWidth() / 2.0 + inner.getReferenceOffset().getY()) -
            (outer.getWidth() / 2.0 + outer.getReferenceOffset().getY()),
        inner.getHeight() - outer.getHeight()
    );
  }

  /**
   * Describes where and how much an inner bounding box protrudes beyond an outer one.
   */
  public static class BoundingBoxProtrusion {

    private final double front;
    private final double back;
    private final double left;
    private final double right;
    private final double top;

    /**
     * Creates a new instance.
     *
     * @param front The protrusion from the front.
     * @param back The protrusion from the back.
     * @param left The protrusion from the left.
     * @param right The protrusion from the right.
     * @param top The protrusion from the top.
     */
    public BoundingBoxProtrusion(double front, double back, double left, double right, double top) {
      this.front = Math.max(0, front);
      this.back = Math.max(0, back);
      this.left = Math.max(0, left);
      this.right = Math.max(0, right);
      this.top = Math.max(0, top);
    }

    /**
     * Indicates whether there is a protrusion from the front.
     *
     * @return {@code true}, if there is a protrusion from the front, otherwise {@code false}.
     */
    public boolean protrudesFront() {
      return front > 0;
    }

    /**
     * Indicates whether there is a protrusion from the back.
     *
     * @return {@code true}, if there is a protrusion from the back, otherwise {@code false}.
     */
    public boolean protrudesBack() {
      return back > 0;
    }

    /**
     * Indicates whether there is a protrusion from the left.
     *
     * @return {@code true}, if there is a protrusion from the left, otherwise {@code false}.
     */
    public boolean protrudesLeft() {
      return left > 0;
    }

    /**
     * Indicates whether there is a protrusion from the right.
     *
     * @return {@code true}, if there is a protrusion from the right, otherwise {@code false}.
     */
    public boolean protrudesRight() {
      return right > 0;
    }

    /**
     * Indicates whether there is a protrusion from the top.
     *
     * @return {@code true}, if there is a protrusion from the top, otherwise {@code false}.
     */
    public boolean protrudesTop() {
      return top > 0;
    }

    /**
     * Indicates whether there is a protrusion anywhere (i.e. from the front, back, left, right or
     * top).
     *
     * @return {@code true}, if there is a protrusion anywhere, otherwise {@code false}.
     */
    public boolean protrudesAnywhere() {
      return protrudesFront() || protrudesBack()
          || protrudesLeft() || protrudesRight()
          || protrudesTop();
    }
  }
}
