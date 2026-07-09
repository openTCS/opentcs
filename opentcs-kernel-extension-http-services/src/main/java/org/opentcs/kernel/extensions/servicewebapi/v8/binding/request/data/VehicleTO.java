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
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class VehicleTO {

  @Nonnull
  @JsonProperty(value = "name", required = true)
  private final String name;
  private BoundingBoxTO boundingBox = new BoundingBoxTO(1000, 1000, 1000, new CoupleTO(0, 0));
  private int energyLevelCritical = 30;
  private int energyLevelGood = 90;
  private int energyLevelFullyRecharged = 90;
  private int energyLevelSufficientlyRecharged = 30;
  private int maxVelocity = 1000;
  private int maxReverseVelocity = 1000;
  private Layout layout = new Layout();
  private List<PropertyTO> properties = List.of();


  public enum State {

    UNKNOWN,
    UNAVAILABLE,
    ERROR,
    IDLE,
    EXECUTING,
    CHARGING
  }

  public enum IntegrationLevel {

    TO_BE_IGNORED,
    TO_BE_NOTICED,
    TO_BE_RESPECTED,
    TO_BE_UTILIZED
  }

  public enum ProcState {

    IDLE,
    AWAITING_ORDER,
    PROCESSING_ORDER
  }

  public enum Orientation {

    UNDEFINED
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class Layout {

    private String routeColor = "#00FF00";
  }
}
// CHECKSTYLE:ON
