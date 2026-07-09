// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Set;
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
public class BlockTO {

  @Nonnull
  @JsonProperty(value = "name", required = true)
  private final String name;
  private Type type = Type.SINGLE_VEHICLE_ONLY;
  private Layout layout = new Layout();
  private Set<String> memberNames = Set.of();
  private List<PropertyTO> properties = List.of();

  public enum Type {
    SINGLE_VEHICLE_ONLY,
    SAME_DIRECTION_ONLY
  }

  @NoArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @ToString
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class Layout {
    private String color = "#FF0000";
  }
}
// CHECKSTYLE:ON
