// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.Map;
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
public class PostVehicleCommAdapterMessageRequestTO {

  @Nonnull
  @JsonProperty(value = "type", required = true)
  private final String type;
  @Nonnull
  @JsonProperty(value = "parameters", required = true)
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> parameters;
}
// CHECKSTYLE:ON
