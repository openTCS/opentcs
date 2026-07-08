// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.model.PlantModel;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.VisualLayoutTO;

/**
 * A transfer object representing a {@link PlantModel} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class GetPlantModelResponseTO {

  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  private List<PointTO> points;
  private List<PathTO> paths;
  private List<LocationTypeTO> locationTypes;
  private List<LocationTO> locations;
  private List<BlockTO> blocks;
  private List<VehicleTO> vehicles;
  private VisualLayoutTO visualLayout;
}
// CHECKSTYLE:ON
