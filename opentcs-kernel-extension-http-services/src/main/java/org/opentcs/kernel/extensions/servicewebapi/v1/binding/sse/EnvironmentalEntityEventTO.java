// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.sse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.TCSObjectEvent;

/**
 * A transfer object (to be used with the SSE API) representing a {@link TCSObjectEvent} for an
 * environmental entity.
 */
public class EnvironmentalEntityEventTO {

  private final EnvironmentalEntityTO currentObjectState;
  private final EnvironmentalEntityTO previousObjectState;

  public EnvironmentalEntityEventTO(
      @Nullable
      EnvironmentalEntityTO currentObjectState,
      @Nullable
      EnvironmentalEntityTO previousObjectState
  ) {
    this.currentObjectState = currentObjectState;
    this.previousObjectState = previousObjectState;
  }

  public EnvironmentalEntityTO getCurrentObjectState() {
    return currentObjectState;
  }

  public EnvironmentalEntityTO getPreviousObjectState() {
    return previousObjectState;
  }

  // CHECKSTYLE:OFF
  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class EnvironmentalEntityTO {

    private String name;
    private Map<String, String> properties;
    private ObjectHistoryTO history;
    private EnvelopeTO envelope;
    private PoseTO pose;
    private TypeTO type;
    private IntegrationLevelTO integrationLevel;
    private LayoutTO layout;
    private Instant createdTime;
    private Instant retiredTime;
  }

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

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class TripleTO {
    private long x;
    private long y;
    private long z;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  public static class CoupleTO {

    private long x;
    private long y;
  }

  public enum TypeTO {
    OBJECT,
    ZONE
  }

  public enum IntegrationLevelTO {
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
