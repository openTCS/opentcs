// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PathTO {

  @Nonnull
  @JsonProperty(value = "name", required = true)
  private final String name;
  @Nonnull
  @JsonProperty(value = "srcPointName", required = true)
  private final String srcPointName;
  @Nonnull
  @JsonProperty(value = "destPointName", required = true)
  private final String destPointName;
  private long length = 1;
  private int maxVelocity;
  private int maxReverseVelocity;
  private List<PeripheralOperationTO> peripheralOperations = List.of();
  private boolean locked;
  private Layout layout = new Layout();
  private List<EnvelopeTO> vehicleEnvelopes = List.of();
  private List<PropertyTO> properties = List.of();

  // CHECKSTYLE:OFF
  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class Layout {

    private ConnectionType connectionType = ConnectionType.DIRECT;
    private List<CoupleTO> controlPoints = List.of();
    private int layerId;

    public enum ConnectionType {

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
