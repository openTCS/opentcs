/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

import java.io.Serializable;

/**
 * A bounding box that can be used, for example, to describe an object's physical dimensions.
 * <p>
 * A bounding box is characterised by a reference point that is located in the center of the
 * bounding box's base (i.e. at height 0). Therefore, the length and width of the bounding box are
 * symmetrical in relation to the reference point and the height is measured from the base of the
 * bounding box.
 * Additionally, an offset to the reference point describes the position of the bounding box in
 * relation to another point. This is useful when describing the bounding box of an object whose
 * reference point is not at its geometric center. The coordinates of the reference offset refer to
 * a coordinate system whose origin is located at the bounding box's reference point and whose axes
 * run along the longitudinal and transverse axes of the bounding box (i.e. the x-coordinate of the
 * reference offset runs along the length and the y-coordinate along the width of the bounding box).
 * </p>
 */
public class BoundingBox
    implements
      Serializable {

  private final long length;
  private final long width;
  private final long height;
  private final Couple referenceOffset;

  /**
   * Creates a new instance with a (0, 0) reference offset.
   *
   * @param length The bounding box's length.
   * @param width The bounding box's width.
   * @param height The bounding box's height.
   */
  public BoundingBox(long length, long width, long height) {
    this(length, width, height, new Couple(0, 0));
  }

  private BoundingBox(long length, long width, long height, Couple referenceOffset) {
    this.length = checkInRange(length, 1, Long.MAX_VALUE, "length");
    this.width = checkInRange(width, 1, Long.MAX_VALUE, "width");
    this.height = checkInRange(height, 1, Long.MAX_VALUE, "height");
    this.referenceOffset = requireNonNull(referenceOffset, "referenceOffset");
  }

  /**
   * Returns the bounding box's length.
   *
   * @return The bounding box's length.
   */
  public long getLength() {
    return length;
  }

  /**
   * Creates a copy of this object, with the given length.
   *
   * @param length The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public BoundingBox withLength(long length) {
    return new BoundingBox(length, width, height, referenceOffset);
  }

  /**
   * Returns the bounding box's width.
   *
   * @return The bounding box's width.
   */
  public long getWidth() {
    return width;
  }

  /**
   * Creates a copy of this object, with the given width.
   *
   * @param width The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public BoundingBox withWidth(long width) {
    return new BoundingBox(length, width, height, referenceOffset);
  }

  /**
   * Returns the bounding box's height.
   *
   * @return The bounding box's height.
   */
  public long getHeight() {
    return height;
  }

  /**
   * Creates a copy of this object, with the given height.
   *
   * @param height The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public BoundingBox withHeight(long height) {
    return new BoundingBox(length, width, height, referenceOffset);
  }

  /**
   * Returns the bounding box's reference offset.
   *
   * @return The bounding box's reference offset.
   */
  public Couple getReferenceOffset() {
    return referenceOffset;
  }

  /**
   * Creates a copy of this object, with the given reference offset.
   *
   * @param referenceOffset The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public BoundingBox withReferenceOffset(Couple referenceOffset) {
    return new BoundingBox(length, width, height, referenceOffset);
  }

  @Override
  public String toString() {
    return "BoundingBox{" +
        "length=" + length +
        ", width=" + width +
        ", height=" + height +
        ", referenceOffset=" + referenceOffset +
        '}';
  }
}
