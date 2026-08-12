// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Dimensions of the load's bounding box.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoadDimensions
    implements
      Serializable {

  /**
   * Absolute length of the loads bounding box (in m).
   */
  private Double length;
  /**
   * Absolute width of the loads bounding box (in m).
   */
  private Double width;
  /**
   * [Optional] Absolute height of the loads bounding box (in m).
   */
  private Double height;

  @JsonCreator
  public LoadDimensions(
      @Nonnull
      @JsonProperty(required = true, value = "length")
      Double length,
      @Nonnull
      @JsonProperty(required = true, value = "width")
      Double width
  ) {
    this.length = requireNonNull(length, "length");
    this.width = requireNonNull(width, "width");
  }

  public Double getLength() {
    return length;
  }

  public LoadDimensions setLength(
      @Nonnull
      Double length
  ) {
    this.length = requireNonNull(length, "length");
    return this;
  }

  public Double getWidth() {
    return width;
  }

  public LoadDimensions setWidth(
      @Nonnull
      Double width
  ) {
    this.width = requireNonNull(width, "width");
    return this;
  }

  public Double getHeight() {
    return height;
  }

  public LoadDimensions setHeight(Double height) {
    this.height = height;
    return this;
  }

  @Override
  public String toString() {
    return "LoadDimensions{" + "length=" + length + ", width=" + width + ", height=" + height + '}';
  }

}
