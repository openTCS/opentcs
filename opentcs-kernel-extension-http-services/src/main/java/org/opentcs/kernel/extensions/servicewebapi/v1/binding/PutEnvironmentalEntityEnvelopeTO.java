// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;

/**
 * An update for an environmental entity's envelope.
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PutEnvironmentalEntityEnvelopeTO {
  private List<CoupleTO> vertices;

  @JsonCreator
  public PutEnvironmentalEntityEnvelopeTO(
      @Nonnull
      @JsonProperty(value = "vertices", required = true)
      List<CoupleTO> vertices
  ) {
    this.vertices = requireNonNull(vertices, "vertices");
  }
}
