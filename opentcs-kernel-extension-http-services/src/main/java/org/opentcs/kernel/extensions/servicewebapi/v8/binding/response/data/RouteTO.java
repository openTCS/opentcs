// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class RouteTO {

  @Nonnull
  private String destinationPoint = "";
  private long costs = -1;
  @Nullable
  private List<Step> steps;

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class Step {

    @Nullable
    private String path;
    @Nullable
    private String sourcePoint;
    @Nonnull
    private String destinationPoint = "";
    @Nonnull
    private String vehicleOrientation
        = TransportOrderTO.DriveOrderTO.RouteTO.StepTO.VehicleOrientationTO.UNDEFINED.name();
  }
}
// CHECKSTYLE:ON
