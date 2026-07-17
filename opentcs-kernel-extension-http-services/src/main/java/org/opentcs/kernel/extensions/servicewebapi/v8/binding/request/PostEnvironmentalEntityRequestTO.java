// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.TripleTO;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PostEnvironmentalEntityRequestTO {

  @Nonnull
  @JsonProperty(value = "envelope", required = true)
  private final EnvelopeTO envelope;
  @Nonnull
  @JsonProperty(value = "pose", required = true)
  private final PoseTO pose;
  private boolean incompleteName;
  private Type type = Type.OBJECT;
  private IntegrationLevel integrationLevel = IntegrationLevel.TO_BE_RESPECTED;
  private LayoutTO layout = new LayoutTO();
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties = new HashMap<>();

  @RequiredArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class EnvelopeTO {
    private List<CoupleTO> vertices;
  }

  @RequiredArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class PoseTO {
    private TripleTO position;
    private double orientationAngle;
  }

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
