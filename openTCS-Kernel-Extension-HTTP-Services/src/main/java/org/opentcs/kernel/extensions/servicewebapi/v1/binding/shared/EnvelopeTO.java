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
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 */
public class EnvelopeTO {

  private String key;
  private List<CoupleTO> vertices;

  @JsonCreator
  public EnvelopeTO(
      @Nonnull @JsonProperty(value = "key", required = true) String key,
      @Nonnull @JsonProperty(value = "vertices", required = true) List<CoupleTO> vertices) {
    this.key = requireNonNull(key, "key");
    this.vertices = requireNonNull(vertices, "vertices");
  }

  @Nonnull
  public String getKey() {
    return key;
  }

  public EnvelopeTO setKey(@Nonnull String key) {
    this.key = requireNonNull(key, "key");
    return this;
  }

  @Nonnull
  public List<CoupleTO> getVertices() {
    return vertices;
  }

  public EnvelopeTO setVertices(@Nonnull List<CoupleTO> vertices) {
    this.vertices = requireNonNull(vertices, "vertices");
    return this;
  }

}
