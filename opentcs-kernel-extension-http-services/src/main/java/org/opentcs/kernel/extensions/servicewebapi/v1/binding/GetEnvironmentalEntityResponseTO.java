// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.ObjectHistoryTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

// CHECKSTYLE:OFF
/**
 * The current state of an environmental entity.
 */
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class GetEnvironmentalEntityResponseTO {

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
}
// CHECKSTYLE:ON
