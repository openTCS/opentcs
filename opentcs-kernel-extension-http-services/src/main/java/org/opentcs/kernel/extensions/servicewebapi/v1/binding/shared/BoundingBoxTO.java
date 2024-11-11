// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;

/**
 */
public class BoundingBoxTO {

  private long length;
  private long width;
  private long height;
  private CoupleTO referenceOffset;

  @JsonCreator
  public BoundingBoxTO(
      @JsonProperty(value = "length", required = true)
      long length,
      @JsonProperty(value = "width", required = true)
      long width,
      @JsonProperty(value = "height", required = true)
      long height,
      @JsonProperty(value = "referenceOffset", required = true)
      @Nonnull
      CoupleTO referenceOffset
  ) {
    this.length = length;
    this.width = width;
    this.height = height;
    this.referenceOffset = requireNonNull(referenceOffset, "referenceOffset");
  }

  public long getLength() {
    return length;
  }

  public BoundingBoxTO setLength(long length) {
    this.length = length;
    return this;
  }

  public long getWidth() {
    return width;
  }

  public BoundingBoxTO setWidth(long width) {
    this.width = width;
    return this;
  }

  public long getHeight() {
    return height;
  }

  public BoundingBoxTO setHeight(long height) {
    this.height = height;
    return this;
  }

  @Nonnull
  public CoupleTO getReferenceOffset() {
    return referenceOffset;
  }

  public BoundingBoxTO setReferenceOffset(
      @Nonnull
      CoupleTO referenceOffset
  ) {
    this.referenceOffset = requireNonNull(referenceOffset, "referenceOffset");
    return this;
  }
}
