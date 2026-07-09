// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class BoundingBoxTO {

  @JsonProperty(value = "length", required = true)
  private final long length;
  @JsonProperty(value = "width", required = true)
  private final long width;
  @JsonProperty(value = "height", required = true)
  private final long height;
  @Nonnull
  @JsonProperty(value = "referenceOffset", required = true)
  private final CoupleTO referenceOffset;
}
// CHECKSTYLE:ON
