// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

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
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.PoseTO;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PointTO {

  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private PoseTO pose;
  @Nonnull
  private TypeTO type;
  @Nonnull
  private List<String> incomingPaths;
  @Nonnull
  private List<String> outgoingPaths;
  @Nonnull
  private List<LinkTO> attachedLinks;
  @Nullable
  private String occupyingVehicle;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, EnvelopeTO> vehicleEnvelopes;
  @Nonnull
  private BoundingBoxTO maxVehicleBoundingBox;
  @Nonnull
  private LayoutTO layout;

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class LayoutTO {

    private CoupleTO labelOffset;
    private int layerId;
  }

  public enum TypeTO {
    HALT_POSITION,
    PARK_POSITION;
  }
}
// CHECKSTYLE:ON
