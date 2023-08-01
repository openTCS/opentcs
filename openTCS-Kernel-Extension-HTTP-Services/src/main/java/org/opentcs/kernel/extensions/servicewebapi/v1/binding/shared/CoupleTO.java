/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public class CoupleTO {

  private long x;
  private long y;

  @JsonCreator
  public CoupleTO(@JsonProperty(value = "x", required = true) long x,
                  @JsonProperty(value = "y", required = true) long y) {
    this.x = x;
    this.y = y;
  }

  public long getX() {
    return x;
  }

  public CoupleTO setX(long x) {
    this.x = x;
    return this;
  }

  public long getY() {
    return y;
  }

  public CoupleTO setY(long y) {
    this.y = y;
    return this;
  }
}
