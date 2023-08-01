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
public class TripleTO {

  private long x;
  private long y;
  private long z;

  @JsonCreator
  public TripleTO(@JsonProperty(value = "x", required = true) long x,
                  @JsonProperty(value = "y", required = true) long y,
                  @JsonProperty(value = "z", required = true) long z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public long getX() {
    return x;
  }

  public TripleTO setX(long x) {
    this.x = x;
    return this;
  }

  public long getY() {
    return y;
  }

  public TripleTO setY(long y) {
    this.y = y;
    return this;
  }

  public long getZ() {
    return z;
  }

  public TripleTO setZ(long z) {
    this.z = z;
    return this;
  }

}
