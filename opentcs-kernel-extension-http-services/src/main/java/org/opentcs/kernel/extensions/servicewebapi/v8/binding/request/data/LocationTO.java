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
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.LinkTO;
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
public class LocationTO {

  @Nonnull
  @JsonProperty(value = "name", required = true)
  private final String name;
  @Nonnull
  @JsonProperty(value = "typeName", required = true)
  private final String typeName;
  @Nonnull
  @JsonProperty(value = "position", required = true)
  private final TripleTO position;
  private List<LinkTO> links = List.of();
  private boolean locked;
  private Layout layout = new Layout();
  private List<PropertyTO> properties = List.of();


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
    private LocationRepresentationTO locationRepresentation = LocationRepresentationTO.DEFAULT;
    private int layerId;
  }
}
// CHECKSTYLE:ON
