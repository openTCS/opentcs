// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.EnvelopeTO;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PathTO {

  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private String sourcePoint;
  @Nonnull
  private String destinationPoint;
  private long length;
  private int maxVelocity;
  private int maxReverseVelocity;
  @Nonnull
  private List<PeripheralOperationTO> peripheralOperations;
  private boolean locked;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, EnvelopeTO> vehicleEnvelopes;
  @Nonnull
  private LayoutTO layout;

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class PeripheralOperationTO {

    @Nonnull
    private String location;
    @Nonnull
    private String operation;
    @Nonnull
    private ExecutionTriggerTO executionTrigger;
    private boolean completionRequired;

    public enum ExecutionTriggerTO {
      IMMEDIATE,
      AFTER_ALLOCATION,
      AFTER_MOVEMENT;
    }
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class LayoutTO {

    @Nonnull
    private ConnectionTypeTO connectionType;
    @Nonnull
    private List<CoupleTO> controlPoints;
    private int layerId;

    public enum ConnectionTypeTO {

      DIRECT,
      ELBOW,
      SLANTED,
      POLYPATH,
      BEZIER,
      BEZIER_3
    }
  }
}
// CHECKSTYLE:ON
