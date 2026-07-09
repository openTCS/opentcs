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
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.TripleTO;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PointTO {

  @Nonnull
  @JsonProperty(value = "name", required = true)
  private final String name;
  private TripleTO position = new TripleTO(0, 0, 0);
  private double vehicleOrientationAngle = Double.NaN;
  private Type type = Type.HALT_POSITION;
  private Layout layout = new Layout();
  private List<EnvelopeTO> vehicleEnvelopes = List.of();
  private BoundingBoxTO maxVehicleBoundingBox
      = new BoundingBoxTO(1000, 1000, 1000, new CoupleTO(0, 0));
  private List<PropertyTO> properties = List.of();

  public enum Type {

    HALT_POSITION,
    PARK_POSITION;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class Layout {

    private CoupleTO position = new CoupleTO(0, 0);
    private CoupleTO labelOffset = new CoupleTO(0, 0);
    private int layerId;
  }
}
// CHECKSTYLE:ON
