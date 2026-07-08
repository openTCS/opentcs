// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opentcs.data.model.LocationType;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.response.data.shared.LocationRepresentationTO;

/**
 * A transfer object representing a {@link LocationType} instance.
 */
// CHECKSTYLE:OFF
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class LocationTypeTO {

  @Nonnull
  private String name;
  @Nonnull
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nonnull
  private List<String> allowedOperations;
  @Nonnull
  private List<String> allowedPeripheralOperations;
  @Nonnull
  private LayoutTO layout;

  @NoArgsConstructor
  @Getter
  @Setter
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class LayoutTO {

    @Nonnull
    private LocationRepresentationTO locationRepresentation;
  }
}

// CHECKSTYLE:ON
