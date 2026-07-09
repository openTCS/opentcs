// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data;

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
public class PeripheralOperationTO {

  @Nonnull
  @JsonProperty(value = "operation", required = true)
  private final String operation;
  @Nonnull
  @JsonProperty(value = "locationName", required = true)
  private final String locationName;
  private ExecutionTrigger executionTrigger = ExecutionTrigger.AFTER_ALLOCATION;
  private boolean completionRequired;

  public enum ExecutionTrigger {

    IMMEDIATE,
    AFTER_ALLOCATION,
    AFTER_MOVEMENT;
  }
}
// CHECKSTYLE:ON
