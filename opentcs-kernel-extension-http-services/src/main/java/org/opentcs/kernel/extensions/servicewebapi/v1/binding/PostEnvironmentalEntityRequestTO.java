// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 * An environmental entity to be created in the kernel.
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PostEnvironmentalEntityRequestTO {

  private boolean incompleteName;
  private EnvelopeTO envelope;
  private PoseTO pose;
  private Type type;
  private IntegrationLevel integrationLevel;
  private LayoutTO layout;
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;

  @JsonCreator
  public PostEnvironmentalEntityRequestTO(
      @Nonnull
      @JsonProperty(value = "envelope", required = true)
      EnvelopeTO envelope,
      @Nonnull
      @JsonProperty(value = "pose", required = true)
      PoseTO pose
  ) {
    this.envelope = requireNonNull(envelope, "envelope");
    this.pose = requireNonNull(pose, "pose");
    this.type = Type.OBJECT;
    this.integrationLevel = IntegrationLevel.TO_BE_RESPECTED;
    this.layout = new LayoutTO();
    this.properties = new HashMap<>();
  }

  // CHECKSTYLE:OFF
  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class EnvelopeTO {
    private List<CoupleTO> vertices;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
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

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class LayoutTO {
    private int layerId;
  }
  // CHECKSTYLE:ON
}
