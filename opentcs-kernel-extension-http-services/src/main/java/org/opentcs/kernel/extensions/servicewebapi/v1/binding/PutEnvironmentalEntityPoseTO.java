// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 * An update for an environmental entity's pose.
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PutEnvironmentalEntityPoseTO {

  private TripleTO position;
  private double orientationAngle;

  @JsonCreator
  public PutEnvironmentalEntityPoseTO(
      @Nonnull
      @JsonProperty(value = "position", required = true)
      TripleTO position,
      @JsonProperty(value = "orientationAngle", required = true)
      double orientationAngle
  ) {
    this.position = requireNonNull(position, "position");
    this.orientationAngle = orientationAngle;
  }
}
