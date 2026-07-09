// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PostOrderSequenceRequestTO {

  private boolean incompleteName;
  @Nonnull
  private List<String> orderTypes = List.of();
  @Nullable
  private String intendedVehicle;
  private boolean failureFatal;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties = Map.of();
}
// CHECKSTYLE:ON
