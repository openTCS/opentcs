/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Couple;

/**
 * A representation of a {@link BoundingBox}.
 */
public class BoundingBoxModel {

  private final long length;
  private final long width;
  private final long height;
  private final Couple referenceOffset;

  /**
   * Creates a new instance.
   *
   * @param length The bounding box's length.
   * @param width The bounding box's width.
   * @param height The bounding box's height.
   * @param referenceOffset The bounding box's reference offset.
   */
  public BoundingBoxModel(
      long length,
      long width,
      long height,
      @Nonnull
      Couple referenceOffset
  ) {
    this.length = length;
    this.width = width;
    this.height = height;
    this.referenceOffset = requireNonNull(referenceOffset, "referenceOffset");
  }

  public long getLength() {
    return length;
  }

  public long getWidth() {
    return width;
  }

  public long getHeight() {
    return height;
  }

  public Couple getReferenceOffset() {
    return referenceOffset;
  }
}
