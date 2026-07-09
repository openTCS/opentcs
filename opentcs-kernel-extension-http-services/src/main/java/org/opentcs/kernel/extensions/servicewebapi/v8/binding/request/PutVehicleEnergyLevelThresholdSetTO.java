// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class PutVehicleEnergyLevelThresholdSetTO {

  @JsonProperty(value = "energyLevelCritical", required = true)
  private final int energyLevelCritical;
  @JsonProperty(value = "energyLevelGood", required = true)
  private final int energyLevelGood;
  @JsonProperty(value = "energyLevelSufficientlyRecharged", required = true)
  private final int energyLevelSufficientlyRecharged;
  @JsonProperty(value = "energyLevelFullyRecharged", required = true)
  private final int energyLevelFullyRecharged;
}
// CHECKSTYLE:ON
