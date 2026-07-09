// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class OrderSequenceTO {
  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private ObjectHistoryTO history;
  @Nonnull
  private List<String> orderTypes;
  @Nonnull
  private List<String> orders;
  private int finishedIndex;
  private boolean complete;
  private boolean finished;
  private boolean failureFatal;
  @Nullable
  private String intendedVehicle;
  @Nullable
  private String processingVehicle;
  @Nonnull
  private Instant creationTime;
  @Nullable
  private Instant finishedTime;
}
// CHECKSTYLE:ON
