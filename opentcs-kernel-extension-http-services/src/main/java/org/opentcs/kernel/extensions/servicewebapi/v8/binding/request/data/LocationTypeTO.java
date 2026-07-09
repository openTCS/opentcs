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
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.PropertyTO;

// CHECKSTYLE:OFF
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@JsonPropertyOrder(alphabetic = true)
public class LocationTypeTO {

  @Nonnull
  @JsonProperty(value = "name", required = true)
  private final String name;
  private List<String> allowedOperations = List.of();
  private List<String> allowedPeripheralOperations = List.of();
  private Layout layout = new Layout();
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

    private LocationRepresentationTO locationRepresentation = LocationRepresentationTO.NONE;
  }
}
// CHECKSTYLE:ON
