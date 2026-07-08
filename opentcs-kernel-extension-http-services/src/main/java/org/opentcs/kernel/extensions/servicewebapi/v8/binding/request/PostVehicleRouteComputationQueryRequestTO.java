// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 *
 */
public class PostVehicleRouteComputationQueryRequestTO {

  private String sourcePoint;
  private List<String> destinationPoints;
  private List<String> resourcesToAvoid;

  @JsonCreator
  @SuppressWarnings("checkstyle:LineLength")
  public PostVehicleRouteComputationQueryRequestTO(
      @Nonnull
      @JsonProperty(value = "destinationPoints", required = true)
      List<String> destinationPoints
  ) {
    this.destinationPoints = requireNonNull(destinationPoints, "destinationPoints");
  }

  @Nullable
  public String getSourcePoint() {
    return sourcePoint;
  }

  public PostVehicleRouteComputationQueryRequestTO setSourcePoint(
      @Nullable
      String sourcePoint
  ) {
    this.sourcePoint = sourcePoint;
    return this;
  }

  @Nonnull
  public List<String> getDestinationPoints() {
    return destinationPoints;
  }

  public PostVehicleRouteComputationQueryRequestTO setDestinationPoints(
      @Nonnull
      List<String> destinationPoints
  ) {
    this.destinationPoints = requireNonNull(destinationPoints, "destinationPoints");
    return this;
  }

  @Nullable
  public List<String> getResourcesToAvoid() {
    return resourcesToAvoid;
  }

  public PostVehicleRouteComputationQueryRequestTO setResourcesToAvoid(
      @Nullable
      List<String> resourcesToAvoid
  ) {
    this.resourcesToAvoid = resourcesToAvoid;
    return this;
  }
}
