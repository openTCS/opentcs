// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

import java.io.Serializable;

/**
 * A transfer object describing a bounding box.
 */
public class BoundingBoxCreationTO
    implements
      Serializable {

  private final long length;
  private final long width;
  private final long height;
  private final CoupleCreationTO referenceOffset;

  /**
   * Creates a new instance with a (0, 0) reference offset.
   *
   * @param length The bounding box's length.
   * @param width The bounding box's width.
   * @param height The bounding box's height.
   */
  public BoundingBoxCreationTO(long length, long width, long height) {
    this(length, width, height, new CoupleCreationTO(0, 0));
  }

  private BoundingBoxCreationTO(
      long length,
      long width,
      long height,
      CoupleCreationTO referenceOffset
  ) {
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
  public BoundingBoxCreationTO withLength(long length) {
    return new BoundingBoxCreationTO(length, width, height, referenceOffset);
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
  public BoundingBoxCreationTO withWidth(long width) {
    return new BoundingBoxCreationTO(length, width, height, referenceOffset);
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
  public BoundingBoxCreationTO withHeight(long height) {
    return new BoundingBoxCreationTO(length, width, height, referenceOffset);
  }

  /**
   * Returns the bounding box's reference offset.
   *
   * @return The bounding box's reference offset.
   */
  public CoupleCreationTO getReferenceOffset() {
    return referenceOffset;
  }

  /**
   * Creates a copy of this object, with the given reference offset.
   *
   * @param referenceOffset The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public BoundingBoxCreationTO withReferenceOffset(CoupleCreationTO referenceOffset) {
    return new BoundingBoxCreationTO(length, width, height, referenceOffset);
  }
}
