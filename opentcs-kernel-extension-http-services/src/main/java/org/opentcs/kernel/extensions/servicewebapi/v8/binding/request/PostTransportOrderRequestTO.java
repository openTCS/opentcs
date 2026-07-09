// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
public class PostTransportOrderRequestTO {

  private boolean incompleteName;
  private boolean dispensable;
  @Nullable
  private Instant deadline;
  @Nullable
  private String intendedVehicle;
  @Nullable
  private String peripheralReservationToken;
  @Nullable
  private String wrappingSequence;
  @Nullable
  private String type;
  @Nonnull
  @JsonProperty(required = true, value = "destinations")
  private final List<Destination> destinations;
  @Nullable
  @JsonPropertyOrder(alphabetic = true)
  private Map<String, String> properties;
  @Nullable
  private List<String> dependencies;

  @RequiredArgsConstructor
  @Getter
  @Setter
  @EqualsAndHashCode
  @Accessors(chain = true)
  @JsonPropertyOrder(alphabetic = true)
  public static class Destination {

    @Nonnull
    @JsonProperty(required = true, value = "locationName")
    private final String locationName;
    @Nonnull
    @JsonProperty(required = true, value = "operation")
    private final String operation;
    @Nullable
    @JsonPropertyOrder(alphabetic = true)
    private Map<String, String> properties;
  }
}
// CHECKSTYLE:ON
