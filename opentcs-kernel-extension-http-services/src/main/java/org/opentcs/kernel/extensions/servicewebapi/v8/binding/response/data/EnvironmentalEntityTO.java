// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.PoseTO;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class EnvironmentalEntityTO {

  private String name = "";
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties = new HashMap<>();
  private ObjectHistoryTO history;
  private EnvelopeTO envelope;
  private PoseTO pose;
  private Type type;
  private IntegrationLevel integrationLevel;
  private LayoutTO layout;
  private boolean retired;
  private Instant createdTime;
  private Instant retiredTime;

  public enum Type {
    OBJECT,
    ZONE
  }

  public enum IntegrationLevel {
    TO_BE_IGNORED,
    TO_BE_NOTICED,
    TO_BE_RESPECTED
  }

  @RequiredArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class LayoutTO {
    private int layerId;
  }
}
// CHECKSTYLE:ON
