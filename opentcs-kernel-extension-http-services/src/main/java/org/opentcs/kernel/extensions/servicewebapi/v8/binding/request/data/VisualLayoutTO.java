// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class VisualLayoutTO {

  @Nonnull
  @JsonProperty(value = "name", required = true)
  private final String name;
  private double scaleX = 50.0;
  private double scaleY = 50.0;
  private List<LayerTO> layers = List.of(new LayerTO(0, 0, true, "layer0", 0));
  private List<LayerGroupTO> layerGroups = List.of(new LayerGroupTO(0, "layerGroup0", true));
  private List<PropertyTO> properties = List.of();
}
// CHECKSTYLE:ON
